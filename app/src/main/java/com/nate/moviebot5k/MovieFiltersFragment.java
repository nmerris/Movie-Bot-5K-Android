package com.nate.moviebot5k;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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

import com.nate.moviebot5k.data.MovieTheaterContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsFragmnt";
    
    @Bind(R.id.fragment_movie_filter_spinner_year) AppCompatSpinner yearSpinner;
    @Bind(R.id.fragment_movie_filter_spinner_sortby) AppCompatSpinner sortbySpinner;
    @Bind(R.id.fragment_movie_filter_spinner_genre) AppCompatSpinner genreSpinner;
    @Bind(R.id.fragment_movie_filter_spinner_cert) AppCompatSpinner certSpinner;

    private static final int GENRES_TABLE_LOADER_ID = 1;
    private static final int CERTS_TABLE_LOADER_ID = 2;
    
    private SharedPreferences mSharedPrefs;
    private SimpleCursorAdapter mGenreSpinnerAdapter, mCertsSpinnerAdapter;
    private Cursor mGenresCursor, mCertsCursor;


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
        yearSpinner.setAdapter(yrSpinnerAdapter);

        // make sure the spinner starts at the same position as last time
        yearSpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_year_spinner_position), 0));
        yearSpinner.setOnItemSelectedListener(this);


        // set an adapter on the sortby spinner
        ArrayAdapter<String> sortbySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.movie_filter_sortby_labels));
        sortbySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortbySpinner.setAdapter(sortbySpinnerAdapter);
        sortbySpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0));
        sortbySpinner.setOnItemSelectedListener(this);


        // set a SimpleCursorAdapter on the genre spinner
        mGenreSpinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                null, // cursor is null here because Loader will deal with it
                // what data column to bind to the text view built into the spinner
                new String[] {MovieTheaterContract.GenresEntry.COLUMN_GENRE_NAME},
                // this was tricky for me to figure out, this is the view id used by the spinner
                // when you use the built in simple_spinner_item layout
                new int[]{android.R.id.text1},
                0); // no flags needed
        mGenreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(mGenreSpinnerAdapter);
        genreSpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_genre_spinner_position), 0));
        genreSpinner.setOnItemSelectedListener(this);
                


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
                String selectedGenreName = parent.getItemAtPosition(position).toString();
                String savedGenreName = mSharedPrefs
                        .getString(getString(R.string.key_movie_filter_genre_id), "");

                // get the genreId from the db, given a genre name

                // check to see if user actually changed the genre filter
                if(!selectedGenreName.equals(savedGenreName)) {

                    Uri currGenreIdQueryUri = MovieTheaterContract.GenresEntry
                            .buildGenreIdUriFromGenreName(selectedGenreName);

                    Cursor c = getActivity().getContentResolver().query(currGenreIdQueryUri, null, null, null, null);

                    if(c.moveToFirst()) {
                        String selectedGenreId = c.getString(1);

                    }

                }







//
//                int savedGenreSpinnerPosition = mSharedPrefs
//                        .getInt(getString(R.string.key_movie_filter_genre_spinner_position), 0);
//
//                // check to see if the user actually changed the genre selection
//                // this is why the order of the resource array matters
//                if(savedSortbySpinnerPosition != position) {
//
//
//
//                    // update the genre filter id
//                    editor.putInt(getString(R.string.key_movie_filter_genre_id), genreValues[position]);
//
//
//
//                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
//                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
//                    editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
//                    // store the position in the spinner so that it's the same next time user comes back here
//                    editor.putInt(getString(R.string.key_movie_filter_genre_spinner_position), position);
//
//                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_genre_value: " + genreValues[position]);
//                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
//
//                }
                break;



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
                    // the order in which the genres are listed doesn't matter
                    null, null, null, null);
        }
        else if(id == CERTS_TABLE_LOADER_ID) {
            Log.i(LOGTAG, "  and about to return new CERTS_TABLE_LOADER");

            return new CursorLoader(getActivity(),
                    MovieTheaterContract.CertsEntry.CONTENT_URI,
                    null, null, null,
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
            mGenresCursor = data;
        }
        else if (loader.getId() == CERTS_TABLE_LOADER_ID) {
//            mCertsSpinnerAdapter.swapCursor(data);
//            mCertsCursor = data;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == GENRES_TABLE_LOADER_ID) {
            mGenreSpinnerAdapter.swapCursor(null);
        }
        else if(loader.getId() == CERTS_TABLE_LOADER_ID) {
//            mCertsSpinnerAdapter.swapCursor(null);
        }
    }

}
