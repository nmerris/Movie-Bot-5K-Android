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

import com.nate.moviebot5k.MovieGridFragment;
import com.nate.moviebot5k.R;
import com.nate.moviebot5k.SingleFragmentActivity;
import com.squareup.picasso.Picasso;

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

    // N8NOTE: using newView instead of getView, Android takes care of reusing the same view as
    // needed while scrolling through the listview
    // NOTE: BaseAdapter, from which CursorAdapter extends, does NOT have a newView method,
    // so it's just easier and convenient to use newView when possible, and more efficient
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

        // get the ViewHolder using the reference to it that was stashed in the tag in newView
        ViewHolder viewHolder = (ViewHolder) view.getTag(MOVIE_POSTER_VIEWHOLDER_TAG_KEY);


        Log.i(LOGTAG, "  about to load poster path with Picasso: " + cursor.getString(MovieGridFragment.MOVIES_TABLE_COL_POSTER_PATH));
        Log.i(LOGTAG, "    and the movieId for same movie is: " + cursor.getInt(MovieGridFragment.MOVIES_TABLE_COL_MOVIE_ID));

        // have Picasso download the image and update the view when it's done
        Picasso.with(context)
                .load(cursor.getString(MovieGridFragment.MOVIES_TABLE_COL_POSTER_PATH))
                .into(viewHolder.posterImageView);
        // TODO: use placeholder images?

    }
}
