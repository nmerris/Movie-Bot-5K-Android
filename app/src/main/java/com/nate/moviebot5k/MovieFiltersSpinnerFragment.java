package com.nate.moviebot5k;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.nate.moviebot5k.adapters.CertSpinnerAdapter;
import com.nate.moviebot5k.adapters.GenreSpinnerAdapter;
import com.nate.moviebot5k.adapters.SortbySpinnerAdapter;
import com.nate.moviebot5k.adapters.YearSpinnerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersSpinnerFragment extends Fragment
        /*implements AdapterView.OnItemSelectedListener*//*, LoaderManager.LoaderCallbacks<Cursor>*/ {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsSpinFrg";

    @Bind(R.id.spinner_year) Spinner mYearSpinner;
    @Bind(R.id.spinner_sortby) Spinner mSortbySpinner;
    @Bind(R.id.spinner_genre) Spinner mGenreSpinner;
    @Bind(R.id.spinner_cert) Spinner mCertSpinner;

//    private SharedPreferences mSharedPrefs;
    private SimpleCursorAdapter mGenreSpinnerAdapter, mCertSpinnerAdapter;


//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // this is where Loaders are recommended to be initialized
        new GenreAndCertSpinnerLoader(getActivity(), mGenreSpinnerAdapter, mCertSpinnerAdapter,
                mGenreSpinner, mCertSpinner, getLoaderManager());

        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        View rootView = inflater.inflate(R.layout.fragment_filter_spinners, container, false);
        ButterKnife.bind(this, rootView);

        ArrayAdapter<String> yrSpinnerAdapter = new YearSpinnerAdapter(getActivity()); // get all the selectable years
        yrSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yrSpinnerAdapter);
        // make sure the spinner starts at the same position as last time
        mYearSpinner.setSelection(sharedPrefs.
                getInt(getString(R.string.key_movie_filter_year_spinner_position), 0));
        mYearSpinner.setOnItemSelectedListener(new SpinnerListener(getActivity()));


        ArrayAdapter<String> sortbySpinnerAdapter = new SortbySpinnerAdapter(getActivity());
        sortbySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortbySpinner.setAdapter(sortbySpinnerAdapter);
        mSortbySpinner.setSelection(sharedPrefs.
                getInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0));
        mSortbySpinner.setOnItemSelectedListener(new SpinnerListener(getActivity()));


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

}
