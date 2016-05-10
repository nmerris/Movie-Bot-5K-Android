package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nate.moviebot5k.R;
import com.nate.moviebot5k.SingleFragmentActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/10/2016.
 */
public class MoviePosterAdapter extends CursorAdapter {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovPosterAdapter";

    private final int MOVIE_POSTER_VIEWHOLDER_TAG_KEY = 1;


    public MoviePosterAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    // so heres the deal: Butterknife is convenient and very tidy, but it does not replace the need
    // for a ViewHolder.. .bind basically makes a (faster?) .findViewById call
    public static class ViewHolder {
        // trying out ButterKnife
        @Bind(R.id.fragment_moviegrid_poster_image_view) ImageView posterImageView;

        public ViewHolder(View view) {
            // use this Butterknife method when NOT in an activity
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(LOGTAG, "entered newView");

        ViewHolder viewHolder;

        View view = LayoutInflater.from(context).inflate(R.layout.moviegrid_poster, parent, false);

        viewHolder = new ViewHolder(view);

        view.setTag(MOVIE_POSTER_VIEWHOLDER_TAG_KEY, view);

        return null;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.i(LOGTAG, "entered bindView");


    }
}
