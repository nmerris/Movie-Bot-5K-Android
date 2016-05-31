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

import org.w3c.dom.Text;

import java.util.ArrayList;


public class ActivityHome extends ActivitySingleFragment
    implements FragmentMovieGrid.Callbacks, FragmentMovieFiltersSpinner.Callbacks,
        FragmentMovieDetails.Callbacks {
    private final String LOGTAG = N8LOG + "ActivityHome";


    // see ActivitySingleFragment
    @Override
    protected Fragment createFragment() {
//        Log.i(LOGTAG, "entered createFragment, about to return a NEW FragmentMovieGrid to ActivitySingleFragment");

        // ActivityHome never shows movies from the favorites table, see FavoritesActivity
        // phone and tablet mode both always have a FragmentMovieGrid, so no need to check here
        // ActivitySingleFragment will just put FragmentMovieGrid in fragment_container

        // NOTE: MGF always just checks sharePrefs for fetch_new_movies boolean when it is created,
        // so there is no need to pass it any arguments to tell it if it should or should not do a fetch
        return FragmentMovieGrid.newInstance(false, mTwoPane);

    }


    // see ActivitySingleFragment
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }



    @Override
    public void onMovieSelected(int movieId, ArrayList<Integer> moviesList) {
        Log.i(LOGTAG, "entered onMovieSelected");

        if(mTwoPane) {
            // in tablet mode, replace the movie detail fragment, which is in the second pane,
            // and instruct it to not use the favorites table
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(false, movieId, true)).commit();
        }
        else {

            Intent intent = new Intent(this, ActivityMovieDetailsPager.class);

            // need the current list of movies, in the same order as in the db, for view pager to work
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList("movie_id_list", moviesList);
            bundle.putBoolean("use_favorites", false);
            bundle.putInt("movie_id_just_clicked", movieId);
            intent.putExtra("bundle_movie_list", bundle);

            startActivity(intent);
        }
    }


    @Override
    public void onGridLoaded(ArrayList<Integer> moviesList) {

        if(mTwoPane && moviesList.size() > 0) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int currSelectedMovieId = sharedPrefs.getInt(getString(R.string.key_currently_selected_movie_id), 0);

            if(currSelectedMovieId == -1) {
                mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                        FragmentMovieDetails.newInstance(true, moviesList.get(0), true)).commit();
            }
            else {
                for (int movieId : moviesList) {
                    if(movieId == currSelectedMovieId) {
                        mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                                FragmentMovieDetails.newInstance(true, movieId, true)).commit();
                    }
                }
            }
        }

    }


    // replace the current FragmentMovieGrid any time a filter parameter has changed
    // NOTE: MGF checks sharedPrefs key fetch_new_movies to see if it should make an API call,
    // so there is no need to pass over a fragment argument in this case
    @Override
    public void onFilterChanged() {
        Log.i(LOGTAG, "entered onFilterChanged, about to REPLACE FragmentMovieGrid");

        // create and REPLACE the movie filter spinner fragment, pass over false so that the
        // favorites table will not be used
        mFragmentManager.beginTransaction().replace(R.id.fragment_container,
                FragmentMovieGrid.newInstance(false, mTwoPane)).commit();


    }


//    // callback from FragmentMovieDetails
//    @Override
//    public void onFavoriteRemoved(int movieId) {
//        Log.i(LOGTAG, "entered onFavoriteRemoved");
//
//
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(LOGTAG, "entered onCreate");

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
//        Log.i(LOGTAG, "entered onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu, menu);

        menu.findItem(R.id.action_sort_favorites).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch(id) {
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
            Log.e(LOGTAG, "just in onUpdateToolbar, movieTitle passed in: " + movieTitle +
            " and tagline: " + movieTagline);

            TextView movieTitleTextView = (TextView) findViewById(R.id.toolbar_movie_title);
            TextView movieTaglineTextView = (TextView) findViewById(R.id.toolbar_movie_tagline);
            movieTitleTextView.setText(movieTitle);
            movieTaglineTextView.setText(movieTagline);
        }
    }

}
