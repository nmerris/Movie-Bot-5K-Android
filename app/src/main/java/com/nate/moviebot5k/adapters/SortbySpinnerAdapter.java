package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.nate.moviebot5k.R;

/**
 * Created by Nathan Merris on 5/13/2016.
 */
public class SortbySpinnerAdapter extends ArrayAdapter<String> {

    public SortbySpinnerAdapter(Context context) {
        super(context,
                R.layout.spinner_item,
                context.getResources().getStringArray(R.array.movie_filter_sortby_labels));
    }

}
