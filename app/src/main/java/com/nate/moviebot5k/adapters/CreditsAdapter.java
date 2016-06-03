package com.nate.moviebot5k.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nate.moviebot5k.ActivitySingleFragment;
import com.nate.moviebot5k.FragmentCredits;
import com.nate.moviebot5k.FragmentMovieGrid;
import com.nate.moviebot5k.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 *
 * Created by Nathan Merris on 5/10/2016.
 */
public class CreditsAdapter extends CursorAdapter {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "CreditsAdptr";
    private Context mContext;

    public CreditsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    public static class ViewHolder {
        @Bind(R.id.credits_adapter_imageview) ImageView creditProfileImageView;
        @Bind(R.id.credits_adapter_character_name_textview) TextView creditCharacterNameTV;
        @Bind(R.id.credits_adapter_cast_name_textview) TextView creditCastNameTV;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.credits_adapter_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
//        Log.i(LOGTAG, "  about to load poster path with Picasso: " + cursor.getString(FragmentMovieGrid.MOVIES_TABLE_COL_POSTER_PATH));
//        Log.i(LOGTAG, "    and the movieId for same movie is: " + cursor.getInt(FragmentMovieGrid.MOVIES_TABLE_COL_MOVIE_ID));

        Picasso.with(context)
                .load(cursor.getString(FragmentCredits.COL_PROFILE_PATH))
                .placeholder(mContext.getResources().getDrawable(R.drawable.placeholder_person))
                .into(viewHolder.creditProfileImageView);

        viewHolder.creditCharacterNameTV.setText(
                String.format(mContext.getString(R.string.format_character_name),
                        cursor.getString(FragmentCredits.COL_CHARACTER)));
        viewHolder.creditCastNameTV.setText(
                String.format(mContext.getString(R.string.format_cast_name),
                        cursor.getString(FragmentCredits.COL_NAME)));
    }

}
