package com.nate.moviebot5k;

import android.support.v4.app.Fragment;

/**
 * Created by Nathan Merris on 5/10/2016.
 *
 */
public class ActivityAboutApp extends ActivitySingleFragment {

//    private final String LOGTAG = N8LOG + getClass().getSimpleName();

    @Override
    protected Fragment createFragment() {
        return new FragmentAboutApp();
    }

}