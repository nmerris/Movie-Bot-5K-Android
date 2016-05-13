package com.nate.moviebot5k;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nate.moviebot5k.adapters.CertSpinnerAdapter;
import com.nate.moviebot5k.adapters.GenreSpinnerAdapter;
import com.nate.moviebot5k.data.MovieTheaterContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsFragmnt";
    
    @Bind(R.id.fragment_movie_filter_spinner_year) AppCompatSpinner mYearSpinner;
    @Bind(R.id.fragment_movie_filter_spinner_sortby) AppCompatSpinner mSortbySpinner;
    @Bind(R.id.fragment_movie_filter_spinner_genre) AppCompatSpinner mGenreSpinner;
    @Bind(R.id.fragment_movie_filter_spinner_cert) AppCompatSpinner mCertSpinner;

    private static final int GENRES_TABLE_LOADER_ID = 1;
    private static final int CERTS_TABLE_LOADER_ID = 2;
    
    private SharedPreferences mSharedPrefs;
    private SimpleCursorAdapter mGenreSpinnerAdapter, mCertSpinnerAdapter;

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
    


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GENRES_TABLE_LOADER_ID, null, this);
        getLoaderManager().initLoader(CERTS_TABLE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_movie_filters, container, false);
        ButterKnife.bind(this, rootView);

        
        // set an adapter on the year spinner
        ArrayAdapter<String> yrSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                Utility.getMovieFilterYears(getActivity())); // get all the selectable years
        yrSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yrSpinnerAdapter);

        // make sure the spinner starts at the same position as last time
        mYearSpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_year_spinner_position), 0));
        mYearSpinner.setOnItemSelectedListener(this);


        // set an adapter on the sortby spinner
        ArrayAdapter<String> sortbySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.movie_filter_sortby_labels));
        sortbySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortbySpinner.setAdapter(sortbySpinnerAdapter);
        mSortbySpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0));
        mSortbySpinner.setOnItemSelectedListener(this);


        // set an adapter on the genre spinner
        mGenreSpinnerAdapter = new GenreSpinnerAdapter(getActivity());
        mGenreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenreSpinner.setAdapter(mGenreSpinnerAdapter);


        // set an adapter on the cert spinner
        mCertSpinnerAdapter = new CertSpinnerAdapter(getActivity());
        mCertSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCertSpinner.setAdapter(mCertSpinnerAdapter);

        
        return rootView;
    }





    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(LOGTAG, "entered onItemSelected");
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        
        switch (parent.getId()) {
            case R.id.fragment_movie_filter_spinner_year:
                String currentYearFilter = mSharedPrefs.getString(getString(R.string.key_movie_filter_year), "");
                String selectedYearFilter = parent.getItemAtPosition(position).toString();

                // check to make sure user did not just select the same item that was already selected
                if(!currentYearFilter.equals(selectedYearFilter)) {
                    // update the year filter
                    editor.putString(getString(R.string.key_movie_filter_year), selectedYearFilter);
                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
                    editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
                    // store the position in the spinner so that it's the same next time user comes back here
                    editor.putInt(getString(R.string.key_movie_filter_year_spinner_position), position);

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_year: " + selectedYearFilter);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
                }
                break;

            case R.id.fragment_movie_filter_spinner_sortby:
                int savedSortbySpinnerPosition = mSharedPrefs
                        .getInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0);

                // check to see if the user actually changed the sortby selection
                // this is why the order of the resource array matters
                if(savedSortbySpinnerPosition != position) {
                    String[] sortbyValues = getResources().getStringArray(R.array.movie_filter_sortby_values);

                    // update the sortby filter value, which is not the same as the label
                    // the value is what is used in MoviesFetcher for the API call
                    // the label is what the user sees in the spinner
                    editor.putString(getString(R.string.key_movie_filter_sortby_value), sortbyValues[position]);

                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
                    editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
                    // store the position in the spinner so that it's the same next time user comes back here
                    editor.putInt(getString(R.string.key_movie_filter_sortby_spinner_position), position);

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_sortby_value: " + sortbyValues[position]);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");

                }
                break;

            case R.id.fragment_movie_filter_spinner_genre:
                String savedGenreId = mSharedPrefs
                        .getString(getString(R.string.key_movie_filter_genre_id), "");

                // get the selected genre filter **ID** from the tag of the view
                String selectedGenreId = (String) view.getTag();

                // check if user actually changed the genre filter
                if(!savedGenreId.equals(selectedGenreId)) {
                    // update the genreId saved in sharedPrefs
                    editor.putString(getString(R.string.key_movie_filter_genre_id), selectedGenreId);
                    // update fetch_new_movies so a new fetch task starts when appropriate
                    editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
                    // save the new position of the spinner
                    editor.putInt(getString(R.string.key_movie_filter_genre_spinner_position), position);

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_genre_id: " + selectedGenreId);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");

                }
                break;

            case R.id.fragment_movie_filter_spinner_cert:
                String savedCert = mSharedPrefs
                        .getString(getString(R.string.key_movie_filter_cert), "");

                // get the selected cert (only need name here, no ID needed for certs api queries)
                TextView tv = (TextView) view;
                String selectedCert = tv.getText().toString();

                // check if user actually changed the cert filter
                if(!savedCert.equals(selectedCert)) {
                    // update the certId saved in sharedPrefs
                    editor.putString(getString(R.string.key_movie_filter_cert), selectedCert);
                    // update fetch_new_movies so a new fetch task starts when appropriate
                    editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
                    // save the new position of the spinner
                    editor.putInt(getString(R.string.key_movie_filter_cert_spinner_position), position);

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_cert_id: " + selectedCert);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");

                }

        }
        editor.commit();
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally blank
    }


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
