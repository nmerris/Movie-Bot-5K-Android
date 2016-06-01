package com.nate.moviebot5k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * The starting point for the user if ActivityStartup does not encounter any problems when it is
 * running through it's initialization procedures.  Any fragments being hosted by this activity
 * can assume that a network connection is available.  Of course if the user looses network while
 * in the middle of using this app, maybe in a tunnel, all fragments are designed to handle that and
 * report an appropriate msg to the user.
 * <br><br>
 *     FragmentMovieGrid is loaded by ActivitySingleFragment by overriding createFragment
 * <br>
 *     Fragment MovieFiltersSpinner is loaded in onCreate
 * <br>
 *     FragmentMovieDetails is loaded in onGridLoaded callback, if app is in tablet mode
 *
 * @see FragmentMovieGrid
 * @see FragmentMovieDetails
 * @see FragmentMovieFiltersSpinner
 */
public class ActivityHome extends ActivitySingleFragment
    implements FragmentMovieGrid.Callbacks, FragmentMovieFiltersSpinner.Callbacks,
        FragmentMovieDetails.Callbacks {

    private final String LOGTAG = N8LOG + "ActivityHome";


    @Override
    protected Fragment createFragment() {
        // ActivityHome never shows movies from the favorites table.
        // phone and tablet mode both always have a FragmentMovieGrid, so no need to check here
        // ActivitySingleFragment will just put FragmentMovieGrid in fragment_container

        // NOTE: MGF always just checks sharePrefs for fetch_new_movies boolean when it is created,
        // so there is no need to pass it any arguments to tell it if it should or should not do a fetch
        // it does need to know if it should use favorites and if app is running in tablet mode
        return FragmentMovieGrid.newInstance(false, mTwoPane);
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }


    @Override
    public void onMovieSelected(int movieId, ArrayList<Integer> moviesList) {
        Log.i(LOGTAG, "entered onMovieSelected, movieId passed in was: " + movieId);

        if(mTwoPane) {
            // in tablet mode, replace the movie detail fragment, which is in the second pane,
            // and instruct it to not use the favorites table
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(false, movieId, true)).commit();
        }
        else {
            // in phone mode.. much more complicated: start a details pager activity and pass it
            // the list of movieIds in the grid (order matters for view pager),
            // tell it not to use the favorites table,
            // and tell it what movieId was just clicked
            startActivity(ActivityMovieDetailsPager.newIntent(this, moviesList, false, movieId));
        }
    }


    @Override
    public void onGridLoaded(ArrayList<Integer> moviesList) {
        // in tablet mode there is both a movie grid and movie details fragment on screen at the same
        // time, and we want to have a movie detail load even before the user clicks on a movie
        // poster from the grid.  First choice is to have the users last clicked movie be presented
        // in the details pane, but it's possible that the user had changed the filter spinners and
        // then NOT clicked on any movies, so it's possible that the last clicked movie will not be
        // in the movie grid, which would be confusing, so in that case just default to loading
        // the first movie from the grid in the second pane
        if(mTwoPane && moviesList.size() > 0) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int currSelectedMovieId = sharedPrefs.getInt(getString(R.string.key_currently_selected_movie_id), 0);

            for (int movieId : moviesList) {
                if(movieId == currSelectedMovieId) {
                    mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                            FragmentMovieDetails.newInstance(false, movieId, true)).commit();
                    return;
                }
            }
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(false, moviesList.get(0), true)).commit();
        }

    }



    @Override
    public void onFilterChanged() {
        // replace the current FragmentMovieGrid any time a filter parameter has changed
        // NOTE: MGF checks sharedPrefs key fetch_new_movies to see if it should make an API call,
        // so there is no need to pass over a fragment argument in this case
        mFragmentManager.beginTransaction().replace(R.id.fragment_container,
                FragmentMovieGrid.newInstance(false, mTwoPane)).commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // create and ADD the movie filter spinner fragment if necessary
        Fragment spinnerfragment = mFragmentManager.findFragmentById(R.id.filter_spinner_container);
        if (spinnerfragment == null) {
            spinnerfragment = new FragmentMovieFiltersSpinner();
            mFragmentManager.beginTransaction().add(R.id.filter_spinner_container, spinnerfragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_sort_favorites).setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.action_favorites:
                // no bundle needed here because ActivityFavorites always just loads a new
                // movie grid fragment with useFavorites arg = true
                intent = new Intent(this, ActivityFavorites.class);
                startActivity(intent);
                break;

            case R.id.action_about_app:
                intent = new Intent(this, ActivityAboutApp.class);
                startActivity(intent);
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

}
