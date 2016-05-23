package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.nate.moviebot5k.api_fetching.MoviesFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.nate.moviebot5k.adapters.MoviePosterAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/9/2016.
 */
public class FragmentMovieGrid extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovGridFragment";

    private static final String BUNDLE_USE_FAVORITES_TABLE_KEY = "use_favorites";
    private static final int MOVIES_LOADER_ID = R.id.loader_movies_table_fragment_movie_grid;

    private Callbacks mCallbacks; // hosting activity will define what the method(s) inside Callback interface should do
    private boolean mUseFavorites; // true if db favorites table should be used in this fragment
    private MoviePosterAdapter mMoviePosterAdapter;
    private SharedPreferences mSharedPrefs;
    private ArrayList<Integer> mMovieIds;

    @Bind(R.id.fragment_movie_grid_gridview) GridView mMoviePosterGridView;


    /**
     * Call from a hosting Activity to get a new fragment for a fragment transaction.  The fragment
     * will display a list of movie posters in grid form: 2 columns in portrait, 3 in landscape.
     * Independence: it's not just an awesome US holiday.
     *
     * @param useFavoritesTable set to true if host needs to display movies using data from the
     *                          favorites table, which can be used with no internet connection
     * @return new FragmentMovieGrid <code>fragment</code>
     */
    public static FragmentMovieGrid newInstance(boolean useFavoritesTable) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, useFavoritesTable);
        FragmentMovieGrid fragment = new FragmentMovieGrid();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required interface for any activity that hosts this fragment
     */
    public interface Callbacks {

        /**
         * Hosting Activity should determine what happens when a movie is tapped from the movie grid.
         * The movieId provided will be the same in both the movies and favorites tables in the database.
         * @param movieId themoviedb ID of the movie that was just tapped by user from the grid view,
         *                which is how all movies and favorites are referenced in the database
         *
         */
        void onMovieSelected(int movieId);
    }

    @Override
    public void onAttach(Context context) {
        // associate the fragment's mCallbacks object with the activity it was just attached to
        mCallbacks = (Callbacks) getActivity();
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mCallbacks = null; // need to make sure this member variable is up to date with the correct activity
        // so nullify it every time this fragment gets detached from it's hosting activity
        super.onDetach();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOGTAG, "entered onSaveInstanceState");
        Log.i(LOGTAG, "  about to stash in Bundle mUseFavorites: " + mUseFavorites);
        outState.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, mUseFavorites);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(savedInstanceState == null) {
            Log.i(LOGTAG, "  and savedInstanceState is NULL, about to get useFavorites bool from frag argument");
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);

            if(!mUseFavorites && mSharedPrefs.getBoolean(getString(R.string.key_fetch_new_movies), true)) {
                Log.i(LOGTAG, "  and since !mUseFavorites AND S.P. fetch new movies is TRUE, about to fire a FetchMoviesTask");
                new FetchMoviesTask(getActivity(), this).execute();
            }
        }
        // must be some other reason the fragment is being recreated, likely an orientation change,
        // so get mUseFavorites table from the Bundle, which was stored prev. in onSaveInstanceState
        else {
            Log.i(LOGTAG, "  and savedInstanceState was NOT NULL, about to get useFavorites bool from SIS Bundle");
            mUseFavorites = savedInstanceState.getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");
        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        ButterKnife.bind(this, rootView);

        Log.i(LOGTAG, "  setting num poster grid columns to: " + getResources().getInteger(R.integer.gridview_view_num_columns));
        mMoviePosterGridView.setAdapter(mMoviePosterAdapter);

        // set a click listener on the adapter
        mMoviePosterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // get the movieId from the tag attached to the view that was just clicked
//                int movieId = (int) view.getTag(R.id.movie_poster_imageview_movie_id_key);
                int movieId = mMovieIds.get(position);


                // store the currently selected movieId in sharedPrefs
                // so when the user comes back to this app, the same movie will be on screen
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                if(mUseFavorites) {
                    editor.putInt(getString(R.string.key_currently_selected_favorite_id), movieId);
                } else {
                    editor.putInt(getString(R.string.key_currently_selected_movie_id), movieId);
                }
                editor.commit();

                Log.i(LOGTAG, "just clicked on movie with ID: " + movieId);

                // call back to the hosting Activity so it can do what it needs to do
                mCallbacks.onMovieSelected(movieId);
            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        Log.i(LOGTAG, "entered onResume");
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//
//        // check if a new fetch movies task should be launched
//        // technically mUseFavorites should never be true if sharedPrefs key_fetch_new_movies is true,
//        // but it doesn't hurt to check it as well here before starting a fetch movies async task
//        if(!mUseFavorites && sharedPrefs.getBoolean(getString(R.string.key_fetch_new_movies), true)) {
//            Log.i(LOGTAG, "  and sharedPrefs fetch_new_movies was true, so about to get more movies");
//
//            new FetchMoviesTask(getActivity()).execute();
//
//            // restart the Loader for the movies table, it doesn't matter if the async task
//            // does not return any movies, it won't update the movies table in that case and the
//            // loader manager can just do nothing until it's restarted again
//            getLoaderManager().restartLoader(MOVIES_TABLE_LOADER_ID, null, this);
//        }
        super.onResume();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onActivityCreated");

        getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }


    // the reason the favorites tables has more columns in it's projection is because when sorting
    // the favorites table, the sorting happens on the device, as opposed to the live movies table,
    // where sorting is done by themoviedb API
    private final String[] MOVIES_TABLE_COLUMNS_PROJECTION = {
            MovieTheaterContract.MoviesEntry._ID,
            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_POPULARITY,
            MovieTheaterContract.MoviesEntry.COLUMN_VOTE_AVG,
            MovieTheaterContract.MoviesEntry.COLUMN_REVENUE
    };
//    public static final int MOVIES_TABLE_COL_ID = 0;
    public static final int MOVIES_TABLE_COL_MOVIE_ID = 1;
    public static final int MOVIES_TABLE_COL_POSTER_PATH = 2;
    public static final int MOVIES_TABLE_COL_POSTER_FILE_PATH = 3;
    public static final int MOVIES_TABLE_COL_POPULARITY = 4;
    public static final int MOVIES_TABLE_COL_VOTE_AVG = 5;
    public static final int MOVIES_TABLE_COL_REVENUE = 6;


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOGTAG, "entered onCreateLoader");

        if(id == MOVIES_LOADER_ID) {
            Log.i(LOGTAG, "  and about to return new MOVIES_LOADER");

            return new CursorLoader(getActivity(),
                    MovieTheaterContract.MoviesEntry.CONTENT_URI, // the whole movies table
                    MOVIES_TABLE_COLUMNS_PROJECTION, // but only need these columns for this fragment
                    MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE + " = ? ", // select by is_favorites
                    new String[]{ String.valueOf(mUseFavorites) }, // select the data based on mUseFavorites
                    null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "entered onLoadFinished");

        // update the ArrayList that contains the movieIds this fragment is showing
        mMovieIds = Utility.getMovieIdList(getActivity());

        if(data == null) Log.e(LOGTAG, "  and data is NULL");
        else Log.e(LOGTAG, "  and data is NOT NULL");

        if(data.moveToFirst()) Log.i(LOGTAG, "    and data.moveToFist was successful");
        else Log.i(LOGTAG, "    and data.moveToFist was NOT successful");

        // swap the cursor so the adapter can load the new images
        mMoviePosterAdapter.swapCursor(data);

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOGTAG, "entered onLoaderReset");
        mMoviePosterAdapter.swapCursor(null);
    }


    private class FetchMoviesTask extends AsyncTask<Void, Void, Integer> {

        Context context;
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

        private FetchMoviesTask(Context c, LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks) {
            context = c;
            this.loaderCallbacks = loaderCallbacks;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(LOGTAG, "just entered FetchMoviesTask.doInBackground");

            return new MoviesFetcher(context).fetchMovies();
        }

        @Override
        protected void onPostExecute(Integer numMoviesFetched) {
            Log.i(LOGTAG,"  in FetchMoviesTask.onPostExecute, numMovies fetched was: " + numMoviesFetched);

            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, loaderCallbacks);


            // TODO: I think I was going to return num movies fetched here, and then display a msg if it was 0
        }
    }

}
