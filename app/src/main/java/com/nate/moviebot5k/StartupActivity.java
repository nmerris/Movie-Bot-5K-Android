package com.nate.moviebot5k;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.nate.moviebot5k.api_fetching.GenresAndCertsFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * Initializes this app the first time it is installed on device, and detects if internet is
 * available.  If internet is available, launches Intent to ActivityHome, if not it will check to
 * see if user has at least one favorite, and then asks user via an AlertDialog if they would like
 * to view their favorites.  If so, launches and Intent to FavoritesActivity.
 */
public class StartupActivity extends AppCompatActivity {
    private static final String LOGTAG = ActivitySingleFragment.N8LOG + "StartupActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");


        initializeSharedPrefs();
        //showDebugLog();

        clearCreditsVideosReviewsTables();


        // go fetch a new list of genres and certs in a background thread, the app will then either continue
        // on to ActivityHome if successful, or the user will be presented with a choice to view
        // their favorites (if they have any), or the app will just show a msg saying that it needs
        // to have internet connection to work (and that they have not favorites)
        new FetchGenresAndCertsTask(this).execute();


    }


    // NOTE: the movies table is wiped out every time a new api call is successfully made in FragmentMovieGrid
    // the reason these tables are wiped out here are because we want to their data around while the user
    // is currently in an 'app session'.. it would not be efficient to wipe each of these tables out
    // every time a new movie is clicked to view details.. it's possible the user might navigate back
    // to the same movie details page they were recently on.. that being said, considering the almost
    // limitless amount of movies that this app may see, it does not make sense to keep this data around
    // forever, furthermore the reviews and videos might change from time to time
    private void clearCreditsVideosReviewsTables() {
        Log.i(LOGTAG, "about to clear out credits, videos, and reviews tables");
        getContentResolver().delete(MovieTheaterContract.CreditsEntry.CONTENT_URI, null, null);
        getContentResolver().delete(MovieTheaterContract.VideosEntry.CONTENT_URI, null, null);
        getContentResolver().delete(MovieTheaterContract.ReviewsEntry.CONTENT_URI, null, null);
    }


    private class FetchGenresAndCertsTask extends AsyncTask<Void, Void, Integer> {

        // async task needs it's own Context it can hold on to, in case of orientation change while
        // do in background is running
        Context context;

        private FetchGenresAndCertsTask(Context c) {
            context = c;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(LOGTAG, "just entered FetchGenresAndCertsTask.doInBackground");

            // fetch a new list of certs from themoviedb, the certifications table will be updated
            new GenresAndCertsFetcher(context).fetchAvailableCertifications();

            // I'm choosing to use genres as the task that returns the number of items fetched,
            // could be either, it's arbitrary, the genres table will also be updated
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
                finish();

            }
            // no items were returned, so check if user has any favorites saved
            // first get a cursor that points to the favorites table, projection does not matter,
            // so just arbitrarily use movie_id
            else {
                Cursor cursor = getContentResolver().query(
                        MovieTheaterContract.FavoritesEntry.CONTENT_URI,
                        new String[]{MovieTheaterContract.FavoritesEntry.COLUMN_MOVIE_ID},
                        null, null, null);

                if(cursor == null) {
                    Log.e(LOGTAG, "    Woah there buddy, somehow a Cursor was null, this should never happen!");

                    // TODO: show msg to user about data being bad, try reinstalling app

                }
                else {
                    try {
                        if (cursor.moveToFirst()) {
                            // user has at least one favorite saved locally

                            Log.i(LOGTAG, "    no connection to themoviedb, BUT user has at least one favorite" +
                                    " saved, so about to launch an intent to FavoritesActivity");

                            // TODO: launch intent to FavoritesActivity
                        } else {
                            // not much can be done at this point, no connection to themoviedb AND
                            // user has no favorites saved, so just need to show them an appropriate msg

                            Log.i(LOGTAG, "    NOTHING can be done at this point," +
                                    " no connection to themoviedb and no favorites saved");

                            // TODO: show msg to user
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } // end else when numGenres fetched was < 10
        } // end onPostExecute
    } // end AsyncTask



    // initialize all sharedPrefs, need this to happen the first time app is installed
    // or if user clears the app data, they will either ALL exist, or NONE will exist
    private void initializeSharedPrefs() {
        Log.i(LOGTAG, "entered initializeSharedPrefs, will report if they do not exist yet");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // if any single sharedPrefs exists, then they all do and have already been initialized
        if(!sharedPreferences.contains(getString(R.string.key_movie_filter_year))) {
            Log.i(LOGTAG, "  sharedPrefs are being created for the first time, writing defaults...");

            // TODO: prob won't end up using num_favorites, easier to just check db each time
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
            editor.putInt(getString(R.string.key_favorites_sortby_spinner_position), 0);

//            editor.commit();
        }

        // and it's always a good idea to fetch new movies when the app starts from dead
        // because the movies in themoviedb database may have changed since app was last used
        editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
        editor.commit();

    }

    // just pumps out a bunch of stuff that I used when writing this app, mostly db initialization
    private void showDebugLog() {

        // MovieTheaterContract movies table
        Log.i(LOGTAG, "MoviesEntry CONTENT_URI: " + MovieTheaterContract.MoviesEntry.CONTENT_URI);
        Log.i(LOGTAG, "MoviesEntry CONTENT_TYPE: " + MovieTheaterContract.MoviesEntry.CONTENT_TYPE);
        Log.i(LOGTAG, "MoviesEntry CONTENT_ITEM_TYPE: " + MovieTheaterContract.MoviesEntry.CONTENT_ITEM_TYPE);
        Log.i(LOGTAG, "MoviesEntry COLUMN_POPULARITY: " + MovieTheaterContract.MoviesEntry.COLUMN_POPULARITY);
        Log.i(LOGTAG, "MoviesEntry Uri returned from buildMovieUriFromMovieId(999): "
            + MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(999));

        // MovieTheaterContract favorites table
        Log.i(LOGTAG, "FavoritesEntry CONTENT_URI: " + MovieTheaterContract.FavoritesEntry.CONTENT_URI);
        Log.i(LOGTAG, "FavoritesEntry CONTENT_TYPE: " + MovieTheaterContract.FavoritesEntry.CONTENT_TYPE);
        Log.i(LOGTAG, "FavoritesEntry CONTENT_ITEM_TYPE: " + MovieTheaterContract.FavoritesEntry.CONTENT_ITEM_TYPE);
        Log.i(LOGTAG, "FavoritesEntry COLUMN_POPULARITY: " + MovieTheaterContract.FavoritesEntry.COLUMN_POPULARITY);
        Log.i(LOGTAG, "FavoritesEntry COLUMN_BACKDROP_FILE_PATH: "
                + MovieTheaterContract.FavoritesEntry.COLUMN_BACKDROP_FILE_PATH);
        Log.i(LOGTAG, "FavoritesEntry Uri returned from buildMovieUriFromMovieId(888): "
                + MovieTheaterContract.FavoritesEntry.buildFavoriteUriFromMovieId(888));

        // MovieTheaterDbHelper create tables
//        MovieTheaterDbHelper testHelper = new MovieTheaterDbHelper(this);
//        testHelper.getReadableDatabase();



    }
}
