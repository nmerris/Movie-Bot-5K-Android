package com.nate.moviebot5k;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.nate.moviebot5k.api_fetching.GenresFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.nate.moviebot5k.data.MovieTheaterDbHelper;

import java.util.List;

/**
 * Initializes this app the first time it is installed on device, and detects if internet is
 * available.  If internet is available, launches Intent to HomeActivity, if not it will check to
 * see if user has at least one favorite, and then asks user via an AlertDialog if they would like
 * to view their favorites.  If so, launches and Intent to FavoritesActivity.
 */
public class StartupActivity extends AppCompatActivity
        /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "StartupActivity";

    private static final int GENRE_LOADER_ID = 1;
    private static final int CERTS_LOADER_ID = 2;


    private Context mContext;
    private int mNumGenresFetched = 0;
    private int mNumCertsFetched = 0;


    // TODO: prevent StartupActivity from allowing orientation changes
    // TODO: need to have a timer (maybe a few seconds) that will basically assume themoviedb can't be reached and then
    // displays a msg prompting user to view favorites (if they have any) or just stops the app with an explanation


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");

        mContext = this;

        initializeSharedPrefs();
        showDebugLog();


        // go fetch a new list of genres in a background thread, the app will then either continue
        // on to HomeActivity if successful, or the user will be presented with a choice to view
        // their favorites (if they have any), or the app will just show a msg saying that it needs
        // to have internet connection to work (and that they have not favorites)
        new FetchGenresTask().execute();


        // TODO: prob. best to make one async task for fetch geners AND certs



//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        finish();

    }



//    // this fires an async task to fetch a list of genres and eventually update the genres db table
//    private void updateGenres() {
//        Log.i(LOGTAG, "entered updateGenres");
//
//        new FetchGenresTask().execute();
//
//
//
//
//
//
//
//    }




    private class FetchGenresTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(LOGTAG, "just entered FetchGenresTask.doInBackground");
            return new GenresFetcher(mContext).fetchAvailableGenres();
        }

        @Override
        protected void onPostExecute(Integer numGenresFetched) {

            mNumGenresFetched = numGenresFetched;

            Log.i(LOGTAG,"EXITING FetchGenresTask.onPostExecute, numGenresFetched was: " + numGenresFetched);
        }

    }






//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        Log.d(LOGTAG, "entered onStart");
//
//
//        updateGenres();
//
//
//
//
//    }


    // initialize all sharedPrefs, need this to happen the first time app is installed
    // or if user clears the app data, they will either ALL exist, or NONE will exist
    private void initializeSharedPrefs() {
        Log.i(LOGTAG, "entered initializeSharedPrefs, will report if they do not exist yet");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // if any single sharedPrefs exists, then they all do and have already been initialized
        if(!sharedPreferences.contains(getString(R.string.key_num_favorites))) {
            Log.i(LOGTAG, "  sharedPrefs are being created, writing defaults...");
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(getString(R.string.key_num_favorites),
                    getResources().getInteger(R.integer.default_num_favorites));
            editor.putString(getString(R.string.key_movie_filter_sortby),
                    getString(R.string.default_movie_filter_sortby));
            editor.putInt(getString(R.string.key_movie_filter_year),
                    getResources().getInteger(R.integer.default_movie_filter_year));
            editor.putString(getString(R.string.key_movie_filter_cert),
                    getString(R.string.default_movie_filter_cert));
            editor.putString(getString(R.string.key_movie_filter_genre),
                    getString(R.string.default_movie_filter_genre));
            editor.putString(getString(R.string.key_favorites_sortby),
                    getString(R.string.default_favorites_sortby));
            editor.putInt(getString(R.string.key_currently_selected_movie_id),
                    getResources().getInteger(R.integer.default_currently_selected_movie_id));
            editor.putInt(getString(R.string.key_currently_selected_favorite_id),
                    getResources().getInteger(R.integer.default_currently_selected_favorite_id));
            editor.putBoolean(getString(R.string.key_fetch_new_movies),
                    getResources().getBoolean(R.bool.default_fetch_new_movies));
            editor.commit();
        }
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



//
//
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.i(LOGTAG, "entered onCreateLoader");
//
//
//        return new CursorLoader(
//                this,
//
//
//        )
//
//
//    }
//
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        Log.i(LOGTAG, "entered onLoadFinished");
//
//
//
//        // TODO: this is where I need to figure out if the api call to fetch genres and certs was successful
//        // and then decide which Intent to launch next: either to HomeActivity or
//        // FavoritesActivity, although that too will depend on whether user has any favorites
//        // the msg display if the api call
//
//        // so I'm not actually using the cursor in this Activity, the loader is just a nice way
//        // to perform the api syncs for genres and certs tables on a background thread, and it
//        // since it's a loader, there is no need to worry about Activity lifecycle changes, like
//        // if the user rotated their device mid-load, which is why it's better than using a an
//        // async task
//
//        // well I guess I'm going to use the cursor to check to see if
//
//
//
//    }
//
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        Log.i(LOGTAG, "entered onLoaderReset");
//    }

}
