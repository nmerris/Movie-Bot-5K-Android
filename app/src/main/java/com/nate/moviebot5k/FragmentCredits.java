package com.nate.moviebot5k;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nate.moviebot5k.adapters.CreditsAdapter;
import com.nate.moviebot5k.data.MovieTheaterContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Displays a scrolling list of credits, which includes a thumbnail image of the cast member,
 * and there character name in the movie.  Picasso is used to load and cache them images.
 */
public class FragmentCredits extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "CreditsFragment";
    
    private static final String BUNDLE_MOVIEID = "movie_id";
    private static final int CREDITS_LOADER_ID = R.id.loader_credits_fragment_credits;
    private int mMovieId;
    private CreditsAdapter mCreditsAdapter;
    
    @Bind(R.id.listview_credits) ListView mListView;
    
    
    /**
     * Call from a hosting Activity to get a new fragment for a fragment transaction.
     *
     * @return new FragmentCredits <code>fragment</code>
     */
    public static FragmentCredits newInstance(int movieId) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_MOVIEID, movieId);
        FragmentCredits fragment = new FragmentCredits();
        fragment.setArguments(args);
        return fragment;
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save the movieId to the outgoing bundle
        outState.putInt(BUNDLE_MOVIEID, mMovieId);
        super.onSaveInstanceState(outState);
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState == null) {
            // get the movieId from the intent that launched this fragment
            mMovieId = getArguments().getInt(BUNDLE_MOVIEID);
        }
        else {
            // or get the movieId from savedInstanceState
            mMovieId = savedInstanceState.getInt(BUNDLE_MOVIEID);
        }
        
    }
    
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CREDITS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_credits, container, false);
        ButterKnife.bind(this, rootView);

        // create a new CreditsAdapter and set it on the listview that was just inflated
        // and bound with ButterKnife
        mCreditsAdapter = new CreditsAdapter(getActivity(), null, 0);
        mListView.setAdapter(mCreditsAdapter);
        return rootView;
    }
    
    
    private final String[] CREDITS_PROJECTION = {
            MovieTheaterContract.CreditsEntry._ID,
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH,
            MovieTheaterContract.CreditsEntry.COLUMN_CHARACTER,
            MovieTheaterContract.CreditsEntry.COLUMN_NAME
    };
    public static final int COL_PROFILE_PATH = 1;
    public static final int COL_CHARACTER = 2;
    public static final int COL_NAME = 3;
    
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // return a Cursor pointed at the correct movieId rows in credits table
        if(id == CREDITS_LOADER_ID) {
            return new CursorLoader(getActivity(),
                    MovieTheaterContract.CreditsEntry.CONTENT_URI,
                    CREDITS_PROJECTION,
                    MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{ String.valueOf(mMovieId) },
                    MovieTheaterContract.CreditsEntry.COLUMN_ORDER + " ASC");
        }

        return null;
    }
    
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // not much going on here in this fragment, it's just a simple vertically scrolling listview
        mCreditsAdapter.swapCursor(data);
    }
    
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCreditsAdapter.swapCursor(null);
    }
    
}
