package com.nate.moviebot5k;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * Created by Nathan Merris on 5/13/2016.
 */
public class GenreAndSpinnerLoader implements LoaderManager.LoaderCallbacks<Cursor> {


    public GenreAndSpinnerLoader(Context context, )




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOGTAG, "entered onCreateLoader");

        if(id == GENRES_TABLE_LOADER_ID) {
            Log.i(LOGTAG, "  and about to return new GENRES_TABLE_LOADER");

            return new CursorLoader(getActivity(),
                    MovieTheaterContract.GenresEntry.CONTENT_URI,
                    genresProjection,
                    // the order in which the genres are listed doesn't matter
                    // in any case they are returned in alphabetical order from themoviedb
                    null, null, null);
        }
        else if(id == CERTS_TABLE_LOADER_ID) {
            Log.i(LOGTAG, "  and about to return new CERTS_TABLE_LOADER");

            return new CursorLoader(getActivity(),
                    MovieTheaterContract.CertsEntry.CONTENT_URI,
                    certsProjection,
                    null, null,
                    // here we  want the proper order, ie NR, G, PG, PG-13, etc
                    MovieTheaterContract.CertsEntry.COLUMN_CERT_ORDER + " ASC");
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOGTAG, "entered onLoadFinished");

        if(loader.getId() == GENRES_TABLE_LOADER_ID) {
            mGenreSpinnerAdapter.swapCursor(data);

            // must wait until load finished to setSelection in spinner, because the
            // onItemSelected spinner callback fires immediately when it's set up, and if
            // the selected item is not in the list yet (because the loader has not returned the
            // cursor which contains the spinner drop down elements), the onItemSelected method
            // will think that the user changed the selection, while will trigger an unnecessary
            // API call when user navigates back to HomeActivity.. details details
            mGenreSpinner.setSelection(mSharedPrefs.
                    getInt(getString(R.string.key_movie_filter_genre_spinner_position), 0));
            // I don't think it matters if setOnItemSelectedListener is here on in onCreate
            mGenreSpinner.setOnItemSelectedListener(this);
        }
        else if (loader.getId() == CERTS_TABLE_LOADER_ID) {
            mCertSpinnerAdapter.swapCursor(data);
            mCertSpinner.setSelection(mSharedPrefs.
                    getInt(getString(R.string.key_movie_filter_cert_spinner_position), 0));
            mCertSpinner.setOnItemSelectedListener(this);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == GENRES_TABLE_LOADER_ID) {
            mGenreSpinnerAdapter.swapCursor(null);
        }
        else if(loader.getId() == CERTS_TABLE_LOADER_ID) {
//            mCertSpinnerAdapter.swapCursor(null);
        }
    }
}
