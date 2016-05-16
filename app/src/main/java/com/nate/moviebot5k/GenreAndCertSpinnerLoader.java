package com.nate.moviebot5k;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * Created by Nathan Merris on 5/13/2016.
 */
public class GenreAndCertSpinnerLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "GenreSpnnerLoader";


    private Context mContext;
    private SharedPreferences mSharedPrefs;
    private SimpleCursorAdapter mGenreSpinnerAdapter, mCertSpinnerAdapter;
    private Spinner mGenreSpinner, mCertSpinner;

    // these loader ID's must not conflict with the ID's in MovieGridFragment!
    // because the same loader manager is used for all that fragment's loading
    private static final int GENRES_TABLE_LOADER_ID = 10;
    private static final int CERTS_TABLE_LOADER_ID = 20;

    // genresProjection and the ints that follow must be changed together, order matters
    public static final String[] genresProjection = {
            MovieTheaterContract.GenresEntry._ID,
            MovieTheaterContract.GenresEntry.COLUMN_GENRE_ID,
            MovieTheaterContract.GenresEntry.COLUMN_GENRE_NAME
    };
    public static final int GENRE_TABLE_COLUMN_ID = 0;
    public static final int GENRE_TABLE_COLUMN_GENRE_ID = 1;
    public static final int GENRE_TABLE_COLUMN_NAME = 2;

    // certsProjection and the ints that follow must be changed together, order matters
    public static final String[] certsProjection = {
            MovieTheaterContract.CertsEntry._ID,
            MovieTheaterContract.CertsEntry.COLUMN_CERT_ORDER,
            MovieTheaterContract.CertsEntry.COLUMN_CERT_NAME
    };
    public static final int CERTS_TABLE_COLUMN_ID = 0;
    public static final int CERTS_TABLE_COLUMN_CERTS_ORDER = 1;
    public static final int CERTS_TABLE_COLUMN_NAME = 2;



    public GenreAndCertSpinnerLoader(AdapterView.OnItemSelectedListener itemSelectedListener,
                                     Context context, SimpleCursorAdapter genreAdapter,
                                     SimpleCursorAdapter certAdapter, Spinner genreSpinner, Spinner certSpinner,
                                     LoaderManager loaderManager) {
//        mItemSelectedListener = itemSelectedListener;
//        mContext = context;
//        mGenreSpinnerAdapter = genreAdapter;
//        mCertSpinnerAdapter = certAdapter;
//        mGenreSpinner = genreSpinner;
//        mCertSpinner = certSpinner;
//        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        loaderManager.initLoader(GENRES_TABLE_LOADER_ID, null, this);
//        loaderManager.initLoader(CERTS_TABLE_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOGTAG, "entered onCreateLoader");

//        if(id == GENRES_TABLE_LOADER_ID) {
//            Log.i(LOGTAG, "  and about to return new GENRES_TABLE_LOADER");
//
//            return new CursorLoader(mContext,
//                    MovieTheaterContract.GenresEntry.CONTENT_URI,
//                    genresProjection,
//                    // the order in which the genres are listed doesn't matter
//                    // in any case they are returned in alphabetical order from themoviedb
//                    null, null, null);
//        }
//        else if(id == CERTS_TABLE_LOADER_ID) {
//            Log.i(LOGTAG, "  and about to return new CERTS_TABLE_LOADER");
//
//            return new CursorLoader(mContext,
//                    MovieTheaterContract.CertsEntry.CONTENT_URI,
//                    certsProjection,
//                    null, null,
//                    // here we  want the proper order, ie NR, G, PG, PG-13, etc
//                    MovieTheaterContract.CertsEntry.COLUMN_CERT_ORDER + " ASC");
//        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "entered onLoadFinished");

//        if(loader.getId() == GENRES_TABLE_LOADER_ID) {
//            mGenreSpinnerAdapter.swapCursor(data);
//
//            // must wait until load finished to setSelection in spinner, because the
//            // onItemSelected spinner callback fires immediately when it's set up, and if
//            // the selected item is not in the list yet (because the loader has not returned the
//            // cursor which contains the spinner drop down elements), the onItemSelected method
//            // will think that the user changed the selection, while will trigger an unnecessary
//            // API call when user navigates back to HomeActivity.. details details
//            mGenreSpinner.setSelection(mSharedPrefs.
//                    getInt(mContext.getString(R.string.key_movie_filter_genre_spinner_position), 0));
//            // I don't think it matters if setOnItemSelectedListener is here
//            mGenreSpinner.setOnItemSelectedListener(mContext);
//        }
//        else if (loader.getId() == CERTS_TABLE_LOADER_ID) {
//            mCertSpinnerAdapter.swapCursor(data);
//            mCertSpinner.setSelection(mSharedPrefs.
//                    getInt(mContext.getString(R.string.key_movie_filter_cert_spinner_position), 0));
//            mCertSpinner.setOnItemSelectedListener(new SpinnerListener(mContext));
//        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        if(loader.getId() == GENRES_TABLE_LOADER_ID) {
//            mGenreSpinnerAdapter.swapCursor(null);
//        }
//        else if(loader.getId() == CERTS_TABLE_LOADER_ID) {
//            mCertSpinnerAdapter.swapCursor(null);
//        }
    }
}
