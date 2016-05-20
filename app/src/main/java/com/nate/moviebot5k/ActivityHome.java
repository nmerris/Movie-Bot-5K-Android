package com.nate.moviebot5k;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;


public class ActivityHome extends ActivitySingleFragment
    implements FragmentMovieGrid.Callbacks, FragmentMovieFiltersSpinner.Callbacks,
        FragmentMovieDetails.Callbacks {
    private final String LOGTAG = N8LOG + "ActivityHome";


    // see ActivitySingleFragment
    @Override
    protected Fragment createFragment() {
        Log.i(LOGTAG, "entered createFragment, about to return a NEW FragmentMovieGrid to ActivitySingleFragment");

        // ActivityHome never shows movies from the favorites table, see FavoritesActivity
        // phone and tablet mode both always have a FragmentMovieGrid, so no need to check here
        // ActivitySingleFragment will just put FragmentMovieGrid in fragment_container

        // NOTE: MGF always just checks sharePrefs for fetch_new_movies boolean when it is created,
        // so there is no need to pass it any arguments to tell it if it should or should not do a fetch
        return FragmentMovieGrid.newInstance(false);

    }


    // see ActivitySingleFragment
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }



    @Override
    public void onMovieSelected(int movieId) {
        Log.i(LOGTAG, "entered onMovieSelected");

        if(mTwoPane) {
            // in tablet mode, replace the movie detail fragment, which is in the second pane,
            // and instruct it to not use the favorites table
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(false, movieId)).commit();
        }
        else {
            // in phone mode, launch an intent to movie details pager activity
            // the movie to show has already been stored in sharedPrefs key currently_selected_movie_id
            // so there is no need for an intent extra
            Intent intent = new Intent(this, ActivityMovieDetailsPager.class);
            startActivity(intent);
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
                FragmentMovieGrid.newInstance(false)).commit();


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
        Log.i(LOGTAG, "entered onCreate");

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
        Log.i(LOGTAG, "entered onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch(id) {
            case R.id.action_favorites:

                // TODO: launch intent to FavoritesActivity

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
            TextView movieTitleTextView = (TextView) findViewById(R.id.toolbar_movie_title);
            TextView movieTaglineTextView = (TextView) findViewById(R.id.toolbar_movie_tagline);
            movieTitleTextView.setText(movieTitle);
            movieTaglineTextView.setText(movieTagline);
        }
    }

}
