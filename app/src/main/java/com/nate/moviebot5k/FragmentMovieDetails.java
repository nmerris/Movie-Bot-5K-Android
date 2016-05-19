package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nate.moviebot5k.api_fetching.MovieDetailsFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;

import butterknife.Bind;
import butterknife.ButterKnife;

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
    
    @Bind(R.id.backdrop_imageview) ImageView mBackdropImageView;
    @Bind(R.id.test_details_textview) TextView mDetailsTextView;
    @Bind(R.id.test_videos_textview) TextView mVideosTextView;
    @Bind(R.id.test_reviews_textview) TextView mReviewsTextView;
    @Bind(R.id.test_credits_profile_1) ImageView mCreditsProfile1;
    @Bind(R.id.test_credits_profile_2) ImageView mCreditsProfile2;
    @Bind(R.id.test_credits_profile_3) ImageView mCreditsProfile3;
    @Bind(R.id.test_credits_profile_4) ImageView mCreditsProfile4;
    @Bind(R.id.test_credits_textview_1) TextView mCreditsTextView1;
    @Bind(R.id.test_credits_textview_2) TextView mCreditsTextView2;
    @Bind(R.id.test_credits_textview_3) TextView mCreditsTextView3;
    @Bind(R.id.test_credits_textview_4) TextView mCreditsTextView4;
    


    // details projection, has columns from 4 diff db tables that have been joined
    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] DETAILS_PROJECTION = {
            MovieTheaterContract.MoviesEntry._ID,
            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID, // only needed for testing
            MovieTheaterContract.MoviesEntry.COLUMN_OVERVIEW,
            MovieTheaterContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieTheaterContract.MoviesEntry.COLUMN_GENRE_NAME1,
            MovieTheaterContract.MoviesEntry.COLUMN_GENRE_NAME2,
            MovieTheaterContract.MoviesEntry.COLUMN_GENRE_NAME3,
            MovieTheaterContract.MoviesEntry.COLUMN_GENRE_NAME4,
            MovieTheaterContract.MoviesEntry.COLUMN_TITLE,
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_VOTE_AVG,
            MovieTheaterContract.MoviesEntry.COLUMN_BUDGET,
            MovieTheaterContract.MoviesEntry.COLUMN_REVENUE,
            MovieTheaterContract.MoviesEntry.COLUMN_RUNTIME,
            MovieTheaterContract.MoviesEntry.COLUMN_TAGLINE,

            MovieTheaterContract.ReviewsEntry.COLUMN_AUTHOR,
            MovieTheaterContract.ReviewsEntry.COLUMN_CONTENT,

            MovieTheaterContract.CreditsEntry.COLUMN_CHARACTER,
            MovieTheaterContract.CreditsEntry.COLUMN_NAME,
            MovieTheaterContract.CreditsEntry.COLUMN_ORDER, // prob only need this for testing, should sortby this column on query
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH,

            MovieTheaterContract.VideosEntry.COLUMN_KEY,
            MovieTheaterContract.VideosEntry.COLUMN_SITE, // prob don't need this
            MovieTheaterContract.VideosEntry.COLUMN_TYPE,

            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH
    };
    private static final int COLUMN_MOVIE_ID = 1;
    private static final int COLUMN_OVERVIEW = 2;
    private static final int COLUMN_RELEASE_DATE = 3;
    private static final int COLUMN_GENRE_NAME1 = 4;
    private static final int COLUMN_GENRE_NAME2 = 5;
    private static final int COLUMN_GENRE_NAME3 = 6;
    private static final int COLUMN_GENRE_NAME4 = 7;
    private static final int COLUMN_MOVIE_TITLE = 8;
    private static final int COLUMN_BACKDROP_PATH = 9;
    private static final int COLUMN_VOTE_AVG = 10;
    private static final int COLUMN_BUDGET = 11;
    private static final int COLUMN_REVENUE = 12;
    private static final int COLUMN_RUNTIME = 13;
    private static final int COLUMN_TAGLINE = 14;
    private static final int COLUMN_REVIEW_AUTHOR = 15;
    private static final int COLUMN_REVIEW_CONTENT = 16;
    private static final int COLUMN_CHARACTER = 17;
    private static final int COLUMN_CAST_NAME = 18;
    private static final int COLUMN_CREDITS_ORDER = 19;
    private static final int COLUMN_PROFILE_PATH = 20;
    private static final int COLUMN_VIDEO_KEY = 21;
    private static final int COLUMN_VIDEO_SITE = 22;
    private static final int COLUMN_VIDEO_TYPE = 23;
    private static final int COLUMN_POSTER_FILE_PATH = 24;
    private static final int COLUMN_BACKDROP_FILE_PATH = 25;
    private static final int COLUMN_PROFILE_FILE_PATH = 26;





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


        // if this fragment is being created from new (ie it's hosting activity has performed a
        // fragment transaction), then get the movieId from the frag arg and then check to see if
        // a fetch details async task should be fired, if it does fire it will restart the loader
        // when it is done, which will be the second time that has happened because it would have
        // already happened on onActivityCreated when the loader is initialized.. seems acceptable
        if(savedInstanceState == null) {
            Log.i(LOGTAG, "  and savedInstanceState is NULL, about to get useFavorites bool and movieId int from frag argument");
            mMovieId = getArguments().getInt(BUNDLE_MOVIE_ID_KEY);
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_TABLE_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
            Log.i(LOGTAG, "    mMovieId is now: " + mMovieId);

            // if this fragment is null, might need to fetch movie details, will only happen if the db
            // does not have details data for current mMovieId
            fireFetchDetailsTaskIfNecessary();

        }
        // must be some other reason the fragment is being recreated, likely an orientation change,
        // so get mUseFavorites table from the Bundle, which was stored prev. in onSaveInstanceState
        // the loader will restart next when onCreateView is called, there is no need to check if a
        // fetch details task needs to happen because that would have already happened when this fragment
        // was initially created
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

        ButterKnife.bind(rootView);


//        TextView textTV =  (TextView) rootView.findViewById(R.id.test_movie_id_textview);
//        // testing
//        if(mUseFavorites) {
//            // of course this would be read from cursor  in onLoadFinished
//            textTV.setText(String.valueOf(mMovieId));
//        } else {
//            // and this would be read from cursor in onLoadFinished
//            textTV.setText(String.valueOf(mMovieId));
//        }



        return rootView;
    }


    @Override
    public void onResume() {
//        Log.i(LOGTAG, "entered onResume");
//        Log.e(LOGTAG, "  and mMovieId is: " + mMovieId);


        // TODO: I think this can be moved to onCreate, since this fragment is only ever created from scratch
        // when it's host does a frag txn
//        fireFetchDetailsTaskIfNecessary();


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
        Log.i(LOGTAG, "entered onCreateLoader");

        if(id == MOVIES_TABLE_LOADER_ID) {
            Log.i(LOGTAG, "  and about to return new MOVIES_TABLE_LOADER");

            return new CursorLoader(
                    getActivity(),
                    MovieTheaterContract.MoviesEntry.buildMovieDetailsUriFromMovieId(mMovieId),
                    null, // projection todo create this
                    null, // selection is ignored by content provider
                    null, // selectionArgs ignored by CP
                    null); // sort order
        }
        else if(id == FAVORITES_TABLE_LOADER_ID) {
            Log.i(LOGTAG, "  and about to return new FAVORITES_TABLE_LOADER");

            return new CursorLoader(
                    getActivity(),
                    MovieTheaterContract.FavoritesEntry.buildFavoriteDetailsUriFromMovieId(mMovieId),
                    null, // todo create this, like above but with extra columns for backdrop and profile pic file paths local
                    null, null, // ignored
                    null); // sort order
        }
        
        
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ entered onLoadFinished");


        if(data != null && data.moveToFirst()) {

            String author = data.getString(data.getColumnIndex(MovieTheaterContract.ReviewsEntry.COLUMN_AUTHOR));
            Log.e(LOGTAG, "  author column data: " + author);








        }
        
        
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}




    private class FetchMovieDetailsTask extends AsyncTask<Void, Void, Void> {
        Context context;
        int movieId;
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

        private FetchMovieDetailsTask(Context c, int movieId, LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks) {
            context = c;
            this.movieId = movieId;
            this.loaderCallbacks = loaderCallbacks;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, "just entered FetchMoviesTask.doInBackground");
            return new MovieDetailsFetcher(context, mMovieId).fetchMovieDetails();
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.i(LOGTAG,"in FetchMovieDetailsTask.onPostExecute, about to restart the Loader");

            // now that the db has been updated with new movie detail data,
            // restart the loader that lives in the class that contains this inner class
            if(mUseFavorites) {
                getLoaderManager().restartLoader(FAVORITES_TABLE_LOADER_ID, null, loaderCallbacks);
            } else {
                getLoaderManager().restartLoader(MOVIES_TABLE_LOADER_ID, null, loaderCallbacks);
            }
        }
    }


    // TODO: clean this mess up
    private void fireFetchDetailsTaskIfNecessary() {

        if(!mUseFavorites) {

            Cursor cursorCredits = getActivity().getContentResolver().query(
                    MovieTheaterContract.CreditsEntry.buildCreditsUriFromMovieId(mMovieId),
                    new String[] {MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID},
                    null, null, null);
            Log.i(LOGTAG, "  cursorCredits.getCount: " + cursorCredits.getCount());


            // the downside is that the following code will perform 3 db queries to check if a details
            // task should be fired, the upside is that it eliminates the possibility of duplicate
            // videos, credits, or reviews data ever being written to the db
            if(cursorCredits != null && cursorCredits.getCount() == 0) {
                Cursor cursorVideos = getActivity().getContentResolver().query(
                        MovieTheaterContract.VideosEntry.buildVideosUriFromMovieId(mMovieId),
                        new String[] {MovieTheaterContract.VideosEntry.COLUMN_MOVIE_ID},
                        null, null, null);
                Log.i(LOGTAG, "    cursorVideos.getCount: " + cursorVideos.getCount());

                if(cursorVideos != null && cursorVideos.getCount() == 0) {
                    Cursor cursorReviews = getActivity().getContentResolver().query(
                            MovieTheaterContract.ReviewsEntry.buildReviewsUriFromMovieId(mMovieId),
                            new String[] {MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID},
                            null, null, null);
                    Log.i(LOGTAG, "      cursorReviews.getCount: " + cursorReviews.getCount());

                    if(cursorReviews != null && cursorReviews.getCount() == 0) {
                        // since the count for the rows for this movieId was 0 for credits, videos, and reviews
                        // tables was zero, that must mean the detail data for this movieId has not
                        // yet been fetched, so go fetch it

                        Log.i(LOGTAG, "        about to fire a fetch details task because it appears that the details data for this movieId does not exist in the db");
                        new FetchMovieDetailsTask(getActivity(), mMovieId, this).execute();
                    }
                    cursorReviews.close();
                }
                cursorVideos.close();
            }
            cursorCredits.close();
        }
        
    }


}
