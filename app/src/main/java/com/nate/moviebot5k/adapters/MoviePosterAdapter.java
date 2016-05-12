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


    public MoviePosterAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        Log.i(LOGTAG, "entered MoviePosterAdapter constructor");
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
//        Log.i(LOGTAG, "entered newView");

        View view = LayoutInflater.from(context).inflate(R.layout.moviegrid_poster, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        // conveniently, you can stash any object with a view with setTag, and grab it later
        // you can even store multiple objects if you use keys (not needed here)
        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        Log.i(LOGTAG, "entered bindView");

        // get the ViewHolder, no need for a key because it's the only object associated with the view
        ViewHolder viewHolder = (ViewHolder) view.getTag();

//        Log.i(LOGTAG, "  about to load poster path with Picasso: " + cursor.getString(MovieGridFragment.MOVIES_TABLE_COL_POSTER_PATH));
//        Log.i(LOGTAG, "    and the movieId for same movie is: " + cursor.getInt(MovieGridFragment.MOVIES_TABLE_COL_MOVIE_ID));

        Picasso.with(context)
                .load(cursor.getString(MovieGridFragment.MOVIES_TABLE_COL_POSTER_PATH))
                .into(viewHolder.posterImageView);

        // TODO: use placeholder images? at least in case a movie has no image poster
        // no wait.. poster and backdrop paths are both NOT NULL in the db, so that will never happen
    }

}
