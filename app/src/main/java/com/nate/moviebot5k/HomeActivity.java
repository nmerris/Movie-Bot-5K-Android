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
    implements MovieGridFragment.Callbacks {
    private final String LOGTAG = N8LOG + "HomeActivity";


    @Override
    protected Fragment createFragment() {
        Log.i(LOGTAG, "entered createFragment, about to return a NEW MovieGridFragment to SingleFragmentActivity");

        // HomeActivity never shows movies from the favorites table, see FavoritesActivity
        // phone and tablet mode both always have a MovieGridFragment, so no need to check here
        // SingleFragmentActivity will just put MovieGridFragment in fragment_container
        return MovieGridFragment.newInstance(false);

    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "entered onCreate");

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
    public void onMovieSelected(int movieId) {
        Log.i(LOGTAG, "entered onMovieSelected");

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
/*            case R.id.action_movie_filters:
                intent = new Intent(this, MovieFiltersActivity.class);
                startActivity(intent);
                break;*/

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
