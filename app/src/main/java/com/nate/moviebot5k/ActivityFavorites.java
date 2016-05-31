package com.nate.moviebot5k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


// this is very similar to ActivityHome but only shows favorites, and the
public class ActivityFavorites extends ActivitySingleFragment
        implements FragmentMovieGrid.Callbacks, FragmentMovieDetails.Callbacks,
        DialogFragmentFavoritesSortby.Callbacks {
    private final String LOGTAG = N8LOG + "ActivityFavs";

    private final String TAG_FAV_SORTBY_DIALOG_FRAGMENT = "favorites_sortby_df";


    // see ActivitySingleFragment
    @Override
    protected Fragment createFragment() {
//        Log.i(LOGTAG, "entered createFragment, about to return a NEW FragmentMovieGrid to ActivitySingleFragment");

        // tell FMG to load ONLY the favorites movies from the db
        return FragmentMovieGrid.newInstance(true, mTwoPane);
    }


    // see ActivitySingleFragment
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_favorites_ref;
    }


    @Override
    public void onMovieSelected(int movieId, ArrayList<Integer> moviesList) {
        Log.i(LOGTAG, "entered onMovieSelected");

        if(mTwoPane) {
            // in tablet mode, replace the movie detail fragment, which is in the second pane,
            // and tell it that it's being hosted by ActivityFavorites (the only thing that does is
            // tell it NOT to fire an api fetch task and instead just initialize the loader with the
            // movieId that is being passed to it)
            mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                    FragmentMovieDetails.newInstance(true, movieId, true)).commit();
        }
        else {

            Log.e(LOGTAG, "  and movieId passed in was: " + movieId);

            // in phone mode, launch an intent to movie details pager activity
            // and add in to the bundle: the movieId to display, the current list of movies in the grid,
            // and tell it that ActivityFavorites is hosting it.. it needs to know that favorties
            // activity is hosting it so that it can too movie detail fragment NOT to make any api
            // fetch calls, just like above with the fragment txn
            Intent intent = new Intent(this, ActivityMovieDetailsPager.class);

            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList("movie_id_list", moviesList);
            bundle.putBoolean("use_favorites", true);
            bundle.putInt("movie_id_just_clicked", movieId);
            intent.putExtra("bundle_movie_list", bundle);

            startActivity(intent);
        }
    }
    
    
    @Override
    public void onGridLoaded(ArrayList<Integer> moviesList) {

//        if(mTwoPane && moviesList.size() > 0) {
//            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//            int currSelectedFavoriteId = sharedPrefs.getInt(getString(R.string.key_currently_selected_favorite_id), 0);
//
//            if(currSelectedFavoriteId == -1) {
//                mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
//                        FragmentMovieDetails.newInstance(true, moviesList.get(0), true)).commit();
//            }
//            else {
//                for (int movieId : moviesList) {
//                    if(movieId == currSelectedFavoriteId) {
////                        mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
////                                FragmentMovieDetails.newInstance(true, movieId, true)).commit();
//                        mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
//                                FragmentMovieDetails.newInstance(true, movieId, true)).commit();
//                    }
//                }
//            }
//        }
        
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(LOGTAG, "entered onCreate");

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Favorites");

        if(mTwoPane) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int numFavorites = sharedPrefs.getInt(getString(R.string.key_num_favorites), 0);

            Log.e(LOGTAG, "  ^^^^^^^^^^^ in onCreate, numFavorites: " + numFavorites);

            if(numFavorites > 0) {
                // if there is more than 1 favorites saved, then currSelectedFavoriteId is
                // guaranteed to be valid as that is taken care of in FabClickListener
                int currSelectedFavoriteId = sharedPrefs.getInt(getString(R.string.key_currently_selected_favorite_id), 0);
                Log.i(LOGTAG, "  and currSelectedFavId: " + currSelectedFavoriteId);

                mFragmentManager.beginTransaction().replace(R.id.container_second_pane,
                        FragmentMovieDetails.newInstance(true, currSelectedFavoriteId, true)).commit();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Log.i(LOGTAG, "entered onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu, menu);

        // hide the favorites menu item
        menu.findItem(R.id.action_favorites).setVisible(false);
        menu.findItem(R.id.action_about_app).setVisible(false);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
//            case R.id.action_about_app:
//                intent = new Intent(this, ActivityAboutApp.class);
//                startActivity(intent);

            case R.id.action_sort_favorites:
                new DialogFragmentFavoritesSortby().show(getSupportFragmentManager(),
                        TAG_FAV_SORTBY_DIALOG_FRAGMENT);

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onUpdateToolbar(String movieTitle, String movieTagline) {

        // update the toolbar, but only if the details toolbar is present, which is only in tablet mode
        if(mTwoPane) {
            Log.e(LOGTAG, "just in onUpdateToolbar, movieTitle passed in: " + movieTitle +
                    " and tagline: " + movieTagline);

            // set the title or title and tagline in the action bar, depending on if the movie
            // in question acutally has tagline data stored in the db.. seems about 80% have taglines
            TextView movieTitleTextView = (TextView) findViewById(R.id.toolbar_movie_title);
            TextView movieTaglineTextView = (TextView) findViewById(R.id.toolbar_movie_tagline);
            if(movieTagline == null) {
                movieTaglineTextView.setVisibility(View.GONE);
                movieTitleTextView.setText(movieTitle);
            }
            else {
                movieTaglineTextView.setVisibility(View.VISIBLE);
                movieTitleTextView.setText(movieTitle);
                movieTaglineTextView.setText(movieTagline);
            }
        }
    }


    @Override
    public void onFavoriteSortbyChanged() {
        // sharedPrefs have already been updated in DialogFragmentFavoritesSortby, so now just
        // need to replace the movie grid fragment.. it will read the sortby value from sharedPrefs
        // and query the db tables as appropriate
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
        FragmentMovieGrid.newInstance(true, mTwoPane)).commit();

    }

}
