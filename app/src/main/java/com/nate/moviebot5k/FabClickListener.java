package com.nate.moviebot5k;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

import com.nate.moviebot5k.data.MovieTheaterContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// handles everything when FAB favorite icon is clicked, including updating icon drawable and db work
class FabClickListener implements View.OnClickListener {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "FABClckLstnr";

    private Context mContext;
    private int mMovieId;
    private FloatingActionButton mFabFavorites;
    private int numCreditsImagesToStoreOffline;
    private final String[] projection = {
            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_PATH
//            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
//            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH
    };
    private final int COLUMN_IS_FAVORITE = 0;
    private final int COLUMN_POSTER_PATH = 1;
    private final int COLUMN_BACKDROP_PATH = 2;
//    private final int COLUMN_POSTER_FILE_PATH = 3;
//    private final int COLUMN_BACKDROP_FILE_PATH = 4;

    private final String[] creditsProjection = {
            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH
//            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH,
    };
    private final int COLUMN_PROFILE_PATH = 0;
//    private final int COLUMN_PROFILE_FILE_PATH = 1;


    public FabClickListener(Context context, int movieId, FloatingActionButton fabFavorites) {
        numCreditsImagesToStoreOffline = context.getResources()
                .getInteger(R.integer.num_credits_profile_images_to_store_offline);


        mContext = context;
        mMovieId = movieId;
        mFabFavorites = fabFavorites;
    }

    
    @Override
    public void onClick(View v) {
        Cursor cursor = mContext.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                new String[]{MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE},
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(mMovieId)}, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            boolean initialFavState = Boolean.valueOf(cursor.getString(COLUMN_IS_FAVORITE));
            Log.i(LOGTAG, "  FAB listener.onClick, movieId is: " + mMovieId);
//            Log.i(LOGTAG, "    and before anything else, column is_favorite for that id is: " + initialFavState);
            cursor.close();
            
            // toggle the fab drawable
            int fabDrawable = initialFavState ?
                    R.drawable.btn_star_off_normal_holo_light : R.drawable.btn_star_on_normal_holo_light;
//            Log.i(LOGTAG, "    and fabDrawable id is: " + fabDrawable);
            mFabFavorites.setImageDrawable(mContext.getResources().getDrawable(fabDrawable));
            
            toggleIsFavoriteInAllTables(initialFavState);
            
            // if the movie was just saved as a favorite, save all relevant images to local device storage
            // and update the db tables with the file paths of these new files
            if (!initialFavState) {
                saveImagesLocally();
            }
        }
        
    }
    
    
    private void saveImagesLocally() {
        Log.i(LOGTAG, "entered saveImagesLocally");

        // update movies table file path columns for poster and backdrop images
        Cursor cursor = mContext.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                projection,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(mMovieId)}, null);

        if (cursor != null && cursor.moveToFirst()) {
            Picasso.with(mContext)
                    .load(cursor.getString(COLUMN_POSTER_PATH))
                    .into(new PicassoTarget(cursor.getString(COLUMN_POSTER_PATH),
                            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
                            MovieTheaterContract.MoviesEntry.TABLE_NAME));

            Picasso.with(mContext)
                    .load(cursor.getString(COLUMN_BACKDROP_PATH))
                    .into(new PicassoTarget(cursor.getString(COLUMN_BACKDROP_PATH),
                            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH,
                            MovieTheaterContract.MoviesEntry.TABLE_NAME));
            
            cursor.close();
        }


        // update credits table file path columns for credits profile images
        Cursor creditsCursor = mContext.getContentResolver().query(
                MovieTheaterContract.CreditsEntry.CONTENT_URI,
                new String[]{ MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH },
                MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) }, null);

        // only downloading 4 images for offline use at this time
        if (creditsCursor != null && creditsCursor.moveToFirst()) {
            for(int i = 0; i < numCreditsImagesToStoreOffline; i++) {
                if(creditsCursor.isAfterLast()) { break; }

                Picasso.with(mContext)
                        .load(creditsCursor.getString(0))
                        .into(new PicassoTarget(creditsCursor.getString(0),
                                MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH,
                                MovieTheaterContract.CreditsEntry.TABLE_NAME));

                creditsCursor.moveToNext();
            }
            creditsCursor.close();
        }


    }



    private class PicassoTarget implements Target {

        String fileName;
        String dbColumnToInsert;
        String tableName;
        String theMovieDbImagePath;


        public PicassoTarget(String theMovieDbImagePath, String dbColumnNameToInsert, String tableName) {
            Log.i(LOGTAG, "IN PICASSOTARGET CONSTRUCTOR, theMDBImagePath is: " + theMovieDbImagePath);


            Uri uri = Uri.parse(theMovieDbImagePath);
            fileName = uri.getLastPathSegment();

            Log.i(LOGTAG, "****** fileName in PicassoTarget constructor is: " + fileName);

            dbColumnToInsert = dbColumnNameToInsert;
            this.tableName = tableName;
            this.theMovieDbImagePath = theMovieDbImagePath;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.e(LOGTAG, "  entered onBitmapLoaded");

            try {
                File file = new File(mContext.getExternalFilesDir(null), fileName);
                FileOutputStream ostream = new FileOutputStream(file);

                // this actually writes the file to local device storage
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                ostream.flush();
                ostream.close();

                // get the file path of the just created file and update the appropriate db table and column
                String localFilePath = file.getPath();
                Log.i(LOGTAG, "      localFilePath is: " + localFilePath);

                ContentValues contentValues = new ContentValues();
                // Picasso need the file path to start with 'file:' when it loads favorites
                contentValues.put(dbColumnToInsert, "file:" + localFilePath);

//                Uri uri = tableName.equals(MovieTheaterContract.MoviesEntry.TABLE_NAME) ?
//                        MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId) :
//                        MovieTheaterContract.CreditsEntry.CONTENT_URI;

                if(tableName.equals(MovieTheaterContract.MoviesEntry.TABLE_NAME)) {
                    mContext.getContentResolver().update(
                            MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                            contentValues,
                            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{String.valueOf(mMovieId)});
                }
                else {
                    mContext.getContentResolver().update(
                            MovieTheaterContract.CreditsEntry.CONTENT_URI,
                            contentValues,
                            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH + " = ?",
                            new String[]{ theMovieDbImagePath });
                }


            }
            catch (NullPointerException e) {
                Log.e("NULL POINTER EXCEPTION", "fileName was NULL in PicassoTarget " +
                        "onBitmapLoaded callback, could not create file");
            }
            catch (IOException e) {
                Log.e("IOException", e.getLocalizedMessage());
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    }



    
    
    // updates all db tables that match movieId.. pass in the current fav state and it will toggle it
    private void toggleIsFavoriteInAllTables(boolean initialFavState) {
        Log.i(LOGTAG, "in toggleIsFavoriteInAllTables, initialFavState is: " + initialFavState +
                " and will be updating all db records with movie_id: " + mMovieId + " to: " + !initialFavState);
        
        ContentValues contentValues = new ContentValues();
        contentValues.put("is_favorite", String.valueOf(!initialFavState));
        
        int numMovieRecordsUpated = mContext.getContentResolver().update(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                contentValues,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) });
        Log.i(LOGTAG, "      num movies table records updated: " + numMovieRecordsUpated);

//            contentValues.put(MovieTheaterContract.VideosEntry.COLUMN_IS_FAVORITE, String.valueOf(!initialFavState));
        int numVideosRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.VideosEntry.buildVideosUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
        Log.i(LOGTAG, "      num videos table records updated: " + numVideosRecordsUpdated);


//            contentValues.put(MovieTheaterContract.CreditsEntry.COLUMN_IS_FAVORITE, String.valueOf(!initialFavState));
        int numCreditsRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.CreditsEntry.buildCreditsUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
        Log.i(LOGTAG, "      num credits table records updated: " + numCreditsRecordsUpdated);


//            contentValues.put(MovieTheaterContract.ReviewsEntry.COLUMN_IS_FAVORITE, String.valueOf(!initialFavState));
        int numReviewsRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.ReviewsEntry.buildReviewsUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
        Log.i(LOGTAG, "      num reviews table records updated: " + numReviewsRecordsUpdated);
        
    }
    
    
}