package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nate.moviebot5k.ActivitySingleFragment;
import com.nate.moviebot5k.FragmentMovieGrid;
import com.nate.moviebot5k.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/10/2016.
 */
public class MoviePosterAdapter extends CursorAdapter {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovPosterAdapter";
    private boolean mUseFavorites;

    public MoviePosterAdapter(Context context, Cursor c, int flags, boolean useFavorites) {
        super(context, c, flags);
        mUseFavorites = useFavorites;
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

        View view = LayoutInflater.from(context).inflate(R.layout.imageview_moviegrid_poster, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        // conveniently, you can stash any object with a view with setTag, and grab it later
        // you can even store multiple objects if you use keys
        // here I'm tagging each view with the same viewholder, and its unique movieId
        view.setTag(viewHolder);
//        view.setTag(R.id.movie_poster_imageview_movie_id_key,
//                cursor.getInt(FragmentMovieGrid.MOVIES_TABLE_COL_MOVIE_ID));

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        Log.i(LOGTAG, "  about to load poster path with Picasso: " + cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_PATH));
        Log.i(LOGTAG, "    and the movieId for same movie is: " + cursor.getInt(FragmentMovieGrid.MOVIES_TABLE_COL_MOVIE_ID));

        if(mUseFavorites) {

            Picasso.with(context)
                    .load(cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_FILE_PATH))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.moviedb_logo))
                    .into(viewHolder.posterImageView);
        }
        else {
            Picasso.with(context)
                    .load(cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_PATH))
                    .into(viewHolder.posterImageView);
        }

//        Picasso.with(context)
//                .load(cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_PATH))
//                .into(viewHolder.posterImageView);

        // TODO: use placeholder images? at least in case a movie has no image poster
        // no wait.. poster and backdrop paths are both NOT NULL in the db, so that will never happen
        // but might be good for slow data connections?
    }

}
