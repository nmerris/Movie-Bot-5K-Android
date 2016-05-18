package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.support.v4.widget.SimpleCursorAdapter;

import com.nate.moviebot5k.R;
import com.nate.moviebot5k.data.MovieTheaterContract;

/**
 * A very simple spinner adapter that uses a simple_spinner_item layout.
 * Both MovieFiltersFragment and FragmentMovieGrid use the same spinner, so why not make an
 * adapter that both of them can use?
 *
 * Created by Nathan Merris on 5/13/2016.
 */
public class CertSpinnerAdapter extends SimpleCursorAdapter {

    public CertSpinnerAdapter(Context context) {
        super(context,
                R.layout.textview_spinner_item,
                null,
                new String[]{MovieTheaterContract.CertsEntry.COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);
    }

}