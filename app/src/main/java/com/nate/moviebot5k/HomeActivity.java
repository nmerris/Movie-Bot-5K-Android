package com.nate.moviebot5k;

import android.support.v4.app.Fragment;
import android.os.Bundle;

public class HomeActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return null;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home_ref;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
