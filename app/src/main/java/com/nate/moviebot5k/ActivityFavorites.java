package com.nate.moviebot5k;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Similar to ActivityHome, but only loads movie grid and detail fragments for favorites movies.
 * Additionally, all the spinners are gone because there is no network activity when in favorites
 * mode (ie when user is in this Activity).  However, there is still some ability to sort in here:
 * the users list of movies can be sorted by revenue, release date, and popularity.  The sorting
 * is a menu action button here, mostly for space reasons, but also to avoid confusion with the
 * spinners in HomeActivity.  StartupActivity will send the user straight to here if it detects
 * no connection to themoviedb server.
 *
 * @see FragmentMovieGrid
 * @see FragmentMovieDetails
 */
public class ActivityFavorites extends ActivitySingleFragment
        implements FragmentMovieGrid.Callbacks, FragmentMovieDetails.Callbacks,
        DialogFragmentFavoritesSortby.Callbacks {

    private final String LOGTAG = N8LOG + "ActivityFavs";
    private final String TAG_FAV_SORTBY_DIALOG_FRAGMENT = "favorites_sortby_df";


    @Override
    protected Fragment createFragment() {
        // tell FMG to load ONLY the favorites movies from the db
        return FragmentMovieGrid.newInstance(true, mTwoPane);
    }


    @Override
    protected int getLayoutResourceId() {
        // the layout resource loaded here is basically the same as ActivityHome, except there is
        // no container for the spinner fragment, because there is no spinner fragment
        return R.layout.activity_favorites_ref;
    }


    @Override
    public void onMovieSelected(int movieId, ArrayList<Integer> moviesList) {

        if(mTwoPane) {
            // in tablet mode, replace the movie detail fragment, which is in the second pane,
            // and tell it that it's being hosted by ActivityFavorites (the only thing that does is
            // tell it NOT to fire an api fetch task and instead just initialize the loader with the
            // movieId that is being passed to it)
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(true, movieId, true)).commit();
        }
        else {
            // in phone mode, launch an intent to movie details pager activity
            // and add in to the bundle: the movieId to display, the current list of movies in the grid,
            // and tell it that ActivityFavorites is hosting it.. it needs to know that favorites
            // activity is hosting it so that it can tell movie detail fragment NOT to make any api
            // fetch calls, just like above with the fragment txn
            startActivity(ActivityMovieDetailsPager.newIntent(this, moviesList, true, movieId));
        }
    }
    
    
    @Override
    public void onGridLoaded(ArrayList<Integer> moviesList) {
        // this does something only when ActivityHome hosts FragmentMovieGrid
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Favorites");

        // in tablet mode, it is nice to load the last viewed movie detail fragment in to the second
        // pane, but only if there is at least 1 favorite saved, note that saveInstanceState does
        // not matter here because orientation is fixed at landscape on tablet devices
        if(mTwoPane) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int numFavorites = sharedPrefs.getInt(getString(R.string.key_num_favorites), 0);

            if(numFavorites > 0) {
                // if there is more than 1 favorites saved, then currSelectedFavoriteId is
                // guaranteed to be valid as that is taken care of in FabClickListener
                int currSelectedFavoriteId = sharedPrefs.getInt(getString(R.string.key_currently_selected_favorite_id), 0);
                mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                        FragmentMovieDetails.newInstance(true, currSelectedFavoriteId, true)).commit();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // hide the favorites and about app menu items
        menu.findItem(R.id.action_favorites).setVisible(false);
        menu.findItem(R.id.action_about_app).setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_sort_favorites:
                // launch a custom dialog fragment to allow user to select how they would like
                // to sort their favorites.. this could be really useful if you have a large list
                // of favorites and, for example, you might want to know which one had the
                // lowest or highest revenue
                new DialogFragmentFavoritesSortby().show(getSupportFragmentManager(),
                        TAG_FAV_SORTBY_DIALOG_FRAGMENT);

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onUpdateToolbar(String movieTitle, String movieTagline) {
        // update the toolbar, but only if the details toolbar is present, which is only in tablet mode
        if(mTwoPane) {
            Utility.updateToolbarTitleAndTagline(this, movieTitle, movieTagline);
        }
    }


    @Override
    public void onFavoriteSortbyChanged() {
        // sharedPrefs have already been updated in DialogFragmentFavoritesSortby, so now just
        // need to replace the movie grid fragment.. it will read the sortby value from sharedPrefs
        // and query the db tables as appropriate
        mFragmentManager.beginTransaction().replace(R.id.fragment_container,
        FragmentMovieGrid.newInstance(true, mTwoPane)).commit();
    }

}
