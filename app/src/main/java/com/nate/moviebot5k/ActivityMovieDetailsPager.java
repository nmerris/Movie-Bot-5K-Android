package com.nate.moviebot5k;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A ViewPager that allows the user to swipe left and right when viewing movie details in phone mode.
 * For the viewpager to stay in sync with the movies in the grid from ActivityHome or ActivityFavorites,
 * this class needs the current list of movies from the grid, and which movieId was just clicked.
 * Additionally, this class needs to know if it should tell FragmentMovieDetails to use favorites or not.
 *
 * Created by Nathan Merris on 5/16/2016.
 */
public class ActivityMovieDetailsPager extends AppCompatActivity
        implements FragmentMovieDetails.Callbacks {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovDetPager";

    @Bind(R.id.activity_movie_detail_view_pager) ViewPager mViewPager;

    private int mNumMovies;
    private boolean mUseFavorites;
    ArrayList<Integer> mMovieIds = new ArrayList<>();

    private static final String BUNDLE_INCOMING_INTENT = "bundle_intent";
    private static final String KEY_MOVIE_ID_LIST = "movie_id_list";
    private static final String KEY_USE_FAVORITES = "use_favorites";
    private static final String KEY_MOVIE_ID_CLICKED = "movie_id_just_clicked";


    /**
     * Builds a new Intent with all necessary extras required for the ViewPager to function.
     * The order of the movies in the list passed in must be the same as the order in which the
     * movies are shown in the movie grid GridView, or the page swiping will be out of sync.
     * Note that this activity is used to view both favorites and non-favorites.
     *
     * @param movieIds The list of currently showing movieIds in the movie grid
     * @param useFavorites True if movie details for favorites movies only should be shown
     * @param selectedMovieId The movieId that user just clicked to launch this intent
     * @return An intent ready for ActivityMovieDetailsPager to process
     */
    public static Intent newIntent(Context context, ArrayList<Integer> movieIds,
                                   boolean useFavorites, int selectedMovieId) {
        Intent intent = new Intent(context, ActivityMovieDetailsPager.class);
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(KEY_MOVIE_ID_LIST, movieIds);
        bundle.putBoolean(KEY_USE_FAVORITES, useFavorites);
        bundle.putInt(KEY_MOVIE_ID_CLICKED, selectedMovieId);
        intent.putExtra(BUNDLE_INCOMING_INTENT, bundle);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_details_pager);
        ButterKnife.bind(this);

        // in this activity, the details toolbar is also the action bar, unlike elsewhere,
        // so set the title appropriately
        Toolbar actionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(!mUseFavorites) {
            getSupportActionBar().setTitle(getString(R.string.movie_details_pager_activity_label));
        } else {
            getSupportActionBar().setTitle(getString(R.string.movie_details_pager_activity_favorites_label));
        }

        // get the intent bundle and grab all it's datumilicious goodies
        Bundle bundle = getIntent().getBundleExtra(BUNDLE_INCOMING_INTENT);
        mMovieIds = bundle.getIntegerArrayList(KEY_MOVIE_ID_LIST);
        mUseFavorites = bundle.getBoolean(KEY_USE_FAVORITES);
        int mMovieJustClicked = bundle.getInt(KEY_MOVIE_ID_CLICKED);
        mNumMovies = mMovieIds.size();

        // set a pager adapter on the view pager
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                // this is why the list of movieIds has to be in the same order as they are in the
                // movie poster GridView
                mMovieIds.get(position);
                int movieId = mMovieIds.get(position);
                return FragmentMovieDetails.newInstance(mUseFavorites, movieId, false);
            }

            @Override
            public int getCount() {
                return mNumMovies;
            }

        });

        // set the viewpager to start on the movie that was clicked back in FragmentMovieGrid
        for(int i = 0; i < mNumMovies; i++) {
            if(mMovieIds.get(i) == mMovieJustClicked) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }

    @Override
    public void onUpdateToolbar(String movieTitle, String movieTagline) {
        // had a heckuva time getting this to work with the view pager, sorry to say I had to give
        // up as this project has already taken quite a lot of time
    }

}
