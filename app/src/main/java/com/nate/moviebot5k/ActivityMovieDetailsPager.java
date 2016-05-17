package com.nate.moviebot5k;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;

/**
 * Created by Nathan Merris on 5/16/2016.
 */
public class ActivityMovieDetailsPager extends ActivitySingleFragment
        /*implements FragmentMovieDetails.Callbacks*/{

    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovDetPager";


//    @Override
//    protected int getLayoutResourceId() {
//        return R.layout.act
//    }

    @Override
    protected Fragment createFragment() {
        // create a new FMD and pass false to tell it not to use the favorites table
        return FragmentMovieDetails.newInstance(false);

    }


//    @Override
//    public void onFavoriteRemoved(int movieId) {
//        Log.i(LOGTAG, "entered onFavoriteRemoved");
//
//    }


}
