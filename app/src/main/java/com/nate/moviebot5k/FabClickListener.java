package com.nate.moviebot5k;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

    private final String[] projection = {
            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH
    };
    private final int COLUMN_IS_FAVORITE = 0;
    private final int COLUMN_POSTER_PATH = 1;
    private final int COLUMN_BACKDROP_PATH = 2;
    private final int COLUMN_POSTER_FILE_PATH = 3;
    private final int COLUMN_BACKDROP_FILE_PATH = 4;


    public FabClickListener(Context context, int movieId, FloatingActionButton fabFavorites) {
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

        Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.e(LOGTAG, "  entered onBitmapLoaded");

                // create a new File object, set file name to same as themoviedb image id
                File file = new File(mContext.getExternalFilesDir(null), "testPoster.jpg");

                try {
                    FileOutputStream ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
//                    boolean wasWriteToStreamSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
//                    Log.e(LOGTAG, "    compress bitmap and write to ostream was successful: " + wasWriteToStreamSuccess);





                    String localFilePath = file.getPath();
//                    Log.i(LOGTAG, "    file path is: " + localFilePath);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
                            "file:" + localFilePath);

                    mContext.getContentResolver().update(
                            MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                            contentValues,
                            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{String.valueOf(mMovieId)});


                    ostream.flush();
                    ostream.close();
                } catch (IOException e) {
                    Log.e("IOException", e.getLocalizedMessage());
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    

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
                            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH));

            Picasso.with(mContext)
                    .load(cursor.getString(COLUMN_BACKDROP_PATH))
                    .into(new PicassoTarget(cursor.getString(COLUMN_BACKDROP_PATH),
                            MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_FILE_PATH));

            // TODO: same thing for all the credits images, maybe with a separate Target impl.

            cursor.close();
        }

    }



    private class PicassoTarget implements Target {

        String fileName;
        String dbColumnToInsert;

        public PicassoTarget(String theMovieDbImagePath, String dbColumnNameToInsert) {
            // in the db, COLUMN_WHATEVER_PATH looks like '/4h8dsheHD89h48HF348.jpg'
            // (it's the unique ID from themoviedb) so I'm reusing it as the filepath for local storage
            if(theMovieDbImagePath != null) {
                String[] s = theMovieDbImagePath.split("/");
                this.fileName = s[1] + ".jpg";
                Log.i(LOGTAG, "****** fileName in PicassoTarget constructor is: " + this.fileName);
            }
            dbColumnToInsert = dbColumnNameToInsert;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.e(LOGTAG, "  entered onBitmapLoaded");

            try {
                File file = new File(mContext.getExternalFilesDir(null), fileName);
                FileOutputStream ostream = new FileOutputStream(file);

                // this actually writes the file to local device storage
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);

                // testing
                boolean wasWriteToStreamSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                Log.e(LOGTAG, "    compress bitmap and write to ostream was successful: " + wasWriteToStreamSuccess);

                String localFilePath = file.getPath();
                Log.i(LOGTAG, "      localFilePath is: " + localFilePath);

                ContentValues contentValues = new ContentValues();
                // Picasso need the file path to start with 'file:' when it loads favorites
                contentValues.put(dbColumnToInsert, "file:" + localFilePath);

                mContext.getContentResolver().update(
                        MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                        contentValues,
                        MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{String.valueOf(mMovieId)});

                ostream.flush();
                ostream.close();
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