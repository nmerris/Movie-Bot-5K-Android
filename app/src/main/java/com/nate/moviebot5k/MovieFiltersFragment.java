package com.nate.moviebot5k;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsFragmnt";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_movie_filters, container, false);


        AppCompatSpinner spinner = (AppCompatSpinner) rootView.findViewById(R.id.fragment_movie_filter_spinner_year);
        ArrayAdapter<String> yrSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                Utility.getMovieFilterYears(getActivity()));
        yrSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(yrSpinnerAdapter);
        spinner.setOnItemSelectedListener(this);






        return rootView;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();

        String currentYearFilter = sharedPrefs.getString(getString(R.string.key_movie_filter_year), "");
        String selectedYearFilter = parent.getItemAtPosition(position).toString();


        if(!currentYearFilter.equals(selectedYearFilter)) {
            editor.putString(getString(R.string.key_movie_filter_year), selectedYearFilter);
            editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
            editor.commit();
            Log.i(LOGTAG, "in onItemSelected, just wrote to sharedPrefs key_movie_filter_year: " + selectedYearFilter);
            Log.i(LOGTAG, "  and changed sharedPrefs key_fetch_new_movies bool to TRUE");
        }




    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
