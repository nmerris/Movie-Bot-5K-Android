package com.nate.moviebot5k;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

import butterknife.Bind;

/**
 * Created by Nathan Merris on 5/6/2016.
 */
public class GenreSpinnerAdapter extends CursorAdapter {
    // there is no need to implement SpinnerAdapter because the only extra thing you get with that
    // interface is getDropDownView, which you would only need to implement if you want the spinner
    // drop down list to have different data than what is show in the spinner 'at rest' state
    // for this app, the drop down list and the at rest will contain the same data  ??????

    // no need to override getViewTypeCount as default is 1 and that's ok here
    // no need to override getItemViewType because they will all be identical in this case
    // no need for a viewholder because each view is only a simple textview

    @Bind(android.R.layout.simple_spinner_item) View simpleSpinnerItem;
    @Bind(android.R.layout.simple_spinner_dropdown_item) View simpleSpinnerDropDownItem;


    public GenreSpinnerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return simpleSpinnerItem;
    }


    // so if the drop down looks crappy, might have to override getDropDownView and/or newDropDownView?
//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        return super.getDropDownView(position, convertView, parent);
//    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {







    }

}
