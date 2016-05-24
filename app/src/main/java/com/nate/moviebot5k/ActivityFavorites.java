package com.nate.moviebot5k;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


// this is very similar to ActivityHome but only shows favorites, and the
public class ActivityFavorites extends ActivitySingleFragment
        implements FragmentMovieGrid.Callbacks, FragmentMovieFiltersSpinner.Callbacks,
        FragmentMovieDetails.Callbacks {
    private final String LOGTAG = N8LOG + "ActivityHome";


    // see ActivitySingleFragment
    @Override
    protected Fragment createFragment() {
//        Log.i(LOGTAG, "entered createFragment, about to return a NEW FragmentMovieGrid to ActivitySingleFragment");

        // tell FMG to load ONLY the favorites movies from the db
        return FragmentMovieGrid.newInstance(true);
    }


    // see ActivitySingleFragment
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }
    // TODO: needs a new layout, spinner block can only have sortby spinner, others don't make sense with favorites
    // prob. have the spinner in the toolbar in phone landscape?



    @Override
    public void onMovieSelected(int movieId, ArrayList<Integer> moviesList) {
        Log.i(LOGTAG, "entered onMovieSelected");

        if(mTwoPane) {
            // in tablet mode, replace the movie detail fragment, which is in the second pane,
            // and instruct it to not use the favorites table
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(true, movieId, true)).commit();
        }
        else {
            // in phone mode, launch an intent to movie details pager activity
            Intent intent = new Intent(this, ActivityMovieDetailsPager.class);

            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList("movie_id_list", moviesList);
            intent.putExtra("bundle_movie_list", bundle);

            startActivity(intent);
        }
    }


    // replace the current FragmentMovieGrid any time a filter parameter has changed
    // NOTE: MGF checks sharedPrefs key fetch_new_movies to see if it should make an API call,
    // so there is no need to pass over a fragment argument in this case
    @Override
    public void onFilterChanged() {
        Log.i(LOGTAG, "entered onFilterChanged, about to REPLACE FragmentMovieGrid");

        // create and REPLACE the movie filter spinner fragment, pass over true so that the
        // favorites table will be used
        mFragmentManager.beginTransaction().replace(R.id.fragment_container,
                FragmentMovieGrid.newInstance(true)).commit();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(LOGTAG, "entered onCreate");

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Favorites");

        // TODO: need to use the smaller spinner fragment here
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

        // hide the favorites menu item
        menu.findItem(R.id.action_favorites).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch(id) {
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
