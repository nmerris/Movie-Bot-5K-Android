package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nate.moviebot5k.R;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class YearSpinnerAdapter extends ArrayAdapter<String> {


    public YearSpinnerAdapter(Context context, String[] years) {
        super(context, R.layout.spinner_year_textview, years);
    }








//
//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        return null;
//    }
//
//


}
