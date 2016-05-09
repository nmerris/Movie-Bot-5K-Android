package com.nate.moviebot5k;

import android.app.LoaderManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

public class HomeActivity extends SingleFragmentActivity {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "HomeActivity";

    @Override
    protected Fragment createFragment() {
        Log.i(LOGTAG, "entered createFragment, about to return a NEW MovieGridFragment to SingleFragmentActivity");

        // HomeActivity never shows movies from the favorites table, see FavoritesActivity
        // phone and tablet mode both always have a MovieGridFragment, so no need to check here
        // SingleFragmentActivity will just put it in fragment_container
        return MovieGridFragment.newInstance(true);

    }

    // will return either activity_single_fragment or activity_master_detail depending on min screen width
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home_ref;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");

    }
}
