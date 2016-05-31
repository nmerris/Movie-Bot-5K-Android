package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.nate.moviebot5k.R;
import com.nate.moviebot5k.Utility;

/**
 * A simple ArrayAdapter that populates the movie Year spinner.  The range of years is
 * obtained by calling Utility.getMovieFilterYears.
 *
 * Created by Nathan Merris on 5/13/2016.
 *
 * @see Utility#getMovieFilterYears(Context)
 */
public class YearSpinnerAdapter extends ArrayAdapter<String> {

    public YearSpinnerAdapter(Context context) {
        super(context, R.layout.textview_spinner_item, Utility.getMovieFilterYears(context));
    }

}