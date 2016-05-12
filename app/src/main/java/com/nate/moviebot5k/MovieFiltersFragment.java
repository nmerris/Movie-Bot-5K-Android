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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsFragmnt";
    
    @Bind(R.id.fragment_movie_filter_spinner_year) AppCompatSpinner yearSpinner;
    @Bind(R.id.fragment_movie_filter_spinner_sortby) AppCompatSpinner sortbySpinner;
    
    SharedPreferences mSharedPrefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_movie_filters, container, false);
        ButterKnife.bind(this, rootView);




        // set an adapter on the year spinner
//        yearSpinner = (AppCompatSpinner) rootView.findViewById(R.id.fragment_movie_filter_spinner_year);
        ArrayAdapter<String> yrSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                Utility.getMovieFilterYears(getActivity())); // get all the selectable years
        yrSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yrSpinnerAdapter);

        // make sure the spinner starts at the same position as last time
        yearSpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_year_spinner_position), 0));
        yearSpinner.setOnItemSelectedListener(this);


        ArrayAdapter<String> sortbySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.movie_filter_sortby_labels));
        sortbySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortbySpinner.setAdapter(sortbySpinnerAdapter);

        sortbySpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0));
        sortbySpinner.setOnItemSelectedListener(this);
                


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
                String currentSortbyFilter = mSharedPrefs.getString(getString(R.string.key_movie_filter_sortby), "");

                for

                String selectedSortbyFilter = parent.getItemAtPosition(position).toString();





                // check to make sure user did not just select the same item that was already selected
                if(!currentSortbyFilter.equals(selectedSortbyFilter)) {

                    // update the sortby filter
                    editor.putString(getString(R.string.key_movie_filter_sortby), selectedSortbyFilter);



                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
                    editor.putBoolean(getString(R.string.key_fetch_new_movies), true);
                    // store the position in the spinner so that it's the same next time user comes back here
                    editor.putInt(getString(R.string.key_movie_filter_sortby_spinner_position), position);

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_sortby: " + selectedSortbyFilter);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
                }
                break;



        }
        
        editor.commit();







    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally blank
    }

}
