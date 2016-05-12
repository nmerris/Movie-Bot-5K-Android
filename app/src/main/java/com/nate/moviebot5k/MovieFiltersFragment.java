package com.nate.moviebot5k;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nate.moviebot5k.adapters.YearSpinnerAdapter;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersFragment extends Fragment {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsFragmnt";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_movie_filters, container, false);


        AppCompatSpinner spinner = (AppCompatSpinner) rootView.findViewById(R.id.fragment_movie_filter_spinner_year);
        spinner.setAdapter(new YearSpinnerAdapter(getActivity(),
                Utility.getMovieFilterYears(getActivity())));



        return rootView;
    }

}
