package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.Toast;

import com.nate.moviebot5k.api_fetching.MoviesFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.nate.moviebot5k.adapters.MoviePosterAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/9/2016.
 */
public class FragmentMovieGrid extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovGridFragment";

    private static final String BUNDLE_USE_FAVORITES_TABLE_KEY = "use_favorites";
    private static final String BUNDLE_TWO_PANE = "two_pane_mode";
    private static final String BUNDLE_MOVIE_ID_LIST = "movie_id_list";
    private static final int MOVIES_LOADER_ID = R.id.loader_movies_table_fragment_movie_grid;

    private Callbacks mCallbacks; // hosting activity will define what the method(s) inside Callback interface should do
    private boolean mUseFavorites; // true if db favorites table should be used in this fragment
    private boolean mTwoPane;
    private MoviePosterAdapter mMoviePosterAdapter;
    private SharedPreferences mSharedPrefs;
    private ArrayList<Integer> mMovieIds;

    @Bind(R.id.fragment_movie_grid_gridview) GridView mMoviePosterGridView;


    /**
     * Call from a hosting Activity to get a new fragment for a fragment transaction.  The fragment
     * will display a list of movie posters in grid form: 2 columns in portrait, 3 in landscape.
     * Independence: it's not just an awesome US holiday.
     *
     * @param useFavorites set to true if host needs to display movies using data from the
     *                          favorites table, which can be used with no internet connection
     * @return new FragmentMovieGrid <code>fragment</code>
     */
    public static FragmentMovieGrid newInstance(boolean useFavorites, boolean mTwoPane) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, useFavorites);
        args.putBoolean(BUNDLE_TWO_PANE, mTwoPane);
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
        void onMovieSelected(int movieId, ArrayList<Integer> moviesList);

        void onGridLoaded(ArrayList<Integer> moviesList);
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
//        Log.i(LOGTAG, "entered onSaveInstanceState");
//        Log.i(LOGTAG, "  about to stash in Bundle mUseFavorites: " + mUseFavorites);
        outState.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, mUseFavorites);
        outState.putIntegerArrayList(BUNDLE_MOVIE_ID_LIST, mMovieIds);

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mMovieIds = new ArrayList<>();

        if(savedInstanceState == null) {
            Log.i(LOGTAG, "  and savedInstanceState is NULL, about to get useFavorites bool from frag argument");
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            mTwoPane = getArguments().getBoolean(BUNDLE_TWO_PANE);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
        }
        // must be some other reason the fragment is being recreated, likely an orientation change,
        // so get mUseFavorites table from the Bundle, which was stored prev. in onSaveInstanceState
        else {
            Log.i(LOGTAG, "  and savedInstanceState was NOT NULL, about to get useFavorites bool AND mMovieIds List from SIS Bundle");
            mUseFavorites = savedInstanceState.getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            mMovieIds = savedInstanceState.getIntegerArrayList(BUNDLE_MOVIE_ID_LIST);
            mTwoPane = savedInstanceState.getBoolean(BUNDLE_TWO_PANE);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onActivityCreated");

        if(savedInstanceState == null && !mUseFavorites) {
            new FetchMoviesTask(getActivity(), this).execute();
        }
        else {
            getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.i(LOGTAG, "entered onCreateView");
        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), null, 0, mUseFavorites);
        View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        ButterKnife.bind(this, rootView);

        // there is no spinner fragment when this fragment is hosted by Favorites Activity, so there
        // is a lot more width to fill the screen in landscape orientation, and 4 columns looks much nicer
        // in phone landscape
        // in all other cases the num columns is defined in the xml for the gridlayout
        if(mUseFavorites && !mTwoPane &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mMoviePosterGridView.setNumColumns(4);
        }

//        Log.i(LOGTAG, "  setting num poster grid columns to: " + getResources().getInteger(R.integer.gridview_view_num_columns));
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
                mCallbacks.onMovieSelected(movieId, mMovieIds);
            }
        });


        // the 'no movies msg' is displayed either here, if no movies are in movieIds list, and
        // in FetchMoviesTask.onLoadFinished, if no movies were returned, need to swap view here
        // as well in case of orientation change, to show the correct view (movie grid or msg view)
        // but only want to show the msg when not showing favorites.. it's ok if the screen is just
        // empty if user navigates to favorites but doesn't actually have any
        if(savedInstanceState != null) {
            if (mMovieIds.size() == 0 && !mUseFavorites) { // no movies returned, for whatever reason
                // replace entire movie grid fragment view with a msg
                rootView.findViewById(R.id.problem_message_movie_grid).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.fragment_movie_grid_gridview).setVisibility(View.GONE);
            } else {
                rootView.findViewById(R.id.fragment_movie_grid_gridview).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.problem_message_movie_grid).setVisibility(View.GONE);
            }
        }


        return rootView;
    }


//    @Override
//    public void onResume() {
//        Log.i(LOGTAG, "entered onResume");
////        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
////
////        // check if a new fetch movies task should be launched
////        // technically mUseFavorites should never be true if sharedPrefs key_fetch_new_movies is true,
////        // but it doesn't hurt to check it as well here before starting a fetch movies async task
////        if(!mUseFavorites && sharedPrefs.getBoolean(getString(R.string.key_fetch_new_movies), true)) {
////            Log.i(LOGTAG, "  and sharedPrefs fetch_new_movies was true, so about to get more movies");
////
////            new FetchMoviesTask(getActivity()).execute();
////
////            // restart the Loader for the movies table, it doesn't matter if the async task
////            // does not return any movies, it won't update the movies table in that case and the
////            // loader manager can just do nothing until it's restarted again
////            getLoaderManager().restartLoader(MOVIES_TABLE_LOADER_ID, null, this);
////        }
//        super.onResume();
//    }





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
        Log.e(LOGTAG, "entered onCreateLoader");

        if(id == MOVIES_LOADER_ID) {

            if(mUseFavorites) {
                // get the sortby filter setting from sharedPrefs.. when this movie grid fragment is
                // being hosted by FavoritesActivity, the only filter params that make sense are
                // highest and lowest revenue, popularity, and rating.. could also have year in there,
                // but I need to stop somewhere, and that might be a bit confusing because then there
                // would be two simultaneous sortby options, so I would need to really consolidate it
                // into one spinner, and I just need to get this project done!!
                String sortBy = mSharedPrefs.getString(getString(R.string.key_favorites_sortby_value), null);
//                String[] sortByParts = sortBy.split("\\.");
//                Log.e(LOGTAG, "    key filter sortby after splitting: " + sortByParts[0] + " " + sortByParts[1]);

                // only load the movies that are marked as favorite in the movies table in db
                // and in this case just sort them by whatever the filter spinner is set to
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.MoviesEntry.CONTENT_URI, // the whole movies table
                        MOVIES_TABLE_COLUMNS_PROJECTION, // but only need these columns for this fragment
                        MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE + " = ?", // not my most proud java coding moment here, but it works
                        new String[]{ "true" }, // select only the rows that match the movieIds returned by movies fetcher
                        sortBy);
            }
            else {
                // load the movies with id's in mMovieIds, which are exactly the movies that
                // MoviesFetcher returned after it's api call, this may end up being a mix
                // of favorites and new movies, which is ok
                // note that the db does not allow duplicate entries with the same movie_id

                String movieIdSelection = "";
                String[] movieIdSelectionArgs = new String[mMovieIds.size()];
                int numMovieIds = mMovieIds.size();

                for(int i = 0; i < mMovieIds.size(); i++) {
                    movieIdSelectionArgs[i] = String.valueOf(mMovieIds.get(i));

                    // "movie_id = ? OR "
                    movieIdSelection += MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";
                    if(i == numMovieIds - 1) break;
                    movieIdSelection += " OR "; // do not want an OR at the end of this obnoxiously long selection statement
                }
//                Log.e(LOGTAG, "    movieIdSelection: " + movieIdSelection);


                return new CursorLoader(getActivity(),
                        MovieTheaterContract.MoviesEntry.CONTENT_URI, // the whole movies table
                        MOVIES_TABLE_COLUMNS_PROJECTION, // but only need these columns for this fragment
                        movieIdSelection, // not my most proud java coding moment here, but it works
                        movieIdSelectionArgs, // select only the rows that match the movieIds returned by movies fetcher
                        MovieTheaterContract.MoviesEntry.COLUMN_FETCH_ORDER + " ASC");
            }

        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "entered onLoadFinished");

        // if this fragment is being hosted by ActivityFavorites,
        // a fetch movies task will never fire.. so mMovieIds List will not be initialized
        // since that is normally done when the task completes, instead just need to create
        // the list from the cursor that was created when the cursor loader was started in onActivityCreated
        if(mUseFavorites) {
            Log.i(LOGTAG, "  and mUseFavorites is TRUE, so about to populate mMovieIds list from cursor that returned all the favorites");
            if(data != null && data.moveToFirst()) {
                while(!data.isAfterLast()) {
                    mMovieIds.add(data.getInt(MOVIES_TABLE_COL_MOVIE_ID));
                    Log.e(LOGTAG, "    just added to mMovieIds list: " + data.getInt(MOVIES_TABLE_COL_MOVIE_ID));
                    data.moveToNext();
                }
                Log.i(LOGTAG, "      num movieIds now in list: " + mMovieIds.size());
            }
        }


        // swap the cursor so the adapter can load the new images
        mMoviePosterAdapter.swapCursor(data);

//        // callback to hosting activity so that a new fragment can be loaded in tablet mode in the
//        // second pane (ie the details view)
//        mCallbacks.onGridLoaded(mMovieIds);

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOGTAG, "entered onLoaderReset");
        mMoviePosterAdapter.swapCursor(null);
    }


    private class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<Integer>> {
        Context context;
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

        private FetchMoviesTask(Context c, LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks) {
            context = c;
            this.loaderCallbacks = loaderCallbacks;
        }

        @Override
        protected ArrayList<Integer> doInBackground(Void... params) {
            return new MoviesFetcher(context).fetchMovies();
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> movieIdList) {
            Log.i(LOGTAG,"  in FetchMoviesTask.onPostExecute, about to restart loader if at least one movie fetched, size of ArrayList movieIdList is: " + movieIdList.size());
            
            // don't want anything to happen if this task returns and it's fragment or hosting
            // activity is dead, if rootView is not null, then we know that mMovieIds will not be null,
            // I don't think I need to check if getActivity() is null also, but I was getting some strange
            // detached messages at one point when calling initLoader, so it doesn't hurt to check
            View rootView = getView();
            if(getActivity() != null && rootView != null) {
                mMovieIds = movieIdList; // it's ok if there are zero movies in movieIdList

                if(movieIdList.size() == 0) { // no movies returned, for whatever reason
                    // replace entire movie grid fragment view with a msg
                    rootView.findViewById(R.id.problem_message_movie_grid).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.fragment_movie_grid_gridview).setVisibility(View.GONE);
                }
                else {
                    rootView.findViewById(R.id.fragment_movie_grid_gridview).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.problem_message_movie_grid).setVisibility(View.GONE);
//                    mMovieIds = movieIdList;
                    getLoaderManager().initLoader(MOVIES_LOADER_ID, null, loaderCallbacks);


                    // callback to hosting activity so that a new fragment can be loaded in tablet mode in the
                    // second pane (ie the details view)
                    mCallbacks.onGridLoaded(mMovieIds);


                }
            }
        }

    } // end FetchMoviesTask

}
