package com.nate.moviebot5k;

import android.annotation.SuppressLint;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nate.moviebot5k.api_fetching.MovieDetailsFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/16/2016.
 */
public class FragmentMovieDetails extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovDetlFrag";

    private static final String BUNDLE_USE_FAVORITES_KEY = "use_favorites";
    private static final String BUNDLE_MOVIE_ID_KEY = "movie_id";
    private Callbacks mCallbacks;


    private static final int FAVORITES_TABLE_LOADER_ID = R.id.loader_favorites_movies_fragment_movie_details;
    private static final int MOVIES_TABLE_LOADER_ID = R.id.loader_movies_fragment_movie_details;
    private static final int CREDITS_TABLE_LOADER_ID = R.id.loader_credits_fragment_movie_details;
    private static final int FAVORITES_CREDITS_TABLE_LOADER_ID = R.id.loader_favorites_credits_fragment_movie_details;
    private static final int VIDEOS_TABLE_LOADER_ID = R.id.loader_videos_fragment_movie_details;
    private static final int FAVORITES_VIDEOS_TABLE_LOADER_ID = R.id.loader_favorites_videos_fragment_movie_details;
    private static final int REVIEWS_TABLE_LOADER_ID = R.id.loader_reviews_fragment_movie_details;
    private static final int FAVORITES_REVIEWS_TABLE_LOADER_ID = R.id.loader_favorites_reviews_fragment_movie_details;


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
    
    
    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] MOVIES_PROJECTION = {
            MovieTheaterContract.MoviesEntry._ID,
            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID,
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
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH
    };
    private static final int COLUMN_MOVIE_ID = 1; // need only for testing
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
    private static final int COLUMN_POSTER_FILE_PATH = 15;
    private static final int COLUMN_BACKDROP_FILE_PATH = 16;
    
    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] REVIEWS_PROJECTION = {
            MovieTheaterContract.ReviewsEntry._ID,
            MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.ReviewsEntry.COLUMN_AUTHOR,
            MovieTheaterContract.ReviewsEntry.COLUMN_CONTENT
    };
    private static final int COLUMN_REVIEW_AUTHOR = 2;
    private static final int COLUMN_REVIEW_CONTENT = 3;
    
    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] CREDITS_PROJECTION = {
            MovieTheaterContract.CreditsEntry._ID,
//            MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.CreditsEntry.COLUMN_CHARACTER,
            MovieTheaterContract.CreditsEntry.COLUMN_NAME,
            MovieTheaterContract.CreditsEntry.COLUMN_ORDER,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH
    };
    private static final int COLUMN_CHARACTER = 2;
    private static final int COLUMN_CAST_NAME = 3;
//    private static final int COLUMN_ORDER = 4;
    private static final int COLUMN_PROFILE_PATH = 5;
    private static final int COLUMN_PROFILE__FILE_PATH = 6;

    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] VIDEOS_PROJECTION = {
            MovieTheaterContract.VideosEntry._ID,
            MovieTheaterContract.VideosEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.VideosEntry.COLUMN_KEY,
            MovieTheaterContract.VideosEntry.COLUMN_SITE,
            MovieTheaterContract.VideosEntry.COLUMN_TYPE
    };
    private static final int COLUMN_VIDEO_KEY = 2;
    private static final int COLUMN_VIDEO_SITE = 3;
    private static final int COLUMN_VIDEO_TYPE = 4;





    // the movieId will be used to read data from either the favorites or movies table
    public static FragmentMovieDetails newInstance(boolean useFavoritesTable, int movieId) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_KEY, useFavoritesTable);
        args.putInt(BUNDLE_MOVIE_ID_KEY, movieId);
        FragmentMovieDetails fragment = new FragmentMovieDetails();
        fragment.setArguments(args);
        return fragment;
    }



    /**
     * Required interface for any activity that hosts this fragment
     */
    public interface Callbacks {
        // if hosted by HomeActivity: nothing happens, just toggle favorites icon (same phone and tablet),
        //   when toggled 'on' add the record to favs table, when toggled 'off' remove same record
        // if hosted by FavoritesAct: (only possible in tablet) just toggle the fav icon and remove
        //   the record from fav table, or vice versa insert it, it's ok if the user just removed
        //   the last favorite, the favorites grid will be updated the next time they come back to
        //   to favorites activity because the db will not have any records, actually prob. just gray
        //   out the menu option to even allow user to navigate to fav act if they don't have any favs
        // if hosted by detail favorites pager act: (only possible in phone mode) just toggle the favorites icon,
        //   there is no need to remove it from the viewpager.. remove or add the record to the fav
        //   table each time it's toggled, basically same as if hosted by home activity, when user
        //   navigates back to favorites activity, the loader will refresh the grid and the removed
        //   favorite will just not be there, when user comes back to favorites pager act, the loader
        //   will similarly just not see the removed fav record in the fav table
        // if hosted by normal detail pager act: (only possible in phone mode) just toggle the fav icon,
        //   and remove or add back in the record to the favorites table, basically same as above
        /**
         * Hosting Activity should determine what happens when a movie is removed from users favorites.
         */
        // NOTE TO SELF: I DON'T THINK I NEED THIS PARTICULAR CALLBACK, BUT I PROB. WILL NEED OTHERS
        // FOR LAUNCHING
//        void onFavoriteRemoved(int movieId);
        void onUpdateToolbar(String movieTitle, String movieTagline);
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

        outState.putInt(BUNDLE_MOVIE_ID_KEY, mMovieId);
        outState.putBoolean(BUNDLE_USE_FAVORITES_KEY, mUseFavorites);

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
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_KEY);
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
            mUseFavorites = savedInstanceState.getBoolean(BUNDLE_USE_FAVORITES_KEY);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
            Log.i(LOGTAG, "    mMovieId is now: " + mMovieId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

//        ButterKnife.bind(rootView);


//        TextView textTV =  (TextView) rootView.findViewById(R.id.test_movie_id_textview);
//        // testing
//        if(mUseFavorites) {
//            // of course this would be read from cursor  in onLoadFinished
//            textTV.setText(String.valueOf(mMovieId));
//        } else {
//            // and this would be read from cursor in onLoadFinished
//            textTV.setText(String.valueOf(mMovieId));
//        }



        if(mUseFavorites) {
            Log.i(LOGTAG, "  and about to init all FAVORITES loaders");
//            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
//            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
//            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
//            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
        }
        else {
            Log.i(LOGTAG, "  and about to init all NON-favorites loaders");
            getLoaderManager().initLoader(MOVIES_TABLE_LOADER_ID, null, this);
            getLoaderManager().initLoader(CREDITS_TABLE_LOADER_ID, null, this);
            getLoaderManager().initLoader(VIDEOS_TABLE_LOADER_ID, null, this);
            getLoaderManager().initLoader(REVIEWS_TABLE_LOADER_ID, null, this);
        }



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
//        if(mUseFavorites) {
//            Log.i(LOGTAG, "  and about to init all FAVORITES loaders");
////            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
////            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
////            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
////            getLoaderManager().initLoader(FAVORITES_TABLE_LOADER_ID, null, this);
//        }
//        else {
//            Log.i(LOGTAG, "  and about to init all NON-favorites loaders");
//            getLoaderManager().initLoader(MOVIES_TABLE_LOADER_ID, null, this);
//            getLoaderManager().initLoader(CREDITS_TABLE_LOADER_ID, null, this);
//            getLoaderManager().initLoader(VIDEOS_TABLE_LOADER_ID, null, this);
//            getLoaderManager().initLoader(REVIEWS_TABLE_LOADER_ID, null, this);
//        }

        super.onActivityCreated(savedInstanceState);
    }




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOGTAG, "entered onCreateLoader");



        switch (id) {
            case MOVIES_TABLE_LOADER_ID:
                Log.i(LOGTAG, "  and about to return new MOVIES_TABLE_LOADER");
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                        MOVIES_PROJECTION, null, null, null);

            case FAVORITES_TABLE_LOADER_ID:
                Log.i(LOGTAG, "  and about to return new FAVORITES_TABLE_LOADER");
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.FavoritesEntry.buildFavoriteUriFromMovieId(mMovieId),
                        MOVIES_PROJECTION, null, null, null);

            case VIDEOS_TABLE_LOADER_ID:
                Log.i(LOGTAG, "  and about to return new VIDEOS_TABLE_LOADER_ID");
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.VideosEntry.buildVideosUriFromMovieId(mMovieId),
                        VIDEOS_PROJECTION, null, null, null);

            case FAVORITES_VIDEOS_TABLE_LOADER_ID:
                Log.i(LOGTAG, "  and about to return new FAVORITES_VIDEOS_TABLE_LOADER_ID");
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.FavoritesVideosEntry.buildFavoritesVideosUriFromMovieId(mMovieId),
                        VIDEOS_PROJECTION, null, null, null);





        }

        return null;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ entered onLoadFinished");
        View rootView = getView();

        if(rootView != null && data.moveToFirst()) {
            ButterKnife.bind(this, rootView);

            switch (loader.getId()) {
                case MOVIES_TABLE_LOADER_ID:
//                    Log.e(LOGTAG, "  from movies table loader, tagline: " + data.getString(COLUMN_TAGLINE));

//                    if(mBackdropImageView == null) Log.e(LOGTAG, "MBACKDROPIMAGE VIEW IS NULL!!");

                    // backdrop image
                    Picasso.with(getActivity())
                            .load(data.getString(COLUMN_BACKDROP_PATH))
                            .into(mBackdropImageView);

                    // other misc movie detail data
                    String genreName1 = data.getString(COLUMN_GENRE_NAME1);
                    String genreName2 = data.getString(COLUMN_GENRE_NAME2);
                    String genreName3 = data.getString(COLUMN_GENRE_NAME3);
                    String genreName4 = data.getString(COLUMN_GENRE_NAME4);
                    String overview = data.getString(COLUMN_OVERVIEW);
                    String releaseDate = data.getString(COLUMN_RELEASE_DATE);
                    String title = data.getString(COLUMN_MOVIE_TITLE);
                    float voteAvg = data.getFloat(COLUMN_VOTE_AVG);
                    long budget = data.getLong(COLUMN_BUDGET);
                    long revenue = data.getLong(COLUMN_REVENUE);
                    int runtime = data.getInt(COLUMN_RUNTIME);
                    String tagline = data.getString(COLUMN_TAGLINE);

                    // callback to hosting activity so the toolbar title can be updated as needed
                    mCallbacks.onUpdateToolbar(title, tagline);


//
//                    Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
//                    toolbar.setTitle(title);
//                    toolbar.setSubtitle(tagline);
//
//                    ((AppCompatActivity) getActivity()).getSupportActionBar()


//                    getActivity().getActionBar().setTitle(title);
//                    getActivity().getActionBar().setSubtitle(tagline);



                    mDetailsTextView.setText("DETAILS FROM MOVIE TABLE: \n" +
                            genreName1 + " " + genreName2 + " " + genreName3 + " " + genreName4 + "\n" +
                            overview + "\n" +
                            "Release Date: " + releaseDate + "\n" +
                            "Vote Avg: " + voteAvg + "\n" +
                            "Budget: " + budget + "\n" +
                            "Runtime: " + runtime + "\n");




                    break;
                case FAVORITES_TABLE_LOADER_ID:



                    break;
                case VIDEOS_TABLE_LOADER_ID:

                    Log.e(LOGTAG, "  from videos table loader, key: " + data.getString(COLUMN_VIDEO_KEY));








                    break;
                case FAVORITES_VIDEOS_TABLE_LOADER_ID:


                    break;
            }
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
                getLoaderManager().restartLoader(FAVORITES_VIDEOS_TABLE_LOADER_ID, null, loaderCallbacks);
                getLoaderManager().restartLoader(FAVORITES_CREDITS_TABLE_LOADER_ID, null, loaderCallbacks);
                getLoaderManager().restartLoader(FAVORITES_REVIEWS_TABLE_LOADER_ID, null, loaderCallbacks);

            } else {
                getLoaderManager().restartLoader(MOVIES_TABLE_LOADER_ID, null, loaderCallbacks);
//                getLoaderManager().restartLoader(CREDITS_TABLE_LOADER_ID, null, loaderCallbacks);
//                getLoaderManager().restartLoader(REVIEWS_TABLE_LOADER_ID, null, loaderCallbacks);
                getLoaderManager().restartLoader(VIDEOS_TABLE_LOADER_ID, null, loaderCallbacks);
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
