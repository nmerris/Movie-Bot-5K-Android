package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nate.moviebot5k.api_fetching.MovieDetailsFetcher;
import com.nate.moviebot5k.api_fetching.MoviesFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * Created by Nathan Merris on 5/16/2016.
 */
public class FragmentMovieDetails extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovDetlFrag";

    private static final String BUNDLE_USE_FAVORITES_TABLE_KEY = "use_favorites";
    private static final String BUNDLE_MOVIE_ID_KEY = "movie_id";
    private static final int FAVORITES_TABLE_LOADER_ID = R.id.loader_favorites_table_fragment_movie_details;
    private static final int MOVIES_TABLE_LOADER_ID = R.id.loader_movies_table_fragment_movie_details;
    private SharedPreferences mSharedPrefs;
    private boolean mUseFavorites; // true if db favorites table should be used in this fragment
//    private Callbacks mCallbacks; // hosting activity will define what the method(s) inside Callback interface should do
    private int mMovieId/*, mFavoriteId*/; // the id for the movie or favorite movie


    // movies table projection





    // the movieId will be used to read data from either the favorites or movies table
    public static FragmentMovieDetails newInstance(boolean useFavoritesTable, int movieId) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, useFavoritesTable);
        args.putInt(BUNDLE_MOVIE_ID_KEY, movieId);
        FragmentMovieDetails fragment = new FragmentMovieDetails();
        fragment.setArguments(args);
        return fragment;
    }



//    /**
//     * Required interface for any activity that hosts this fragment
//     */
//    public interface Callbacks {
//        // if hosted by HomeActivity: nothing happens, just toggle favorites icon (same phone and tablet),
//        //   when toggled 'on' add the record to favs table, when toggled 'off' remove same record
//        // if hosted by FavoritesAct: (only possible in tablet) just toggle the fav icon and remove
//        //   the record from fav table, or vice versa insert it, it's ok if the user just removed
//        //   the last favorite, the favorites grid will be updated the next time they come back to
//        //   to favorites activity because the db will not have any records, actually prob. just gray
//        //   out the menu option to even allow user to navigate to fav act if they don't have any favs
//        // if hosted by detail favorites pager act: (only possible in phone mode) just toggle the favorites icon,
//        //   there is no need to remove it from the viewpager.. remove or add the record to the fav
//        //   table each time it's toggled, basically same as if hosted by home activity, when user
//        //   navigates back to favorites activity, the loader will refresh the grid and the removed
//        //   favorite will just not be there, when user comes back to favorites pager act, the loader
//        //   will similarly just not see the removed fav record in the fav table
//        // if hosted by normal detail pager act: (only possible in phone mode) just toggle the fav icon,
//        //   and remove or add back in the record to the favorites table, basically same as above
//        /**
//         * Hosting Activity should determine what happens when a movie is removed from users favorites.
//         */
//        // NOTE TO SELF: I DON'T THINK I NEED THIS PARTICULAR CALLBACK, BUT I PROB. WILL NEED OTHERS
//        // FOR LAUNCHING
//        void onFavoriteRemoved(int movieId);
//    }


//    @Override
//    public void onAttach(Context context) {
//        // associate the fragment's mCallbacks object with the activity it was just attached to
//        mCallbacks = (Callbacks) getActivity();
//        super.onAttach(context);
//    }
//
//    @Override
//    public void onDetach() {
//        mCallbacks = null; // need to make sure this member variable is up to date with the correct activity
//        // so nullify it every time this fragment gets detached from it's hosting activity
//        super.onDetach();
//    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(BUNDLE_MOVIE_ID_KEY, mMovieId);
        outState.putBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY, mUseFavorites);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        mMovieId = mSharedPrefs
//                .getInt(getString(R.string.key_currently_selected_movie_id), 0);
//        mFavoriteId = mSharedPrefs
//                .getInt(getString(R.string.key_currently_selected_favorite_id), 0);



        // check if hosting activity has requested that this fragment use the favorites table
        if(savedInstanceState == null) {
            Log.i(LOGTAG, "  and savedInstanceState is NULL, about to get useFavorites bool and movieId int from frag argument");
            mMovieId = getArguments().getInt(BUNDLE_MOVIE_ID_KEY);
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
            Log.i(LOGTAG, "    mMovieId is now: " + mMovieId);
        }
        // must be some other reason the fragment is being recreated, likely an orientation change,
        // so get mUseFavorites table from the Bundle, which was stored prev. in onSaveInstanceState
        else {
            Log.i(LOGTAG, "  and savedInstanceState was NOT NULL, about to get useFavorites bool and movieId int from SIS Bundle");
            mMovieId = savedInstanceState.getInt(BUNDLE_MOVIE_ID_KEY);
            mUseFavorites = savedInstanceState.getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
            Log.i(LOGTAG, "    mMovieId is now: " + mMovieId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);


        TextView textTV =  (TextView) rootView.findViewById(R.id.test_movie_id_textview);

        // testing
        if(mUseFavorites) {
            // of course this would be read from favorites table in onLoadFinished
            textTV.setText(String.valueOf(mMovieId));



        } else {
            // and this would be read from movies table in onLoadFinished
            textTV.setText(String.valueOf(mMovieId));
        }



        return rootView;
    }


    @Override
    public void onResume() {
        Log.i(LOGTAG, "entered onResume");
        Log.e(LOGTAG, "  and mMovieId is: " + mMovieId);

        // check if the movies table already has the record with the EXTRA MOVIE DETAILS for the
        // movieId that this fragment
        // is showing, if it does then there is no need to make another API call because it must
        // have already been done, otherwise start a fetch details task so the db will be updated as needed
        // in addition to the reviews and videos that might be loaded in the fetch details task,
        // there are 3 other columns that are populated: budget, revenue, and runtime.
        // it's possible that an obscure movie will have no reviews or videos, so can't rely on those
        // columns to check... of the other three columns, it seems to me that runtime is prob. going
        // to be something that every movie in themoviedb's database will have data for, so I chose
        // to condition the following fetch task on that column
        // NOTE: if the favorites table is being used, an API fetch will never happen.. the only way
        // the user can get here and be using the favorites table is if they had previously selected
        // a movie as a favorite, which can only be done if the full record, including the extra
        // details that are loaded in this fragment, had already been loaded to the movies table
        if(!mUseFavorites) {



//            Cursor cursor = getActivity().getContentResolver().query(
//                    MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
//                    new String[] {MovieTheaterContract.MoviesEntry.COLUMN_RUNTIME},
//                    null, null, null);
//
//            if(cursor != null && cursor.moveToFirst()) {
//                Log.i(LOGTAG, "  just checked movies table, found movieId: " + mMovieId +
//                        ", has runtime column data: " + cursor.getInt(0));
//
//                // if runtime == 0 then this movie must not have had details fetched yet,
//                // so launch a new fetch details task, this will fill in all the columns with data
//                // that was not obtained during the fetch task that FragmentMovieGrid launched
//                // before the user clicked the poster thumbnail to get here
//                if(cursor.getInt(0) == 0) {
//                    new FetchMovieDetailsTask(getActivity(), mMovieId).execute();
//                }
//                cursor.close();
//            }

            new FetchMovieDetailsTask(getActivity(), mMovieId).execute();


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
            Log.i(LOGTAG, "  and about to initLoader FAVORITES_TABLE_LOADER, NOT IMPLEMENTED YET");
            getLoaderManager().initLoader(MOVIES_TABLE_LOADER_ID, null, this);
        }

        super.onActivityCreated(savedInstanceState);
    }




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }




    private class FetchMovieDetailsTask extends AsyncTask<Void, Void, Void> {
        Context context;
        int movieId;

        private FetchMovieDetailsTask(Context c, int movieId) {
            context = c;
            this.movieId = movieId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, "just entered FetchMoviesTask.doInBackground");
            return new MovieDetailsFetcher(context, mMovieId).fetchMovieDetails();
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.i(LOGTAG,"in FetchMovieDetailsTask.onPostExecute");
        }
    }


}
