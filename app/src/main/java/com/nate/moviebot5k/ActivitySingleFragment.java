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
// necessarily needed, because that will allow the app to address Google API bugs between
// full releases, because Google publishes support library revisions more often than full OS
// version updgrades.  Addressing Google bugs would simply be a matter of repackaging and shipping
// out the same app, but with updated support libraries in the APK.


/**
 * A convenience class that prevents subclassing Activities from having to contain the same
 * FragmentManager code repeatedly, when loading a fragment into an Activity that hosts at least one single
 * fragment in a simple FrameLayout.
 *
 * <p>
 * In some cases, the subclassing Activity many not even need
 * to override onCreate at all.  Note that MenuDetailPagerActivity does not subclass this because
 * it needs it's FrameLayout to be wrapped in a ViewPager.
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
 * has only one container for one fragment.
 *
 * <p>
 * Note: MenuActivity extends AppCompatActivity
 * </p>
 *
 * @author Nathan Merris
 */
public abstract class ActivitySingleFragment extends AppCompatActivity
{

    public static final String N8LOG = "N8LOG "; // logtag prefix to use for entire app
    private final String LOGTAG = N8LOG + "SingleFragmentAct";
    boolean mTwoPane; // true if subclassing activity is hosting a dual pane layout
    FragmentManager mFragmentManager;

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
        //Log.i(LOGTAG, "just entered onCreate()");

        displayScreenDP();

        // load the layout resource, subclassing Activity may or may not provide it's own
        setContentView(getLayoutResourceId());

        // create a TOOLBAR, which is NOT the same as an Actionbar aka Appbar, however, we can
        // use it just like an old action bar by calling setSupportActionBar
        // a toolbar_movie_grid is much more flexible and customizable than an action bar
        // all cool coders use Toolbars
        Toolbar actionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBarToolbar);

        mTwoPane = findViewById(R.id.container_second_pane) != null;

        mFragmentManager = getSupportFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);

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

    public void displayScreenDP() {
        // TESTING: just want to see what screen dp of device is..
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        //Log.i(LOGTAG, "just entered onCreate");
//        Log.i(LOGTAG, "==== screen dpWidth is: " + dpWidth + ", and dpHeight is: " + dpHeight + " ====");
    }


}
