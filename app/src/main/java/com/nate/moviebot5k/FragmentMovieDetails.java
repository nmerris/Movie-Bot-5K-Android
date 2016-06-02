package com.nate.moviebot5k;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nate.moviebot5k.api_fetching.MovieDetailsFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
    private static final String BUNDLE_MTWO_PANE = "mtwopane_mode";

    private Callbacks mCallbacks;

    private static final int MOVIES_LOADER_ID = R.id.loader_movies_fragment_movie_details;
    private static final int CREDITS_LOADER_ID = R.id.loader_credits_fragment_movie_details;
    private static final int VIDEOS_LOADER_ID = R.id.loader_videos_fragment_movie_details;
    private static final int REVIEWS_LOADER_ID = R.id.loader_reviews_fragment_movie_details;

    private SharedPreferences mSharedPrefs;
    private boolean mUseFavorites; // true if db favorites table should be used in this fragment
    private int mMovieId; // the id for the movie or favorite movie
    private boolean mTwoPane;
    private List<Long> mMovieIdList; // the list of movies to display

    @Bind(R.id.fab_favorites) FloatingActionButton mFabFavorites;
    @Bind(R.id.backdrop_imageview) ImageView mBackdropImageView;
    @Bind(R.id.movie_title_textview) TextView mMovieTitleTV;
    @Bind(R.id.movie_tagline_textview) TextView mMovieTaglineTV;
    @Bind(R.id.movie_genre_textview) TextView mMovieGenresTV;
    @Bind(R.id.movie_release_date_textview) TextView mReleaseDateTV;
    @Bind(R.id.movie_vote_avg_textview) TextView mVoteAvgTV;
    @Bind(R.id.movie_num_votes_textview) TextView mNumVotesTV;
    @Bind(R.id.movie_budget_textview) TextView mBudgetTV;
    @Bind(R.id.movie_revenue_textview) TextView mRevenueTV;
    @Bind(R.id.movie_runtime_textview) TextView mRuntimeTV;
    @Bind(R.id.movie_overview_textview) TextView mOverviewTV;
    @Bind(R.id.credits_profile_1_cast_name_textview) TextView mCastName1TV;
    @Bind(R.id.credits_profile_1_character_name_textview) TextView mCharacterName1TV;
    @Bind(R.id.credits_profile_1_imageview) ImageView mProfile1ImageView;
    
    @Bind(R.id.credits_profile_2_cast_name_textview) TextView mCastName2TV;
    @Bind(R.id.credits_profile_2_character_name_textview) TextView mCharacterName2TV;
    @Bind(R.id.credits_profile_2_imageview) ImageView mProfile2ImageView;
    
    @Bind(R.id.credits_profile_3_cast_name_textview) TextView mCastName3TV;
    @Bind(R.id.credits_profile_3_character_name_textview) TextView mCharacterName3TV;
    @Bind(R.id.credits_profile_3_imageview) ImageView mProfile3ImageView;
    
    @Bind(R.id.credits_profile_4_cast_name_textview) TextView mCastName4TV;
    @Bind(R.id.credits_profile_4_character_name_textview) TextView mCharacterName4TV;
    @Bind(R.id.credits_profile_4_imageview) ImageView mProfile4ImageView;

    @Bind(R.id.video_thumbnail1_imageview) ImageView mVideoThumbnailImageView1;
    @Bind(R.id.video_thumbnail2_imageview) ImageView mVideoThumbnailImageView2;
    @Bind(R.id.video1_share_button) ImageView mVideoShareButton1ImageView;
    @Bind(R.id.video2_share_button) ImageView mVideoShareButton2ImageView;
    @Bind(R.id.video_section_title) TextView mVideoSectionTitle;


//    @Bind(R.id.video_layout1) LinearLayout mVideoLayout1;
//    @Bind(R.id.video_layout2) LinearLayout mVideoLayout2;



/*    @Bind(R.id.test_videos1_textview) TextView mVideosTextView1;
    @Bind(R.id.test_videos2_textview) TextView mVideosTextView2;
//    @Bind(R.id.test_videos3_textview) TextView mVideosTextView3;
//    @Bind(R.id.test_videos4_textview) TextView mVideosTextView4;
    @Bind(R.id.video_thumbnail1_imageview) ImageView mVideoThumbnailImageView1;
    @Bind(R.id.video_thumbnail2_imageview) ImageView mVideoThumbnailImageView2;

    @Bind(R.id.test_reviews_textview) TextView mReviewsTextView;

    @Bind(R.id.test_credits_profile_1) ImageView mCreditsProfile1;
    @Bind(R.id.test_credits_profile_2) ImageView mCreditsProfile2;
    @Bind(R.id.test_credits_profile_3) ImageView mCreditsProfile3;
    @Bind(R.id.test_credits_profile_4) ImageView mCreditsProfile4;
    @Bind(R.id.test_credits_textview_1) TextView mCreditsTextView1;
    @Bind(R.id.test_credits_textview_2) TextView mCreditsTextView2;
    @Bind(R.id.test_credits_textview_3) TextView mCreditsTextView3;
    @Bind(R.id.test_credits_textview_4) TextView mCreditsTextView4;*/
    
    
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
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE,
            MovieTheaterContract.MoviesEntry.COLUMN_VOTE_COUNT
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
    private static final int COLUMN_IS_FAVORITE = 17;
    private static final int COLUMN_NUM_VOTES = 18;
    
    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] REVIEWS_PROJECTION = {
            MovieTheaterContract.ReviewsEntry._ID,
            MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.ReviewsEntry.COLUMN_AUTHOR,
            MovieTheaterContract.ReviewsEntry.COLUMN_CONTENT,
    };
    private static final int COLUMN_REVIEW_AUTHOR = 2;
    private static final int COLUMN_REVIEW_CONTENT = 3;

    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] CREDITS_PROJECTION = {
            MovieTheaterContract.CreditsEntry._ID,
            MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.CreditsEntry.COLUMN_CHARACTER,
            MovieTheaterContract.CreditsEntry.COLUMN_NAME,
            MovieTheaterContract.CreditsEntry.COLUMN_ORDER,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH,
    };
    private static final int COLUMN_CHARACTER = 2;
    private static final int COLUMN_CAST_NAME = 3;
    private static final int COLUMN_PROFILE_PATH = 5;
    private static final int COLUMN_PROFILE_FILE_PATH = 6;

    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] VIDEOS_PROJECTION = {
            MovieTheaterContract.VideosEntry._ID,
            MovieTheaterContract.VideosEntry.COLUMN_MOVIE_ID,
            MovieTheaterContract.VideosEntry.COLUMN_KEY,
            MovieTheaterContract.VideosEntry.COLUMN_SITE,
            MovieTheaterContract.VideosEntry.COLUMN_TYPE,
            MovieTheaterContract.VideosEntry.COLUMN_THUMBNAIL_URL,
            MovieTheaterContract.VideosEntry.COLUMN_NAME,
    };
    private static final int COLUMN_VIDEO_KEY = 2;
    private static final int COLUMN_VIDEO_SITE = 3;
    private static final int COLUMN_VIDEO_TYPE = 4;
    private static final int COLUMN_VIDEO_THUMBNAIL_URL = 5;
    private static final int COLUMN_VIDEO_NAME = 6;





    // the movieId will be used to read data from either the favorites or movies table
    public static FragmentMovieDetails newInstance(boolean useFavorites, int movieId, boolean mTwoPane) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_KEY, useFavorites);
        args.putBoolean(BUNDLE_MTWO_PANE, mTwoPane);
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
        outState.putBoolean(BUNDLE_MTWO_PANE, mTwoPane);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOGTAG, "******** JUST ENTERED ONACTIVITYCREATED ******");


        if(savedInstanceState == null && !mUseFavorites) {
            Log.e(LOGTAG, "  and since SIS was null, about to MAYBE fire an async task, depends on if this movieId already has detail data in the db");

            // returns true only if vids, credits, and reviews tables have no entries with mMovieId
            // if it does find all those tables empty for mMovieId, it will fire a fetch details task
            // that will insert data for those tables, in addition to the extra bits of movie detail
            // data get put in the movies table
            boolean updatedVidsReviewsCredits = updateVidsReviewsCreditsIfNecessary();

            // since the fetch task returned false, that means the db already has data in it for mMovieId
            // for the videos, credits, and reviews tables, but we still need to update the extra few columns
            // of data in the movies table, since that may have been erased the last time the movie grid fragment
            // wiped out the non favorites movies table data, so in this case in order to avoid having duplicate
            // entries (since the movie_id column in not UNIQUE in the videos, credits, or reviews tables), need
            // to still fire a fetch details task, but pass in false to tell it to ignore vids, credits,
            // and reviews data/tables
            if(!updatedVidsReviewsCredits) {
                new FetchMovieDetailsTask(getActivity(), mMovieId, false, this).execute();
            }

        }
        // the movies loader has to be restarted if phone orientation changes or the fab icon
        // drawable may not be correct: if the user clicked the favorite button, then rotated their phone,
        // unless the loader is restarted (as opposed to just calling initLoader), the old cursor will
        // be reused and the icon will not have the correct drawable
        // but it's more efficient to reuse the old loader, so just call initLoader on the others
        else if(savedInstanceState != null && !mTwoPane) {
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
            getLoaderManager().initLoader(CREDITS_LOADER_ID, null, this);
            getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, this);
            getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);
        }
        else { // this is reached in tablet mode, which does not allow orientation changes
            getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
            getLoaderManager().initLoader(CREDITS_LOADER_ID, null, this);
            getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, this);
            getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);
        }

        super.onActivityCreated(savedInstanceState);
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
            mTwoPane = getArguments().getBoolean(BUNDLE_MTWO_PANE);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
            Log.i(LOGTAG, "    mMovieId is now: " + mMovieId);
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
            mTwoPane = savedInstanceState.getBoolean(BUNDLE_MTWO_PANE);
            Log.i(LOGTAG, "    mUseFavorites is now: " + mUseFavorites);
            Log.i(LOGTAG, "    mMovieId is now: " + mMovieId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // the ref points to the same details frag layout on tablet and phone portrait,
        // but points to a wider layout in phone landscape
        View rootView = inflater.inflate(R.layout.fragment_movie_details_ref, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }


    // the details view does not care about the is_favorite column, the loader will just load whatever
    // movieId was clicked from movie grid, which should ONLY be favorites if user is in favorites activity
    // and it may be a mix of favorites/non favs if in home activity or movie pager activity
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOGTAG, "entered onCreateLoader");

//        String selectMovieIdAndIsFav = "movie_id = ? AND is_favorite = ?";
//        String[] selectionArgs =
//                new String[]{ String.valueOf(mMovieId), String.valueOf(mUseFavorites) };
        String selection = "movie_id = ?";
        String[] selectionArgs =
                new String[]{ String.valueOf(mMovieId) };

        switch (id) {
            case MOVIES_LOADER_ID:
//                Log.i(LOGTAG, "  and about to return new MOVIES_TABLE_LOADER");
                return new CursorLoader(getActivity(), MovieTheaterContract.MoviesEntry.CONTENT_URI,
                        MOVIES_PROJECTION, selection, selectionArgs, null);

            case VIDEOS_LOADER_ID:
//                Log.i(LOGTAG, "  and about to return new VIDEOS_LOADER_ID");
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.VideosEntry.CONTENT_URI, VIDEOS_PROJECTION,
                        selection, selectionArgs, null);

            case REVIEWS_LOADER_ID:
//                Log.i(LOGTAG, "  and about to return new REVIEWS_LOADER_ID");
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.ReviewsEntry.CONTENT_URI, REVIEWS_PROJECTION,
                        selection, selectionArgs, null);

        case CREDITS_LOADER_ID:
//            Log.i(LOGTAG, "  and about to return new CREDITS_LOADER_ID");
            return new CursorLoader(getActivity(),
                    MovieTheaterContract.CreditsEntry.CONTENT_URI, CREDITS_PROJECTION,
                    selection, selectionArgs,
                    MovieTheaterContract.CreditsEntry.COLUMN_ORDER + " ASC");

        }

        return null;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        if(getView() != null) {
            switch (loader.getId()) {
                case MOVIES_LOADER_ID:
                    if(data.moveToFirst()) { updateMoviesUI(data); }
                    break;

                case VIDEOS_LOADER_ID:
                    updateVideosUI(data);
                    break;

                case REVIEWS_LOADER_ID:
                    updateReviewsUI(data);
                    break;

                case CREDITS_LOADER_ID:
                    if(data.moveToFirst()) { updateCreditsUI(data); }
                    break;

                default:
                    Log.e(LOGTAG, "    DEFAULT CASE REACHED IN ON-LOAD-FINISHED IN MOV DETAIL FRAGMENT!!");

            } // end switch
        } // end if(rootView != null...)
    } // end onLoadFinished


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        Log.e(LOGTAG, "ENTERED ONLOADER RESET");
    }


    private class VideoViewListener implements View.OnClickListener {
        private String key;

        private VideoViewListener(String youTubeVideoKey) {
            key = youTubeVideoKey;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Utility.buildYouTubeUri(key));
            startActivity(intent);
        }
    }


    private class VideoShareListener implements View.OnClickListener {
        private String key;

        private VideoShareListener(String youTubeVideoKey) {
            key = youTubeVideoKey;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,
                    "Check out this video: " + Utility.buildYouTubeUri(key).toString());
            intent.setType("text/plain");
            if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }


    private class FetchMovieDetailsTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        int movieId;
        boolean updateVidsReviewsCredits;
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

        private FetchMovieDetailsTask(Context c, int movieId, boolean updateVidsReviewsCredits, LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks) {
            context = c;
            this.movieId = movieId;
            this.updateVidsReviewsCredits = updateVidsReviewsCredits;
            this.loaderCallbacks = loaderCallbacks;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(LOGTAG, "just entered FetchMovieDetailsTask.doInBackground");
            return new MovieDetailsFetcher(context, mMovieId, updateVidsReviewsCredits).fetchMovieDetails();
        }

        @Override
        protected void onPostExecute(Boolean wasSuccessful) {
            Log.i(LOGTAG,"in FetchMovieDetailsTask.onPostExecute, about to restart the Loader if no problems during fetch");

            View rootView = getView();
            if(getActivity() != null && rootView != null) {

                if(!wasSuccessful) {
                    rootView.findViewById(R.id.problem_message_details).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.scrollview).setVisibility(View.GONE);
                    rootView.findViewById(R.id.fab_favorites).setVisibility(View.GONE);
                    mCallbacks.onUpdateToolbar(null, null);
                }
                else {
                    rootView.findViewById(R.id.scrollview).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.fab_favorites).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.problem_message_details).setVisibility(View.GONE);

                    getLoaderManager().initLoader(MOVIES_LOADER_ID, null, loaderCallbacks);
                    getLoaderManager().initLoader(CREDITS_LOADER_ID, null, loaderCallbacks);
                    getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, loaderCallbacks);
                    getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, loaderCallbacks);
                }
            }
        }
    }


    // TODO: clean this mess up
    // need to check if the db already has details data in the 4 tables before firing the task, or
    // you will end up with duplicate records in videos, credits, and reviews tables, which do not
    // have a UNIQUE constraint on the movie_id column
    // returns TRUE if the task was fired
    private boolean updateVidsReviewsCreditsIfNecessary() {

//            String selectMovieIdAndIsFav = "movie_id = ? AND is_favorite = ?";
//            String[] selectionArgs =
//                    new String[]{ String.valueOf(mMovieId), "false" };
        String selection = "movie_id = ?";
        String[] selectionArgs =
                new String[]{ String.valueOf(mMovieId) };

        Cursor cursorCredits = getActivity().getContentResolver().query(
                MovieTheaterContract.CreditsEntry.CONTENT_URI,
                new String[] {MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID}, // only need to check for existence of movie_id here
                selection,
                selectionArgs,
                null);
        Log.i(LOGTAG, "  cursorCredits.getCount: " + cursorCredits.getCount());


        // the downside is that the following code will perform 3 db queries to check if a details
        // task should be fired, the upside is that it eliminates the possibility of duplicate
        // videos, credits, or reviews data ever being written to the db
        if(cursorCredits != null && cursorCredits.getCount() == 0) {
            Cursor cursorVideos = getActivity().getContentResolver().query(
                    MovieTheaterContract.VideosEntry.CONTENT_URI,
                    new String[] {MovieTheaterContract.VideosEntry.COLUMN_MOVIE_ID},
                    selection,
                    selectionArgs,
                    null);
            Log.i(LOGTAG, "    cursorVideos.getCount: " + cursorVideos.getCount());

            if(cursorVideos != null && cursorVideos.getCount() == 0) {
                Cursor cursorReviews = getActivity().getContentResolver().query(
                        MovieTheaterContract.ReviewsEntry.CONTENT_URI,
                        new String[] {MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID},
                        selection,
                        selectionArgs,
                        null);
                Log.i(LOGTAG, "      cursorReviews.getCount: " + cursorReviews.getCount());

                if(cursorReviews != null && cursorReviews.getCount() == 0) {
                    // since the count for the rows for this movieId was 0 for credits, videos, and reviews
                    // tables was zero, that must mean the detail data for this movieId has not
                    // yet been fetched, so go fetch it

                    Log.i(LOGTAG, "        about to fire a fetch details task because it appears that the details data for this movieId does not exist in the db");
                    new FetchMovieDetailsTask(getActivity(), mMovieId, true, this).execute();
                }
                cursorReviews.close();
                return true;
            }
            cursorVideos.close();
        }
        cursorCredits.close();
        return false;
    }


    private void updateMoviesUI(Cursor data) {
        // get the FAB and set it's drawable depending on if movie is a favorite or not
        mFabFavorites.setOnClickListener(new FabClickListener(getActivity(), mMovieId, mFabFavorites));

        // set the fab drawable depending on if the movie being displayed is already a favorite or not
        // and depending on if the device was just rotated or not
        boolean favoriteState = Boolean.valueOf(data.getString(COLUMN_IS_FAVORITE));
        int fabDrawable = favoriteState ?
                R.drawable.btn_star_on : R.drawable.btn_star_off;
        mFabFavorites.setImageDrawable(getResources().getDrawable(fabDrawable));

        // backdrop image.. load from either local device or from network, depending
        // on if this movie is a favorite.. even if this fragment is being hosted by
        // HomeActivity (which may or may not have some favorite movies mixed in with
        // whatever the api call returned), might as well still load the images from
        // local memory to save an expensive image download
        if(data.getString(COLUMN_IS_FAVORITE).equals("true")) {
            Picasso.with(getActivity())
                    .load(data.getString(COLUMN_BACKDROP_FILE_PATH))
                    .into(mBackdropImageView);
        }
        else {
            Picasso.with(getActivity())
                    .load(data.getString(COLUMN_BACKDROP_PATH))
                    .into(mBackdropImageView);
        }


        // other misc movie detail data
        String title = data.getString(COLUMN_MOVIE_TITLE);
        String tagline = data.getString(COLUMN_TAGLINE);
        mMovieTitleTV.setText(title);

        // set the tagline or remove it from layout if it's null
        // empty string and null are NOT the same
        if(tagline != null && !tagline.equals("")) {
            mMovieTaglineTV.setVisibility(View.VISIBLE);
            mMovieTaglineTV.setText(tagline);
        }
        else {
            mMovieTaglineTV.setVisibility(View.GONE);
        }


        // populate the genre textviews, or make them invisible if null or empty
        int[] genreColumns = {COLUMN_GENRE_NAME1, COLUMN_GENRE_NAME2,
                COLUMN_GENRE_NAME3, COLUMN_GENRE_NAME4};
        String genres = getString(R.string.genres) + " ";

        for(int i = 0; i < genreColumns.length; i++) {
            String genreName = data.getString(genreColumns[i]);

            if((genreName == null || genreName.equals("")) && i == 0) {
                mMovieGenresTV.setVisibility(View.INVISIBLE);
                break;
            } else if(genreName == null || genreName.equals("")) {
                break;
            } else {
                if(i != 0) { genres += ", "; }
                mMovieGenresTV.setVisibility(View.VISIBLE);
                genres += genreName;
            }
        }
        mMovieGenresTV.setText(genres);


        // must be in 'yyyy-MM-dd' format
        Calendar calendar = Utility.parseDate(data.getString(COLUMN_RELEASE_DATE));
        mReleaseDateTV.setText(String.format(getString(R.string.format_release_date), calendar));

        mVoteAvgTV.setText(String.valueOf(getString(R.string.vote_average) + " " +
                data.getFloat(COLUMN_VOTE_AVG)));

        mNumVotesTV.setText(String.format(getString(R.string.format_num_votes),
                data.getInt(COLUMN_NUM_VOTES)));

        // set budget, revenue, and runtime or remove from view if zero
        long budget = data.getLong(COLUMN_BUDGET);
        long revenue = data.getLong(COLUMN_REVENUE);
        int runtime = data.getInt(COLUMN_RUNTIME);

        if(budget != 0) {
            mBudgetTV.setVisibility(View.VISIBLE);
            mBudgetTV.setText(String.format(getString(R.string.format_budget), budget));
        }
        else {
            mBudgetTV.setVisibility(View.GONE);
        }

        if(revenue != 0) {
            mRevenueTV.setVisibility(View.VISIBLE);
            mRevenueTV.setText(String.format(getString(R.string.format_revenue), revenue));
        }
        else {
            mRevenueTV.setVisibility(View.GONE);
        }

        if(runtime != 0) {
            mRuntimeTV.setVisibility(View.VISIBLE);
            mRuntimeTV.setText(String.format(getString(R.string.format_runtime), runtime));
        }
        else {
            mRuntimeTV.setVisibility(View.GONE);
        }

        // OVERVIEW
        mOverviewTV.setText(data.getString(COLUMN_OVERVIEW));

        if(mTwoPane) {
            // app is running in tablet mode, let hosting activity deal with toolbar update
            // there is nothing special in the toolbar in phone mode, FYI
            mCallbacks.onUpdateToolbar(title, tagline);
        }

    }


    private void updateVideosUI(Cursor data) {

        if(!data.moveToFirst()) {
            mVideoSectionTitle.setVisibility(View.GONE);
            mVideoShareButton1ImageView.setVisibility(View.GONE);
            mVideoShareButton2ImageView.setVisibility(View.GONE);
            mVideoThumbnailImageView1.setVisibility(View.GONE);
            mVideoThumbnailImageView2.setVisibility(View.GONE);
            return;
        }

        if(!data.getString(COLUMN_VIDEO_KEY).equals("")) {
//            Log.e(LOGTAG, "    video key: " + data.getString(COLUMN_VIDEO_KEY));
//            Log.e(LOGTAG, "    youtube URL: " + Utility.buildYouTubeUri(data.getString(COLUMN_VIDEO_KEY)));
//            Log.e(LOGTAG, "    thumb URL: " + data.getString(COLUMN_VIDEO_THUMBNAIL_URL));

            data.moveToFirst();

            // video thumbnail image
            Picasso.with(getActivity())
                    .load(data.getString(COLUMN_VIDEO_THUMBNAIL_URL))
                    .placeholder(R.drawable.ic_play_circle_outline_white_48dp)
                    .into(mVideoThumbnailImageView1);

            // set a click listener on the ImageView video trailer thumbnail and play button
            mVideoThumbnailImageView1.setOnClickListener(
                    new VideoViewListener(data.getString(COLUMN_VIDEO_KEY)));

            // set a click listener on the ImageView share button
            mVideoShareButton1ImageView.setOnClickListener(
                    new VideoShareListener(data.getString(COLUMN_VIDEO_KEY)));

            if(!data.moveToNext()) {
                mVideoShareButton2ImageView.setVisibility(View.GONE);
                mVideoThumbnailImageView2.setVisibility(View.GONE);
                return;
            }
        }

        if(!data.getString(COLUMN_VIDEO_KEY).equals("")) {
//            Log.e(LOGTAG, "    2video key: " + data.getString(COLUMN_VIDEO_KEY));
//            Log.e(LOGTAG, "    2youtube URL: " + Utility.buildYouTubeUri(data.getString(COLUMN_VIDEO_KEY)));
//            Log.e(LOGTAG, "    2thumb URL: " + data.getString(COLUMN_VIDEO_THUMBNAIL_URL));

            // video thumbnail image
            Picasso.with(getActivity())
                    .load(data.getString(COLUMN_VIDEO_THUMBNAIL_URL))
                    .placeholder(R.drawable.ic_play_circle_outline_white_48dp)
                    .into(mVideoThumbnailImageView2);

            // set a click listener on the ImageView video trailer thumbnail and play button
            mVideoThumbnailImageView2.setOnClickListener(
                    new VideoViewListener(data.getString(COLUMN_VIDEO_KEY)));

            // set a click listener on the ImageView share button
            mVideoShareButton2ImageView.setOnClickListener(
                    new VideoShareListener(data.getString(COLUMN_VIDEO_KEY)));
        }
    }


    private void updateReviewsUI(Cursor data) {






    }


    private void updateCreditsUI(Cursor data) {
        int profileImageColumn = mUseFavorites ? COLUMN_PROFILE_FILE_PATH : COLUMN_PROFILE_PATH;

        ImageView[] imageViews = {mProfile1ImageView, mProfile2ImageView,
                mProfile3ImageView, mProfile4ImageView};
        TextView[] characterTVs = {mCharacterName1TV, mCharacterName2TV,
                mCharacterName3TV, mCharacterName4TV};
        TextView[] castTVs = {mCastName1TV, mCastName2TV, mCastName3TV, mCastName4TV};

        for(int i = 0; i < imageViews.length; i++) {
            if(data.isAfterLast()) { break; }

            if (data.getString(profileImageColumn) != null) {
                // cast member profile image path
                Picasso.with(getActivity())
                        .load(data.getString(profileImageColumn))
                        .placeholder(R.drawable.person_placeholder)
                        .into(imageViews[i]);

                characterTVs[i].setText(String.format(getString(R.string.format_character_name),
                        data.getString(COLUMN_CHARACTER)));
                castTVs[i].setText(String.format(getString(R.string.format_cast_name),
                        data.getString(COLUMN_CAST_NAME)));
            }
            data.moveToNext();
        }
    }

}






