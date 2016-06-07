package com.nate.moviebot5k;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;

// I am attempting a min SDK of 11.. thus all the support libraries
// I have also read that it is good practice to use support libraries when possible, even if not
// necessarily needed, because that will allow the app to address Google API bug fixes between
// full releases, because Google publishes support library revisions more often than full OS
// version upgrades.


/**
 * A convenience class that prevents subclassing Activities from having to contain the same
 * FragmentManager code repeatedly, when loading a fragment into an Activity that hosts at least one single
 * fragment in a simple FrameLayout.  It also makes a couple universally necessary members available
 * to all subclassing Activities: mTwoPane and mFragmentManager.
 * Finally, the movie grid toolbar is inflated here.  Note that when this app is running in tablet
 * mode, a second toolbar is added above the movie details fragment pane.
 *
 * <p>
 * In some cases, the subclassing Activity may not even need
 * </p>
 *
 * <p>
 * Each subclassing Activity must implement createFragment, so this Activity knows what fragment
 * to put in fragment_container.
 * </p>
 *
 * Each subclassing Activity MAY override getLayoutResourceId if it wants to provide it's own
 * layout.  Every subclassing Activity that needs to run fragments in two different containers
 * MUST override getLayoutResourceId, as the default layout that this Activity inflates
 * has only one container for one fragment (with id 'fragment_container')
 *
 * @author Nathan Merris
 */
public abstract class ActivitySingleFragment extends AppCompatActivity
{

    public static final String N8LOG = "N8LOG "; // logtag prefix to use for entire app
    private final String LOGTAG = N8LOG + "SingleFragmentAct";
    boolean mTwoPane; // true if subclassing activity is hosting a dual pane layout
    FragmentManager mFragmentManager; // for use by any subclassing Activity

    /**
     * Loads a Fragment into a simple FrameLayout that fills entire screen for Activities that
     * choose not to override ActivitySingleFragment.getLayoutResourceId.
     *
     * <p>
     * Activities that choose to override getLayoutResourceId must provide a layout resource
     * with a container with id name = 'fragment_container'.  There can be other fragment containers
     * in the provided layout resource, but the fragment returned in createFragment will always
     * be loaded into the container named 'fragment_container'.  Everything else in the layout
     * is ignored by ActivitySingleFragment.
     * </p>
     *
     * @return the <code>Fragment</code> that implementing Activity needs to be loaded into 'fragment_container'
     * @see ActivitySingleFragment#getLayoutResourceId()
     */
    protected abstract Fragment createFragment();


    /**
     * Loads the layout resource provided.  ActivitySingleFragment will look for a container with
     * id name = 'fragment_container' so the layout provided must respect that.  The Fragment that
     * was provided by overriding ActivitySingleFragment.createFragment will be loaded into
     * 'fragment_container'.
     *
     * @return the layout resource id to load
     * @see ActivitySingleFragment#createFragment()
     */
    @LayoutRes
    protected int getLayoutResourceId() {
        return R.layout.activity_single_fragment;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Utility.displayScreenDP(this, LOGTAG);

        // load the layout resource, subclassing Activity may or may not provide it's own
        setContentView(getLayoutResourceId());

        // create a TOOLBAR, which is NOT the same as an Actionbar aka Appbar, however, we can
        // use it just like an old action bar by calling setSupportActionBar
        // a toolbar_movie_grid is much more flexible and customizable than an action bar
        // all cool coders use Toolbars
        Toolbar actionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBarToolbar);

        // if there is a container for a second fragment, app must be running in tablet mode
        mTwoPane = findViewById(R.id.container_second_pane) != null;

        mFragmentManager = getSupportFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);

        // add the fragment to fragment_container
        if (fragment == null) {
            fragment = createFragment();
            mFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

    } // end onCreate()


    @Override
    protected void onResume() {
        // it really works better to just keep the app in landscape on tablet and larger devices
        if(mTwoPane) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onResume();
    }

}
