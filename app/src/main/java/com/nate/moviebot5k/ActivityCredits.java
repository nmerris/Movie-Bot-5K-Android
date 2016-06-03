package com.nate.moviebot5k;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Nathan Merris on 5/10/2016.
 *
 */
public class ActivityCredits extends ActivitySingleFragment {

    private static final String BUNDLE_INCOMING_INTENT = "bundle_intent";
    private static final String KEY_MOVIE_ID = "movie_id";

    public static Intent newIntent(Context context, int movieId) {
        Intent intent = new Intent(context, ActivityCredits.class);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_MOVIE_ID, movieId);
        intent.putExtra(BUNDLE_INCOMING_INTENT, bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getSupportActionBar().setTitle(getString(R.string.credits_activity_label));
        } catch (NullPointerException npe){
            // nothing here, just need to handle this npe when in phone landscape mode, which is
            // fullscreen without an action bar
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_credits_ref;
    }

    @Override
    protected Fragment createFragment() {
        int movieId = getIntent().getBundleExtra(BUNDLE_INCOMING_INTENT).getInt(KEY_MOVIE_ID);
        return FragmentCredits.newInstance(movieId);
    }

}