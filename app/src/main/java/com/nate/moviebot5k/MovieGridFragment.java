package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DimenRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * Created by Nathan Merris on 5/9/2016.
 */
public class MovieGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovGridFragment";

    private static final String BUNDLE_USE_FAVORITES_TABLE_KEY = "use_favorites";
    private static final int MOVIES_TABLE_LOADER_ID = 1;
    private static final int FAVORITES_TABLE_LOADER_ID =2;

    private Callbacks mCallbacks; // hosting activity will define what the method(s) inside Callback interface should do
    private boolean mUseFavorites; // true if db favorites table should be used in this fragment

    public MovieGridFragment() {}

    /**
     * Call from a hosting Activity to get a new fragment for a fragment transaction.  The fragment
     * will display a list of movie posters in grid form: 2 columns in portrait, 3 in landscape.
     * Independence: it's not just an awesome US holiday.
     *
     * @param useFavoritesTable set to true if host needs to display movies using data from the
     *                          favorites table, which can be used with no internet connection
     * @return new MovieGridFragment <code>fragment</code>
     */
    public static MovieGridFragment newInstance(boolean useFavoritesTable) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, useFavoritesTable);
        MovieGridFragment fragment = new MovieGridFragment();
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

        if(savedInstanceState == null) {
            Log.i(LOGTAG, "  and savedInstanceState is NULL, about to get useFavorites bool from frag argument");
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
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




        return null;

    }


    @Override
    public void onResume() {
        Log.i(LOGTAG, "entered onResume");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // check if a new fetch movies task should be launched
        if(sharedPrefs.getBoolean(getString(R.string.key_fetch_new_movies), true)) {
            Log.i(LOGTAG, "  and sharedPrefs fetch_new_movies was true, so about to get more movies");
            // TODO: restart Loader?
        }
        super.onResume();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onActivityCreated");

        // start the appropriate Loader depending on which Activity is hosting this fragment
        if(mUseFavorites) {
            Log.i(LOGTAG, "  and about to initLoader FAVORITES_TABLE_LOADER");
            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
        }
        else {
            Log.i(LOGTAG, "  and about to initLoader MOVIES_TABLE_LOADER");
            getLoaderManager().initLoader(MOVIES_TABLE_LOADER_ID, null, this);
        }

        super.onActivityCreated(savedInstanceState);
    }



    // define a projection for this fragment's Loaders, only want to query what we need for
    // the movie grid views.  NOTE: this is not going to restrict what actual data is fetched from
    // themoviedb during the API call, that will grab all the data it needs, the point here to just
    // grab the data we need to make MovieGridFragment have what it needs to do it's thing
    private final String[] MOVIES_TABLE_COLUMNS_PROJECTION = {
            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_PATH
    };
    // these columns variables match the order of the projection above, if you change one you must
    // also change the other
    static final int MOVIES_TABLE_COL_MOVIE_ID = 0;
    static final int MOVIES_TABLE_COL_POSTER_PATH = 1;


    // the reason the favorites tables has more columns in it's projection is because when sorting
    // the favorites table, the sorting happens on the device, as opposed to the live movies table,
    // where sorting is done by themoviedb API
    private final String[] FAVORITES_TABLE_COLUMNS_PROJECTION = {
            MovieTheaterContract.FavoritesEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.FavoritesEntry.COLUMN_POSTER_FILE_PATH,
            MovieTheaterContract.FavoritesEntry.COLUMN_POPULARITY,
            MovieTheaterContract.FavoritesEntry.COLUMN_VOTE_AVG,
            MovieTheaterContract.FavoritesEntry.COLUMN_REVENUE
    };
    static final int FAVORITES_TABLE_COL_MOVIE_ID = 0;
    static final int FAVORITES_TABLE_COL_POSTER_FILE_PATH = 1;
    static final int FAVORITES_TABLE_COL_POPULARITY = 2;
    static final int FAVORITES_TABLE_COL_VOTE_AVG = 3;
    static final int FAVORITES_TABLE_COL_REVENUE = 4;


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOGTAG, "entered onCreateLoader");

        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "entered onLoadFinished");

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOGTAG, "entered onLoaderReset");

    }




    /**
     * Make the movie posters look nice, with even padding all around.
     *
     * @author edwardaa on Stackoverflow
     * @see <a>http://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing</a>
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, @DimenRes int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

}
