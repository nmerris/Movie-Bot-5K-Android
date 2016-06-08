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
 * A simple cursor adapter that loads image data from the movies table.  If the app is running
 * in favorites mode, a locally stored image file path is grabbed from the db, otherwise the
 * URL to themoviedb image is grabbed from the db.  Either way, Picasso is used to load the images
 * into the GridView.
 *
 * Created by Nathan Merris on 5/10/2016.
 */
public class MoviePosterAdapter extends CursorAdapter {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovPosterAdapter";
    private boolean mUseFavorites;
    private Context mContext;

    public MoviePosterAdapter(Context context, Cursor c, int flags, boolean useFavorites) {
        super(context, c, flags);
        mContext = context;
        mUseFavorites = useFavorites;
    }


    // the deal: Butterknife is convenient and very tidy, but it does not replace the need
    // for a ViewHolder.. bind basically makes a (faster?) .findViewById call
    public static class ViewHolder {
        @Bind(R.id.fragment_moviegrid_poster_image_view) ImageView posterImageView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.imageview_moviegrid_poster, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
//        Log.i(LOGTAG, "  about to load poster path with Picasso: " + cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_PATH));
//        Log.i(LOGTAG, "    and the movieId for same movie is: " + cursor.getInt(FragmentMovieGrid.MOVIES_TABLE_COL_MOVIE_ID));

        // if viewing favorites, have Picasso load the locally stored files
        if(mUseFavorites) {
            Picasso.with(context)
                    .load(cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_FILE_PATH))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.placeholder_movie))
                    .into(viewHolder.posterImageView);
        }
        else { // otherwise load the images from themoviedb server
            Picasso.with(context)
                    .load(cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_PATH))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.placeholder_movie))
                    .into(viewHolder.posterImageView);
        }
    }

}
