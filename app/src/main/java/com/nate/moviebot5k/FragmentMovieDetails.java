package com.nate.moviebot5k;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
//    private Callbacks mCallbacks; // hosting activity will define what the method(s) inside Callback interface should do
    private int mMovieId; // the id for the movie or favorite movie
    private boolean mTwoPane;
    private List<Long> mMovieIdList; // the list of movies to display

    @Bind(R.id.fab_favorites) FloatingActionButton mFabFavorites;

    @Bind(R.id.backdrop_imageview) ImageView mBackdropImageView;
    @Bind(R.id.test_details_textview) TextView mDetailsTextView;

    @Bind(R.id.test_videos1_textview) TextView mVideosTextView1;
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
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE
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
    private static final int COLUMN_PROFILE__FILE_PATH = 6;

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
    public static FragmentMovieDetails newInstance(boolean useFavoritesTable, int movieId, boolean mTwoPane) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_USE_FAVORITES_KEY, useFavoritesTable);
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
        outState.putBoolean(BUNDLE_MTWO_PANE, mTwoPane);

//        outState.p

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {


        // TODO: I'm really not sure where to put initLoader..
        getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        getLoaderManager().initLoader(CREDITS_LOADER_ID, null, this);
        getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);




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

//        // TODO: I'm really not sure where to put initLoader..
//        getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
//        getLoaderManager().initLoader(CREDITS_LOADER_ID, null, this);
//        getLoaderManager().initLoader(VIDEOS_LOADER_ID, null, this);
//        getLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.i(LOGTAG, "entered onCreateLoader");

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
//        Log.i(LOGTAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ entered onLoadFinished");
        View rootView = getView();

//        if(!data.moveToFirst()) Log.e(LOGTAG, "  AND DATA COULD NOT MOVE TO FIRST WITH LOADER ID: " + loader.getId());



        if(rootView != null && data.moveToFirst()) {

            switch (loader.getId()) {
                case MOVIES_LOADER_ID:

                    // get the FAB and set it's drawable depending on if movie is a favorite or not
//                    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_favorites);
                    mFabFavorites.setOnClickListener(new FabClickListenerAndDrawableUpdater());
//                    Cursor cursor = getActivity().getContentResolver().query(
//                            MovieTheaterContract.MoviesEntry.CONTENT_URI,
//                            new String[]{ MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE },
//                            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
//                            new String[]{ String.valueOf(mMovieId) }, null);
//
//                    cursor.moveToFirst();

                    Log.i(LOGTAG, "  movieId in onLoadFinished for MOVIES_LOADER was: " + mMovieId);

                    boolean favoriteState = Boolean.valueOf(data.getString(COLUMN_IS_FAVORITE));

                    Log.i(LOGTAG, "  and before anything else, column is_favorite for that id is: " + favoriteState);

                    // set the fab drawable depending on if the movie being displayed is already a favorite or not
                    int fabDrawable = favoriteState ?
                            R.drawable.btn_star_on_normal_holo_light : R.drawable.btn_star_off_normal_holo_light;

//                    Log.i(LOGTAG, "    and fabDrawable id is: " + fabDrawable);

                    mFabFavorites.setImageDrawable(getResources().getDrawable(fabDrawable));




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

                    // update the toolbar title and subtitle
                    if(mTwoPane) {
                        // app is running in tablet mode, let hosting activity deal with toolbar update
                        mCallbacks.onUpdateToolbar(title, tagline);
                    }


                    mDetailsTextView.setText("DETAILS FROM MOVIE TABLE: \n" +
                            genreName1 + " " + genreName2 + " " + genreName3 + " " + genreName4 + "\n" +
                            "MOVIE_ID: " + mMovieId + "\n" +
                            overview + "\n" +
                            "Release Date: " + releaseDate + "\n" +
                            "Vote Avg: " + voteAvg + "\n" +
                            "Budget: " + budget + "\n" +
                            "Runtime: " + runtime + "\n");

                    break;





                case VIDEOS_LOADER_ID:
//                    Log.e(LOGTAG, "  from videos table loader, key: " + data.getString(COLUMN_VIDEO_KEY));

                    // testing, prob. need some kind of adapter in due to varying numbers of videos
                    // but will always only show so many on main details page, and will need to have
                    // a link to another activity that shows them all in scrolling list

                    // is it really worth it to use an adapter?  not sure
                    // I don't really need to check for full here, that was checked before this
                    // switch statement started when if(data.moveToFirst... executed
                    if(data.getString(COLUMN_VIDEO_KEY) != null) {
                        // video thumbnail image 1
                        Picasso.with(getActivity())
                                .load(data.getString(COLUMN_VIDEO_THUMBNAIL_URL))
                                .into(mVideoThumbnailImageView1);
                        // set a click listener on the ImageView video trailer thumbnail
                        mVideoThumbnailImageView1.setOnClickListener(new VideoViewListener(data.getString(COLUMN_VIDEO_KEY)));

                        // set a click listener on the ... well this will be an share ICON in future
                        mVideosTextView1.setText(data.getString(COLUMN_VIDEO_NAME));
                        mVideosTextView1.setOnClickListener(new VideoShareListener(data.getString(COLUMN_VIDEO_KEY)));

                    }


                    // again testing, just doing 2 videos now, if there are even 2
                    if(data.moveToNext()) {
                        // video thumbnail image 2
                        Picasso.with(getActivity())
                                .load(data.getString(COLUMN_VIDEO_THUMBNAIL_URL))
                                .into(mVideoThumbnailImageView2);

                        mVideosTextView2.setText(data.getString(COLUMN_VIDEO_NAME));
                    }
                    break;





                case REVIEWS_LOADER_ID:

                    // testing
                    String testRevText = "REVIEW AUTHOR 1: " + data.getString(COLUMN_REVIEW_AUTHOR) +
                            "\n" + data.getString(COLUMN_REVIEW_CONTENT) + "\n";

                    if(data.moveToNext()) {
                        testRevText += "REVIEW AUTHOR 2: " + data.getString(COLUMN_REVIEW_AUTHOR) +
                                "\n" + data.getString(COLUMN_REVIEW_CONTENT) + "\n";
                    }
                    if(data.moveToNext()) {
                        testRevText += "REVIEW AUTHOR 3: " + data.getString(COLUMN_REVIEW_AUTHOR) +
                                "\n" + data.getString(COLUMN_REVIEW_CONTENT) + "\n";
                    }

                    mReviewsTextView.setText(testRevText);
                    break;





                case CREDITS_LOADER_ID:
                    // better check for null here.. some cast may not have a profile img path
                    // the credits order should already be correct due to the order param passed
                    // in when this particular loader was created

                    // testing
                    if(data.getString(COLUMN_PROFILE_PATH) != null) {
                        // cast member profile image path
                        Picasso.with(getActivity())
                                .load(data.getString(COLUMN_PROFILE_PATH))
                                .into(mCreditsProfile1);

                        mCreditsTextView1.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
                    }
                    if(data.moveToNext()) {
                        if(data.getString(COLUMN_PROFILE_PATH) != null) {
                            Picasso.with(getActivity())
                                    .load(data.getString(COLUMN_PROFILE_PATH))
                                    .into(mCreditsProfile2);
                        }
                        mCreditsTextView2.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
                    }
                    if(data.moveToNext()) {
                        if(data.getString(COLUMN_PROFILE_PATH) != null) {
                            Picasso.with(getActivity())
                                    .load(data.getString(COLUMN_PROFILE_PATH))
                                    .into(mCreditsProfile3);
                        }
                        mCreditsTextView3.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
                    }
                    if(data.moveToNext()) {
                        if(data.getString(COLUMN_PROFILE_PATH) != null) {
                            Picasso.with(getActivity())
                                    .load(data.getString(COLUMN_PROFILE_PATH))
                                    .into(mCreditsProfile4);
                        }
                        mCreditsTextView4.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
                    }
                    break;

                default:
                    Log.e(LOGTAG, "    DEFAULT CASE REACHED IN ON-LOAD-FINISHED IN MOV DETAIL FRAGMENT!!");

            } // end switch
        } // end if(rootView != null...)






//        if(!data.moveToFirst()) Log.e(LOGTAG, "  AND DATA COULD NOT MOVE TO FIRST WITH LOADER ID: " + loader.getId());
//
//        if(rootView != null && data.moveToFirst()) {
//
//            switch (loader.getId()) {
//                case MOVIES_LOADER_ID:
//
//                    // get the FAB and set it's drawable depending on if movie is a favorite or not
////                    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_favorites);
//                    mFabFavorites.setOnClickListener(new FabClickListenerAndDrawableUpdater(data));
//
//
//
//
//
//                    // backdrop image
//                    Picasso.with(getActivity())
//                            .load(data.getString(COLUMN_BACKDROP_PATH))
//                            .into(mBackdropImageView);
//
//                    // other misc movie detail data
//                    String genreName1 = data.getString(COLUMN_GENRE_NAME1);
//                    String genreName2 = data.getString(COLUMN_GENRE_NAME2);
//                    String genreName3 = data.getString(COLUMN_GENRE_NAME3);
//                    String genreName4 = data.getString(COLUMN_GENRE_NAME4);
//                    String overview = data.getString(COLUMN_OVERVIEW);
//                    String releaseDate = data.getString(COLUMN_RELEASE_DATE);
//                    String title = data.getString(COLUMN_MOVIE_TITLE);
//                    float voteAvg = data.getFloat(COLUMN_VOTE_AVG);
//                    long budget = data.getLong(COLUMN_BUDGET);
//                    long revenue = data.getLong(COLUMN_REVENUE);
//                    int runtime = data.getInt(COLUMN_RUNTIME);
//                    String tagline = data.getString(COLUMN_TAGLINE);
//
//                    // update the toolbar title and subtitle
//                    if(mTwoPane) {
//                        // app is running in tablet mode, let hosting activity deal with toolbar update
//                        mCallbacks.onUpdateToolbar(title, tagline);
//                    }
//
//
//                    mDetailsTextView.setText("DETAILS FROM MOVIE TABLE: \n" +
//                            genreName1 + " " + genreName2 + " " + genreName3 + " " + genreName4 + "\n" +
//                            "MOVIE_ID: " + mMovieId + "\n" +
//                            overview + "\n" +
//                            "Release Date: " + releaseDate + "\n" +
//                            "Vote Avg: " + voteAvg + "\n" +
//                            "Budget: " + budget + "\n" +
//                            "Runtime: " + runtime + "\n");
//
//                    break;
//
//
//
//
//
//                case VIDEOS_LOADER_ID:
//                    Log.e(LOGTAG, "  from videos table loader, key: " + data.getString(COLUMN_VIDEO_KEY));
//
//                    // testing, prob. need some kind of adapter in due to varying numbers of videos
//                    // but will always only show so many on main details page, and will need to have
//                    // a link to another activity that shows them all in scrolling list
//
//                    // is it really worth it to use an adapter?  not sure
//                    // I don't really need to check for full here, that was checked before this
//                    // switch statement started when if(data.moveToFirst... executed
//                    if(data.getString(COLUMN_VIDEO_KEY) != null) {
//                        // video thumbnail image 1
//                        Picasso.with(getActivity())
//                                .load(data.getString(COLUMN_VIDEO_THUMBNAIL_URL))
//                                .into(mVideoThumbnailImageView1);
//                        // set a click listener on the ImageView video trailer thumbnail
//                        mVideoThumbnailImageView1.setOnClickListener(new VideoViewListener(data.getString(COLUMN_VIDEO_KEY)));
//
//                        // set a click listener on the ... well this will be an share ICON in future
//                        mVideosTextView1.setText(data.getString(COLUMN_VIDEO_NAME));
//                        mVideosTextView1.setOnClickListener(new VideoShareListener(data.getString(COLUMN_VIDEO_KEY)));
//
//                    }
//
//
//                    // again testing, just doing 2 videos now, if there are even 2
//                    if(data.moveToNext()) {
//                        // video thumbnail image 2
//                        Picasso.with(getActivity())
//                                .load(data.getString(COLUMN_VIDEO_THUMBNAIL_URL))
//                                .into(mVideoThumbnailImageView2);
//
//                    mVideosTextView2.setText(data.getString(COLUMN_VIDEO_NAME));
//                    }
//                    break;
//
//
//
//
//
//                case REVIEWS_LOADER_ID:
//
//                    // testing
//                    String testRevText = "REVIEW AUTHOR 1: " + data.getString(COLUMN_REVIEW_AUTHOR) +
//                    "\n" + data.getString(COLUMN_REVIEW_CONTENT) + "\n";
//
//                    if(data.moveToNext()) {
//                        testRevText += "REVIEW AUTHOR 2: " + data.getString(COLUMN_REVIEW_AUTHOR) +
//                                "\n" + data.getString(COLUMN_REVIEW_CONTENT) + "\n";
//                    }
//                    if(data.moveToNext()) {
//                        testRevText += "REVIEW AUTHOR 3: " + data.getString(COLUMN_REVIEW_AUTHOR) +
//                                "\n" + data.getString(COLUMN_REVIEW_CONTENT) + "\n";
//                    }
//
//                    mReviewsTextView.setText(testRevText);
//                    break;
//
//
//
//
//
//                case CREDITS_LOADER_ID:
//                    // better check for null here.. some cast may not have a profile img path
//                    // the credits order should already be correct due to the order param passed
//                    // in when this particular loader was created
//
//                    // testing
//                    if(data.getString(COLUMN_PROFILE_PATH) != null) {
//                        // cast member profile image path
//                        Picasso.with(getActivity())
//                                .load(data.getString(COLUMN_PROFILE_PATH))
//                                .into(mCreditsProfile1);
//
//                        mCreditsTextView1.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
//                            ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
//                    }
//                    if(data.moveToNext()) {
//                        if(data.getString(COLUMN_PROFILE_PATH) != null) {
//                            Picasso.with(getActivity())
//                                    .load(data.getString(COLUMN_PROFILE_PATH))
//                                    .into(mCreditsProfile2);
//                        }
//                        mCreditsTextView2.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
//                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
//                    }
//                    if(data.moveToNext()) {
//                        if(data.getString(COLUMN_PROFILE_PATH) != null) {
//                            Picasso.with(getActivity())
//                                    .load(data.getString(COLUMN_PROFILE_PATH))
//                                    .into(mCreditsProfile3);
//                        }
//                        mCreditsTextView3.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
//                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
//                    }
//                    if(data.moveToNext()) {
//                        if(data.getString(COLUMN_PROFILE_PATH) != null) {
//                            Picasso.with(getActivity())
//                                    .load(data.getString(COLUMN_PROFILE_PATH))
//                                    .into(mCreditsProfile4);
//                        }
//                        mCreditsTextView4.setText("CHARACTER: " + data.getString(COLUMN_CHARACTER) +
//                                ", CAST NAME: " + data.getString(COLUMN_CAST_NAME));
//                    }
//                    break;
//
//                default:
//                    Log.e(LOGTAG, "    DEFAULT CASE REACHED IN ON-LOAD-FINISHED IN MOV DETAIL FRAGMENT!!");
//
//            } // end switch
//        } // end if(rootView != null...)

    } // end onLoadFinished


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}


    private class FabClickListenerAndDrawableUpdater implements View.OnClickListener {
//        private Cursor cursor;

        private FabClickListenerAndDrawableUpdater() {
//            Log.e(LOGTAG, "just entered FabListener... constructor");

//            // TODO: really all this constructor db code should just be up in onLoadFinished
//            Cursor cursor = getActivity().getContentResolver().query(
//                    MovieTheaterContract.MoviesEntry.CONTENT_URI,
//                    new String[]{ MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE },
//                    MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
//                    new String[]{ String.valueOf(mMovieId) }, null);
//
//            cursor.moveToFirst();
//
//            Log.i(LOGTAG, "  movieId in constructor was: " + mMovieId);
//            Log.i(LOGTAG, "  and before anything else, column is_favorite for that id is: " + cursor.getString(0));
//
//
//            // set the fab drawable depending on if the movie being displayed is already a favorite or not
//            int fabDrawable = cursor.getString(0).equals("true") ?
//                    R.drawable.btn_star_on_normal_holo_light : R.drawable.btn_star_off_normal_holo_light;
//
//            cursor.close();
//
//            Log.i(LOGTAG, "    and fabDrawable id is: " + fabDrawable);
//
//            mFabFavorites.setImageDrawable(getResources().getDrawable(fabDrawable));
        }

        @Override
        public void onClick(View v) {

            Cursor cursor = getActivity().getContentResolver().query(
                    MovieTheaterContract.MoviesEntry.CONTENT_URI,
                    new String[]{ MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE },
                    MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{ String.valueOf(mMovieId) }, null);

            cursor.moveToFirst();

            boolean initialFavState = Boolean.valueOf(cursor.getString(0));
            Log.i(LOGTAG, "  FAB listener.onClick, movieId is: " + mMovieId);
            Log.i(LOGTAG, "    and before anything else, column is_favorite for that id is: " + initialFavState);



            // toggle the fab drawable
            int fabDrawable = initialFavState ?
                    R.drawable.btn_star_off_normal_holo_light : R.drawable.btn_star_on_normal_holo_light;
//            Log.i(LOGTAG, "    and fabDrawable id is: " + fabDrawable);
            mFabFavorites.setImageDrawable(getResources().getDrawable(fabDrawable));

            // update the db
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE, String.valueOf(!initialFavState));
            getActivity().getContentResolver().update(
                    MovieTheaterContract.MoviesEntry.CONTENT_URI,
                    contentValues,
                    MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{ String.valueOf(mMovieId) });

            cursor.close();
//
//            // if db already has value of false for column is_favorite, set isNowFavorite to true
//            boolean isNowFavorite = cursor1.getString(cursor1.getColumnIndex(MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE)).equals("false");
//            Log.e(LOGTAG, "in FabListener.. isNowFavorite (should be toggle of whatever button was before it was clicked) is now: " + isNowFavorite);
//
//            // set fab drawable to the appropriate image
//            int fabDrawable = isNowFavorite ?
//                    R.drawable.btn_star_on_normal_holo_light : R.drawable.btn_star_off_normal_holo_light;
//            mFabFavorites.setImageDrawable(getResources().getDrawable(fabDrawable));
//
//            // create a one column ContentValues to use top update the db
//            ContentValues cv = new ContentValues();
//            Log.i(LOGTAG, "  String.valueOf(isNowFavorites): " + String.valueOf(isNowFavorite));
//            cv.put(MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE, String.valueOf(isNowFavorite));
//
//            // update db, in this case the provider takes care of selection and selection args
//            // it grabs them from the URI
//            int numUpdated = getActivity().getContentResolver().update(
//                    MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
//                    cv, null, null);
//
//
//
//            String newIsFavorite = cursor1.getString(cursor1.getColumnIndex(MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE));
//
//            cursor1.close();

//
//            Log.i(LOGTAG, "    after updating db, column is_favorite is now: " + newIsFavorite);
//
//
//            Log.i(LOGTAG, "  and num records updated (should be 1) was: " + numUpdated);

        }
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
//            Log.i(LOGTAG, "just entered FetchMovieDetailsTask.doInBackground");
            return new MovieDetailsFetcher(context, mMovieId).fetchMovieDetails();
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.i(LOGTAG,"in FetchMovieDetailsTask.onPostExecute, about to restart the Loader");

            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, loaderCallbacks);
            getLoaderManager().restartLoader(CREDITS_LOADER_ID, null, loaderCallbacks);
            getLoaderManager().restartLoader(REVIEWS_LOADER_ID, null, loaderCallbacks);
            getLoaderManager().restartLoader(VIDEOS_LOADER_ID, null, loaderCallbacks);
        }
    }


    // TODO: clean this mess up
    private void fireFetchDetailsTaskIfNecessary() {

        if(!mUseFavorites) {

            String selectMovieIdAndIsFav = "movie_id = ? AND is_favorite = ?";
            String[] selectionArgs =
                    new String[]{ String.valueOf(mMovieId), "false" };

            Cursor cursorCredits = getActivity().getContentResolver().query(
                    MovieTheaterContract.CreditsEntry.CONTENT_URI,
                    new String[] {MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID}, // only need to check for existence of movie_id here
                    selectMovieIdAndIsFav,
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
                        selectMovieIdAndIsFav,
                        selectionArgs,
                        null);
                Log.i(LOGTAG, "    cursorVideos.getCount: " + cursorVideos.getCount());

                if(cursorVideos != null && cursorVideos.getCount() == 0) {
                    Cursor cursorReviews = getActivity().getContentResolver().query(
                            MovieTheaterContract.ReviewsEntry.CONTENT_URI,
                            new String[] {MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID},
                            selectMovieIdAndIsFav,
                            selectionArgs,
                            null);
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
