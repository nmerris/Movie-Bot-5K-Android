package com.nate.moviebot5k;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Hosts the 'about app' fragment
 *
 * Created by Nathan Merris on 5/10/2016.
 *
 */
public class ActivityAboutApp extends ActivitySingleFragment {

    @Override
    protected Fragment createFragment() {
        return new FragmentAboutApp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.about_app_activity_label));
    }

}