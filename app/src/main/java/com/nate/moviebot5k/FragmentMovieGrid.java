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

import com.nate.moviebot5k.api_fetching.MoviesFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.nate.moviebot5k.adapters.MoviePosterAdapter;

import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Displays a vertically scrolling grid of movie thumbnails.  The URI of the thumbnails come from the
 * movies table.  The images for the movie poster thumbnails are read from local device storage if
 * the hosting activity is showing favorites, or Picasso is used to load them from themoviedb
 * servers otherwise. Hosting activities should use the newInstance method to create this fragment.
 * <br><br>
 * When the user clicks a thumbnail, onMovieSelected callback fires, passing
 * the movieId of the movie that was just clicked.
 * <br><br>
 * When the movie grid itself is finished loading,
 * the onGridLoaded callback fires.
 *
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
    private boolean mTwoPane; // true if app is running in tablet mode
    private MoviePosterAdapter mMoviePosterAdapter;
    private SharedPreferences mSharedPrefs;
    private ArrayList<Integer> mMovieIds; // the list of all movieIds currently in the gridview

    @Bind(R.id.fragment_movie_grid_gridview) GridView mMoviePosterGridView;


    /**
     * Call from a hosting Activity to get a new fragment for a fragment transaction.  The fragment
     * will display a list of movie posters in grid form.
     * Independence: it's not just an awesome US holiday.
     *
     * @param useFavorites set to true if host needs to display movies that are marked as favorites
     *                     in the db, movies and their images will be displayed even with no internet connection
     * @param mTwoPane set to true if the app is running in tablet mode
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
         * @param movieId themoviedb ID of the movie that was just tapped by user from the grid view,
         *                which is how all movies and favorites are referenced in the database
         */
        void onMovieSelected(int movieId, ArrayList<Integer> moviesList);

        /**
         * Called when FragmentMovieGrid has finished loading all the movies in the gridview.
         * @param moviesList the list of movies just loaded, in the same order they are in the grid
         */
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
        outState.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, mUseFavorites);
        outState.putIntegerArrayList(BUNDLE_MOVIE_ID_LIST, mMovieIds);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mMovieIds = new ArrayList<>();

        // get the fragment arguments if this fragment is being created from scratch
        if(savedInstanceState == null) {
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            mTwoPane = getArguments().getBoolean(BUNDLE_TWO_PANE);
        }
        // must be some other reason the fragment is being recreated, likely an orientation change,
        // so get member variables from the Bundle, which were stored prev. in onSaveInstanceState
        else {
            mUseFavorites = savedInstanceState.getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            mMovieIds = savedInstanceState.getIntegerArrayList(BUNDLE_MOVIE_ID_LIST);
            mTwoPane = savedInstanceState.getBoolean(BUNDLE_TWO_PANE);
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(savedInstanceState == null && !mUseFavorites) {
            // since fragment is being created from scratch and hosting activity is not requesting
            // favorites be used, fire an async task to fetch a new batch of movie data.. this will
            // update the movies table in the db, and restart the loader when it's done
            new FetchMoviesTask(getActivity(), this).execute();
        }
        else {
            // otherwise just start the loader again
            getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        mMoviePosterGridView.setAdapter(mMoviePosterAdapter);

        // set a click listener on the adapter
        mMoviePosterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get the movieId from the position that was just clicked.. the position of the click
                // matches the index of the array list for a given movie
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


    private final String[] MOVIES_TABLE_COLUMNS_PROJECTION = {
            MovieTheaterContract.MoviesEntry._ID,
            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH
    };
    public static final int MOVIES_TABLE_COL_MOVIE_ID = 1;
    public static final int MOVIES_TABLE_COL_POSTER_PATH = 2;
    public static final int MOVIES_TABLE_COL_POSTER_FILE_PATH = 3;


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(id == MOVIES_LOADER_ID) {

            if(mUseFavorites) {
                // get the sortby filter setting from sharedPrefs, the sortby setting for favorites
                // is different than the sortby setting for 'normal' mode where api calls are made,
                // and the sorting is actually done by themoviedb's servers, but here the sorting
                // is done locally in this apps db
                String sortBy = mSharedPrefs.getString(getString(R.string.key_favorites_sortby_value), null);

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
                // load the movies in mMovieIds, which are exactly the movies that
                // MoviesFetcher returned after it's api call, this may end up being a MIX
                // of favorites and new movies, which is ok
                // note that the db does not allow duplicate entries with the same movie_id
                String movieIdSelection = "";
                String[] movieIdSelectionArgs = new String[mMovieIds.size()];
                int numMovieIds = mMovieIds.size();

                // create an obnoxiously long selection statement to select the correct movieIds,
                // its ugly printed out, but not too offensive here in a loop
                for(int i = 0; i < mMovieIds.size(); i++) {
                    movieIdSelectionArgs[i] = String.valueOf(mMovieIds.get(i));

                    // "movie_id = ? OR "
                    movieIdSelection += MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";
                    if(i == numMovieIds - 1) break;
                    movieIdSelection += " OR "; // do not want an OR at the end of this obnoxiously long selection statement
                }
                // and return a cursor that points to only the movies we want
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
        // if this fragment is being hosted by ActivityFavorites,
        // a fetch movies task will never fire.. so mMovieIds List will not be initialized
        // since that is normally done when the task completes, instead just need to create
        // the list from the cursor that was created when the cursor loader was started in onActivityCreated
        if(mUseFavorites) {
            if(data != null && data.moveToFirst()) {
                while(!data.isAfterLast()) {
                    mMovieIds.add(data.getInt(MOVIES_TABLE_COL_MOVIE_ID));
                    data.moveToNext();
                }
            }
        }

        // swap the cursor so the adapter can load the new images
        mMoviePosterAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviePosterAdapter.swapCursor(null);
    }


    /**
     * Queries themoviedb's servers to get movie data.  The query parameters are read in from
     * sharePrefs and are used in MoviesFetcher, which this task launches in a background thread.
     * The results are stored in the movies table in this apps db.  When this task completes, if at
     * least one movie was fetched then it is displayed in the movie grid, otherwise an error
     * message is displayed.  Finally, if at least one movie was fetched,
     * FragmentMovieGrid.onGridLoaded callback fires, and the new list of movieIds is passed along.
     *
     * @see MoviesFetcher
     */
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
                    getLoaderManager().initLoader(MOVIES_LOADER_ID, null, loaderCallbacks);

                    // callback to hosting activity so that a new fragment can be loaded in tablet mode in the
                    // second pane (ie the details view)
                    mCallbacks.onGridLoaded(mMovieIds);

                }
            }
        }

    } // end FetchMoviesTask

}
