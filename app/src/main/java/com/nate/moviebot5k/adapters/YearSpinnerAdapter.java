package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.nate.moviebot5k.Utility;

/**
 * Created by Nathan Merris on 5/13/2016.
 */
public class YearSpinnerAdapter extends ArrayAdapter<String> {

    public YearSpinnerAdapter(Context context) {
        super(context,
                android.R.layout.simple_spinner_item,
                Utility.getMovieFilterYears(context)); // get all the selectable years);
    }

}