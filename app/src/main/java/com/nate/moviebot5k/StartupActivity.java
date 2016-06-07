package com.nate.moviebot5k;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.nate.moviebot5k.api_fetching.GenresAndCertsFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * Initializes this app:
 * <br>
 *     1. at first install - creates all the necessary db tables and initializes all sharedPrefs
 *     with default values
 *     <br>
 *     2. any startup after first install - clears out the vidoes, reviews, and credits tables
 *     of all records not marked as 'favorites'
 *
 * <br><br>
 * Fires a fetch task to see if a connection to themoviedb is available:
 * <br>
 *     1. if connection was ok - launches intent to ActivityHome
 *     <br>
 *     2. if connection error - launches intent to ActivityFavorites IF user has at least one
 *     favorite saved, if they have NO favorites and NO network connection, then app just shows
 *     and error message because there really nothing to do at that point
 *
 * <br><br>
 * Note: a splashscreen is shown in the background while this is all going on.
 *
 */
public class StartupActivity extends AppCompatActivity {
    private static final String LOGTAG = ActivitySingleFragment.N8LOG + "StartupActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        TextView messageTextView = (TextView) findViewById(R.id.problem_message);

        initializeSharedPrefs();

        clearCreditsVideosReviewsTables();

        // after this task returns, the app will either go to ActivityFavorites or ActivityHome
        new FetchGenresAndCertsTask(this, messageTextView).execute();
    }


    // NOTE: old movies records are deleted every time new movies are fetched in MoviesFetcher,
    // however for these tables, just clean out the non favorites records each time app starts from dead
    private void clearCreditsVideosReviewsTables() {
        Log.i(LOGTAG, "about to clear out credits, videos, and reviews tables but only NON favorites records");
        String[] selectionArgs = new String[]{ "false" };

        getContentResolver().delete(MovieTheaterContract.CreditsEntry.CONTENT_URI,
                MovieTheaterContract.CreditsEntry.COLUMN_IS_FAVORITE + " = ?", selectionArgs);

        getContentResolver().delete(MovieTheaterContract.VideosEntry.CONTENT_URI,
                MovieTheaterContract.VideosEntry.COLUMN_IS_FAVORITE + " = ?", selectionArgs);

        getContentResolver().delete(MovieTheaterContract.ReviewsEntry.CONTENT_URI,
                MovieTheaterContract.ReviewsEntry.COLUMN_IS_FAVORITE + " = ?", selectionArgs);
    }


    /**
     * Launches background thread tasks to fetch a list of genres and certifications from
     * themoviedb's servers.  I arbitrarily chose to use the number of genres returned as evidence
     * of a good or faulty connection to themoviedb.  To clarify: if zero genres are returned, this
     * app assumes an internet connection is not available and will then launch an intent to
     * ActivityFavorites.  If at least 10 (also an arbitrary number) genres were returned, this app
     * assumes an internet connection is good to go and launches an intent to ActivityHome.
     */
    private class FetchGenresAndCertsTask extends AsyncTask<Void, Void, Integer> {
        Context context;
        TextView messageTV;

        private FetchGenresAndCertsTask(Context c, TextView message) {
            context = c;
            messageTV = message;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            new GenresAndCertsFetcher(context).fetchAvailableCertifications();
            return new GenresAndCertsFetcher(context).fetchAvailableGenres();
        }

        @Override
        protected void onPostExecute(Integer numGenresFetched) {
            Log.i(LOGTAG,"  in FetchGenresAndCertsTask.onPostExecute, numGenresFetched was: " + numGenresFetched);

            // if at least 10 genres was fetched, the assumption here is that it was successful
            // so go ahead and launch ActivityHome
            if (numGenresFetched > 10) { // 10 is arbitrary
                Log.i(LOGTAG, "    since there were at least 10 genres fetched, connection to" +
                        " themoviedb must be ok, so about to launch intent to ActivityHome");

                Intent intent = new Intent(context, ActivityHome.class);
                startActivity(intent);
                // finish this activity so that clicking 'back' from ActivityHome exits the app
                finish();

            }
            // no items were returned, so check if user has any favorites saved
            // first get a cursor that points to the favorites table, projection does not matter,
            // so just arbitrarily use movie_id
            else {
                Cursor cursor = getContentResolver().query(
                        MovieTheaterContract.MoviesEntry.CONTENT_URI,
                        new String[]{MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID},
                        MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE + " = ?",
                        new String[]{ "true" },
                        null);

                if(cursor == null) {
                    messageTV.setText(getString(R.string.startup_activity_bad_db));
                }
                else {
                    try {
                        if (cursor.moveToFirst()) {
                            // user has at least one favorite saved locally
                            Log.i(LOGTAG, "    no connection to themoviedb, BUT user has at least one favorite" +
                                    " saved, so about to launch an intent to FavoritesActivity");

                            Intent intent = new Intent(context, ActivityFavorites.class);
                            TaskStackBuilder.create(context).addNextIntentWithParentStack(intent).startActivities();
                            // finish this activity because we want 'back' to exit the app from ActivityFavorites
                            finish();

                        } else {
                            // not much can be done at this point, no connection to themoviedb AND
                            // user has no favorites saved, so just need to show them an appropriate msg
                            messageTV.setText(getString(R.string.startup_activity_dead_end));
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } // end else when numGenres fetched was < 10
        } // end onPostExecute
    } // end AsyncTask


    /**
     * Prepares all sharedPrefs: if they do not exist, they will be created and defaults will be written.
     * If they already exist, nothing happens.
     */
    private void initializeSharedPrefs() {
        Log.i(LOGTAG, "entered initializeSharedPrefs, will report if they do not exist yet");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // if any single sharedPrefs exists, then they all do and have already been initialized
        if(!sharedPreferences.contains(getString(R.string.key_movie_filter_year))) {
            Log.i(LOGTAG, "  sharedPrefs are being created for the first time, writing defaults...");

            editor.putInt(getString(R.string.key_num_favorites), 0);

            editor.putString(getString(R.string.key_movie_filter_sortby_value),
                    getString(R.string.default_movie_filter_sortby_value));

            editor.putString(getString(R.string.key_movie_filter_year),
                    getString(R.string.default_movie_filter_year));

            editor.putString(getString(R.string.key_movie_filter_cert),
                    getString(R.string.default_movie_filter_cert));

            editor.putString(getString(R.string.key_movie_filter_genre_id),
                    getString(R.string.default_movie_filter_genre_id));

            editor.putString(getString(R.string.key_favorites_sortby_value),
                    getString(R.string.default_favorites_sortby_value));

            editor.putInt(getString(R.string.key_currently_selected_movie_id),
                    getResources().getInteger(R.integer.default_currently_selected_movie_id));

            editor.putInt(getString(R.string.key_currently_selected_favorite_id),
                    getResources().getInteger(R.integer.default_currently_selected_favorite_id));

//            editor.putBoolean(getString(R.string.key_fetch_new_movies), true);

            // all spinners start at zeroth position
            editor.putInt(getString(R.string.key_movie_filter_year_spinner_position), 0);
            editor.putInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0);
            editor.putInt(getString(R.string.key_movie_filter_cert_spinner_position), 0);
            editor.putInt(getString(R.string.key_movie_filter_genre_spinner_position), 0);

            // the favorites sortby dialog fragment also starts in zeroth position
            editor.putInt(getString(R.string.key_favorites_sortby_selected_item_position), 0);

//            editor.commit();
        }

        // and it's always a good idea to fetch new movies when the app starts from dead
        // because the movies in themoviedb database may have changed since app was last used
        editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
        editor.commit();

    }

}
