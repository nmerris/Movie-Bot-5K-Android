package com.nate.moviebot5k;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class HomeActivity extends SingleFragmentActivity
    implements MovieGridFragment.Callbacks, MovieFiltersSpinnerFragment.Callbacks {
    private final String LOGTAG = N8LOG + "HomeActivity";


    // see SingleFragmentActivity
    @Override
    protected Fragment createFragment() {
        Log.i(LOGTAG, "entered createFragment, about to return a NEW MovieGridFragment to SingleFragmentActivity");

        // HomeActivity never shows movies from the favorites table, see FavoritesActivity
        // phone and tablet mode both always have a MovieGridFragment, so no need to check here
        // SingleFragmentActivity will just put MovieGridFragment in fragment_container

        // NOTE: MGF always just checks sharePrefs for fetch_new_movies boolean when it is created,
        // so there is no need to pass it any arguments to tell it if it should or should not do a fetch
        return MovieGridFragment.newInstance(false);

    }


    // see SingleFragmentActivity
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }


    // user selected a movie to see the details, so replace the MovieDetailFragment, passing over
    // the movieId of the movie that was just selected
    @Override
    public void onMovieSelected(int movieId) {
        Log.i(LOGTAG, "entered onMovieSelected");

        // TODO: load new detail fragment with arg movieId

    }


    // replace the current MovieGridFragment any time a filter parameter has changed
    // NOTE: MGF checks sharedPrefs key fetch_new_movies to see if it should make an API call,
    // so there is no need to pass over a fragment argument in this case
    @Override
    public void onFilterChanged() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");

        // create a TOOLBAR, which is NOT the same as an Actionbar aka Appbar, however, we can
        // use it just like an old action bar by calling setSupportActionBar
        // a toolbar is much more flexible and customizable than an action bar
        // all cool coders use Toolbars
        Toolbar actionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBarToolbar);

        // create and add the movie filter spinner fragment if necessary
        Fragment spinnerfragment = mFragmentManager.findFragmentById(R.id.filter_spinner_container);
        if (spinnerfragment == null) {
            spinnerfragment = new MovieFiltersSpinnerFragment();
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
                intent = new Intent(this, AboutAppActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


}
