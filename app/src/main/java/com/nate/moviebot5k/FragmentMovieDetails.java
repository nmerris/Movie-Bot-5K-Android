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
import android.widget.TextView;

import com.nate.moviebot5k.api_fetching.MovieDetailsFetcher;
import com.nate.moviebot5k.data.MovieTheaterContract;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Displays movie details, the top 2 reviews (if there are any), the top 2 videos (if there are any),
 * and the top 4 credits (there are almost always at least 4 credits associated with any movie, even
 * the really obscure and/or old ones).  This fragment needs to know 3 things to work properly: the
 * movieId in the movies table of the movie to show (mMovieId), if the app is running in tablet mode
 * (mTwoPane), and if it should only use records labeled as favorites from the db (mUseFavorites).
 * The hosting activity should use the static newInstance method to create this fragment.
 *
 * <br><br>
 *     Loaders are used for each db table that is queried, and they are all initialized in
 *     onActivityCreated.  There are two basic scenarios that play out after that:
 *
 *     <br>
 *     1. NOT IN 'FAVORITES MODE' - The loaders are all restarted in FetchMovieDetailsTask.onPostExecute, if
 *     the fetch task was successful.  At this point the db has already been updated with the new
 *     data and restarting the loaders will update the UI.
 *     <br>
 *     2. IN 'FAVORITES MODE' - In favorites mode, no network activity occurs, the idea being that
 *     the user can view their favorites even if they are a mile underground in a cave somewhere..
 *     spelunking android app users who need to view their favorite movies list: it's a user demographic
 *     that must not be ignored.  But seriously, the loaders just initialize in onActivityCreated,
 *     and that's about it.. the db is already filled with all the data from whenver the user first
 *     saved the movie as a favorite.
 *
 * <br><br>
 * Note: to avoid unnecessary network calls, this fragment checks the db to see if the movie in question
 * already has detail data.  If it does, no network call is made.
 * To be clear: in this app the data in the db is created in 2 phases: an
 * initial chunk of the 'movies' table is populated in FragmentMovieGrid.  But due to the limits
 * imposed by themoviedb's API, additional fetches are needed to get the rest of the movie detail data.
 * For example, the budget, revenue, runtime, and tagline columns are filled in HERE.  Additionally,
 * all data in the credits, reviews, and videos tables are filled in here.
 *
 * <br><br>
 * Finally, a Floating Action Button that appears are disappears as the screen is scrolled is set up
 * to process favorite add/remove requests by the user.
 *
 * @see MovieDetailsFetcher
 * @see MovieTheaterContract
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

    private boolean mUseFavorites; // true if db favorites table should be used in this fragment
    private int mMovieId; // the id for the movie or favorite movie
    private boolean mTwoPane; // true in tablet mode

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

    @Bind(R.id.credits_show_all) TextView mCreditsShowAll;
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

    @Bind(R.id.reviews_section_title) TextView mReviewSectionTitle;
    @Bind(R.id.review_author1_textview) TextView mReviewAuthor1TV;
    @Bind(R.id.review_author2_textview) TextView mReviewAuthor2TV;
    @Bind(R.id.review_content1_textview) TextView mReviewContent1TV;
    @Bind(R.id.review_content2_textview) TextView mReviewContent2TV;

    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] MOVIES_PROJECTION = {
            MovieTheaterContract.MoviesEntry._ID,
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
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE,
            MovieTheaterContract.MoviesEntry.COLUMN_VOTE_COUNT
    };
    private static final int COLUMN_OVERVIEW = 1;
    private static final int COLUMN_RELEASE_DATE = 2;
    private static final int COLUMN_GENRE_NAME1 = 3;
    private static final int COLUMN_GENRE_NAME2 = 4;
    private static final int COLUMN_GENRE_NAME3 = 5;
    private static final int COLUMN_GENRE_NAME4 = 6;
    private static final int COLUMN_MOVIE_TITLE = 7;
    private static final int COLUMN_BACKDROP_PATH = 8;
    private static final int COLUMN_VOTE_AVG = 9;
    private static final int COLUMN_BUDGET = 10;
    private static final int COLUMN_REVENUE = 11;
    private static final int COLUMN_RUNTIME = 12;
    private static final int COLUMN_TAGLINE = 13;
    private static final int COLUMN_BACKDROP_FILE_PATH = 14;
    private static final int COLUMN_IS_FAVORITE = 15;
    private static final int COLUMN_NUM_VOTES = 16;
    
    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] REVIEWS_PROJECTION = {
            MovieTheaterContract.ReviewsEntry._ID,
            MovieTheaterContract.ReviewsEntry.COLUMN_AUTHOR,
            MovieTheaterContract.ReviewsEntry.COLUMN_CONTENT,
    };
    private static final int COLUMN_REVIEW_AUTHOR = 1;
    private static final int COLUMN_REVIEW_CONTENT = 2;

    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] CREDITS_PROJECTION = {
            MovieTheaterContract.CreditsEntry._ID,
            MovieTheaterContract.CreditsEntry.COLUMN_CHARACTER,
            MovieTheaterContract.CreditsEntry.COLUMN_NAME,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH,
    };
    private static final int COLUMN_CHARACTER = 1;
    private static final int COLUMN_CAST_NAME = 2;
    private static final int COLUMN_PROFILE_PATH = 3;
    private static final int COLUMN_PROFILE_FILE_PATH = 4;

    // IF YOU CHANGE THIS THEN YOU MUST ALSO CHANGE THE INTS BELOW IT
    private final String[] VIDEOS_PROJECTION = {
            MovieTheaterContract.VideosEntry._ID,
            MovieTheaterContract.VideosEntry.COLUMN_KEY,
            MovieTheaterContract.VideosEntry.COLUMN_THUMBNAIL_URL
    };
    private static final int COLUMN_VIDEO_KEY = 1;
    private static final int COLUMN_VIDEO_THUMBNAIL_URL = 2;


    /**
     * Creates a new FragmentMovieDetails with all the info it needs to work.
     *
     * @param useFavorites pass true if this fragment should not make any network calls and instead
     *                     only use data from the db tables to display a details fragment
     * @param movieId themoviedb movieId of which to show the details
     * @param mTwoPane pass true if app is running in tablet (ie two pane) mode
     * @return a new fragment
     */
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
        /**
         * At present, this method is only called in tablet mode because I had trouble getting the
         * viewpager to work and stay in sync with the toolbar having custom title and subtitle.
         * Fires when the second pane toolbar should be updated so the title and subtitle
         * match that of the currently displayed movie.
         *
         * @param movieTitle the movie title of the currently displayed movie
         * @param movieTagline the tagline of the currently displayed movie
         */
        void onUpdateToolbar(String movieTitle, String movieTagline);

        /**
         * Fires when user clicks the button to view all the credits for the currently displayed movie.
         * @param movieId the movieId currently being displayed
         */
        void onCreditsShowAllClicked(int movieId);
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

        if(savedInstanceState == null && !mUseFavorites) {
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

        // this is reached in tablet mode, which does not allow orientation changes, so
        // just initialize all the loaders
        else {
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

        // if this fragment is being created from new (ie it's hosting activity has performed a
        // fragment transaction), then get the movieId from the frag arg and then check to see if
        // a fetch details async task should be fired, if it does fire it will restart the loader
        // when it is done, which will be the second time that has happened because it would have
        // already happened on onActivityCreated when the loader is initialized.. seems acceptable
        if(savedInstanceState == null) {
            mMovieId = getArguments().getInt(BUNDLE_MOVIE_ID_KEY);
            mUseFavorites = getArguments().getBoolean(BUNDLE_USE_FAVORITES_KEY);
            mTwoPane = getArguments().getBoolean(BUNDLE_MTWO_PANE);
            Log.i(LOGTAG, "in onCreate: mMovieId from fragment argument is: " + mMovieId);
        }

        // must be some other reason the fragment is being recreated, likely an orientation change,
        // so get mUseFavorites table from the Bundle, which was stored prev. in onSaveInstanceState
        // the loader will restart next when onCreateView is called, there is no need to check if a
        // fetch details task needs to happen because that would have already happened when this fragment
        // was initially created
        else {
            mMovieId = savedInstanceState.getInt(BUNDLE_MOVIE_ID_KEY);
            mUseFavorites = savedInstanceState.getBoolean(BUNDLE_USE_FAVORITES_KEY);
            mTwoPane = savedInstanceState.getBoolean(BUNDLE_MTWO_PANE);
            Log.i(LOGTAG, "in onCreate: mMovieId from savedInstanceState is: " + mMovieId);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }


    // the details view does not care about the is_favorite column, the loader will just load whatever
    // movieId was clicked from movie grid, which should ONLY be favorites if user is in favorites activity
    // and it may be a mix of favorites/non favs if in home activity or movie pager activity
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // selection and selectionArgs are same for every loader
        String selection = "movie_id = ?";
        String[] selectionArgs =
                new String[]{ String.valueOf(mMovieId) };

        switch (id) {
            case MOVIES_LOADER_ID:
                return new CursorLoader(getActivity(), MovieTheaterContract.MoviesEntry.CONTENT_URI,
                        MOVIES_PROJECTION, selection, selectionArgs, null);

            case VIDEOS_LOADER_ID:
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.VideosEntry.CONTENT_URI, VIDEOS_PROJECTION,
                        selection, selectionArgs, null);

            case REVIEWS_LOADER_ID:
                return new CursorLoader(getActivity(),
                        MovieTheaterContract.ReviewsEntry.CONTENT_URI, REVIEWS_PROJECTION,
                        selection, selectionArgs, null);

        case CREDITS_LOADER_ID:
            // credits table should be sorted, the order is by most prominent cast to least
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

        // never hurts to check if this fragment's root view is still valid
        // as each loader finishes, update the UI as appropriate
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
                    // I have yet to come across a single movie that did not have credits data
                    if(data.moveToFirst()) { updateCreditsUI(data); }

                    // due to the need to get this project done, I did not save every credits
                    // profile image to the local device, so you only get 4 credits when viewing
                    // favorites, however in 'normal' mode you can see them all
                    if(mUseFavorites) {
                        mCreditsShowAll.setVisibility(View.GONE);
                    } else {
                        // set a click listener on the Show All button and call back to hosting
                        // activity if user clicks it
                        mCreditsShowAll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCallbacks.onCreditsShowAllClicked(mMovieId);
                            }
                        });
                    }
                    break;

                default:
                    Log.e(LOGTAG, "    DEFAULT CASE REACHED IN ON-LOAD-FINISHED IN MOV DETAIL FRAGMENT!!");

            } // end switch
        } // end if(rootView != null...)
    } // end onLoadFinished


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}


    /**
     * Listens for clicks on the video thumbnail image and fires an ACTION_VIEW intent if clicked,
     * setting the youTube video key through with the intent.
     */
    private class VideoViewListener implements View.OnClickListener {
        private String key;

        private VideoViewListener(String youTubeVideoKey) {
            key = youTubeVideoKey;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Utility.buildYouTubeUri(key));
            if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }


    /**
     * Listens for clicks on the share icon next to each video thumbnail and fires an ACTION_SEND
     * intent, with an extra that is a link to the video just clicked.
     */
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


    /**
     * Makes a network call to themoviedb.com server to fetch details data for the movie in question.
     * The db tables are updated in the background thread created with this task, if there are no
     * problems.  Finally, the loaders associated with this fragment are started in onPostExecute,
     * but only if the fetch task did not have any problems.  If there were any problems, a msg is
     * displayed on screen explaining that there were problems.
     */
    private class FetchMovieDetailsTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        int movieId;
        boolean updateVidsReviewsCredits;
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

        /**
         * Creates a new fetch task.  Before executing this task, check the db tables for videos,
         * reviews, and credits to see if they already contain details data for movieId.  If the tables
         * do NOT already have details data, pass in true for updateVidsReviewsCredits so that this
         * task will update those tables.  loaderCallbacks are used to restart the loader after this
         * task completes, but only if it was successful.
         *
         * @param movieId themoviedb movieId of which to fetch movie details data
         * @param updateVidsReviewsCredits pass true if the task should also fetch videos, reviews,
         *                                 and credits data, and update the appropriate tables, otherwise
         *                                 pass false to avoid duplicate records in these tables
         * @param loaderCallbacks the LoaderCallbacks object that will be used to restart the loader
         *                        when this task completes, but only if it was successful during fetch
         */
        private FetchMovieDetailsTask(Context c, int movieId, boolean updateVidsReviewsCredits,
                                      LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks) {
            context = c;
            this.movieId = movieId;
            this.updateVidsReviewsCredits = updateVidsReviewsCredits;
            this.loaderCallbacks = loaderCallbacks;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // the meaty stuff happens here
            return new MovieDetailsFetcher(context, mMovieId, updateVidsReviewsCredits).fetchMovieDetails();
        }

        @Override
        protected void onPostExecute(Boolean wasSuccessful) {
            // to avoid null pointer exceptions, best to check that both rootview and the activity
            // that was hosting this task before it fired and started a new thread, are still valid
            View rootView = getView();
            if(getActivity() != null && rootView != null) {
                // not a successful fetch, so show an error msg
                if(!wasSuccessful) {
                    rootView.findViewById(R.id.problem_message_details).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.scrollview).setVisibility(View.GONE);
                    rootView.findViewById(R.id.fab_favorites).setVisibility(View.GONE);
                    mCallbacks.onUpdateToolbar(null, null);
                }
                // fetch was ok, so make sure everything is visible and restart the loaders
                // so the UI will be updated with the new data
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


    /**
     * Returns true only if videos, credits, and reviews tables have no entries that have
     * column movie_id = mMovieId.
     * If it does find all those tables empty, it will fire a fetch details task
     * that will insert data for those tables, in addition to the extra bits of movie detail
     * data gets updated in the movies table.
     * <br><br>
     * I am really not very happy with this entire method, I
     * feel that it's pretty inefficient to do up to 3 queries in a row.  This project took longer
     * than I anticipated and if I had infinite time I would make it more elegant, but it works.
     * I really ended up relying too heavily on the db tables and my content provider in this app.
     * I thought that was going to be the way to go, but it ended up being a lot of work.  I should
     * have used something like a singleton, like in my first Popular Movies app, and kept more
     * data in super fast memory in java objects.  And did a sparing amount of db work.  I am glad
     * I got a lot of practice with db work in general though, so it's not all bad :)
     * That would have saved me trouble elsewhere too.
     * Live and learn!
     *
     * @return true if videos, reviews, and credits tables all have zero rows that match mMovieId
     */
    private boolean updateVidsReviewsCreditsIfNecessary() {
        String selection = "movie_id = ?";
        String[] selectionArgs =
                new String[]{ String.valueOf(mMovieId) };

        Cursor cursorCredits = getActivity().getContentResolver().query(
                MovieTheaterContract.CreditsEntry.CONTENT_URI,
                new String[] {MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID}, // only need to check for existence of movie_id here
                selection,
                selectionArgs,
                null);

        if(cursorCredits != null && cursorCredits.getCount() == 0) {
            Cursor cursorVideos = getActivity().getContentResolver().query(
                    MovieTheaterContract.VideosEntry.CONTENT_URI,
                    new String[] {MovieTheaterContract.VideosEntry.COLUMN_MOVIE_ID},
                    selection,
                    selectionArgs,
                    null);

            if(cursorVideos != null && cursorVideos.getCount() == 0) {
                Cursor cursorReviews = getActivity().getContentResolver().query(
                        MovieTheaterContract.ReviewsEntry.CONTENT_URI,
                        new String[] {MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID},
                        selection,
                        selectionArgs,
                        null);

                if(cursorReviews != null && cursorReviews.getCount() == 0) {
                    // since the count for the rows for this movieId was 0 for credits, videos, and reviews
                    // tables was zero, that must mean the detail data for this movieId has not
                    // yet been fetched, so go fetch it
                    new FetchMovieDetailsTask(getActivity(), mMovieId, true, this).execute();
                }
                cursorReviews.close();
                // there were no records in any of the tables for this movieId, so return true
                return true;
            }
            cursorVideos.close();
        }
        cursorCredits.close();
        // at least on of the tables had a record for this movieId, so return false
        return false;
    }


    /**
     * Updates the display portions that use data from the movies table.  Also sets a click
     * listener on the Floating Action Button for add/remove favorite.
     */
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
        int column = data.getString(COLUMN_IS_FAVORITE).equals("true") ?
                COLUMN_BACKDROP_FILE_PATH : COLUMN_BACKDROP_PATH;
        Picasso.with(getActivity())
                .load(data.getString(column))
                .placeholder(getResources().getDrawable(R.drawable.placeholder_backdrop))
                .into(mBackdropImageView);

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
    
    
    /**
     * Updates the display portions that use data from the videos table.  Also sets click
     * listeners on both the video thumbnail image (to view movies in youtube) and the share
     * button next to each video.  Presently only shows a max of 2 videos.
     */
    private void updateVideosUI(Cursor data) {

        // hide everything if there are no videos to display
        if(!data.moveToFirst()) {
            mVideoSectionTitle.setVisibility(View.GONE);
            mVideoShareButton1ImageView.setVisibility(View.GONE);
            mVideoShareButton2ImageView.setVisibility(View.GONE);
            mVideoThumbnailImageView1.setVisibility(View.GONE);
            mVideoThumbnailImageView2.setVisibility(View.GONE);
            return;
        }

        if(!data.getString(COLUMN_VIDEO_KEY).equals("")) {
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
    
    
    /**
     * Updates the display portions that use data from the reviews table.
     * Presently only shows a max of 2 reviews.
     */
    private void updateReviewsUI(Cursor data) {
        // hide everything if there are not actually any reviews
        // I think this looks nicer than having it say 'no reviews' or whatever
        if(!data.moveToFirst()) {
            mReviewSectionTitle.setVisibility(View.GONE);
            mReviewAuthor1TV.setVisibility(View.GONE);
            mReviewAuthor2TV.setVisibility(View.GONE);
            mReviewContent1TV.setVisibility(View.GONE);
            mReviewContent2TV.setVisibility(View.GONE);
            return;
        }

        // set text for review author 1
        mReviewAuthor1TV.setText(String.format(getString(R.string.format_review_author),
                data.getString(COLUMN_REVIEW_AUTHOR)));
        mReviewContent1TV.setText(data.getString(COLUMN_REVIEW_CONTENT));

        if(!data.moveToNext()) {
            mReviewAuthor2TV.setVisibility(View.GONE);
            mReviewContent2TV.setVisibility(View.GONE);
            return;
        }

        // set text for review author 2
        mReviewAuthor2TV.setText(String.format(getString(R.string.format_review_author),
                data.getString(COLUMN_REVIEW_AUTHOR)));
        mReviewContent2TV.setText(data.getString(COLUMN_REVIEW_CONTENT));
    }

    /**
     * Updates the display portions that use data from the credits table.
     * Presently only shows a max of 4 credits in this fragment.  User can navigate to
     * ActivityCredits to view all credits for a movie.  Due to time considerations, if you are
     * viewing your favorites, you only get 4 credits.  It's not ideal, but I can't spend forever on
     * this, my second serious Android project ever attempted.
     */
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
                        .placeholder(R.drawable.placeholder_person)
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






