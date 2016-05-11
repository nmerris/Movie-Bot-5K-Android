package com.nate.moviebot5k;

import android.support.v4.app.Fragment;

/**
 * Created by Nathan Merris on 5/10/2016.
 */
public class MovieFiltersActivity extends SingleFragmentActivity {

    // no need to override getLayoutResourceId because this Activity can only be reached in phone mode,
    // and it only needs to host a fragment in a single FrameLayout, which SingleFragmentActivity provides

    @Override
    protected Fragment createFragment() {
        return new MovieFiltersFragment();
    }

}
