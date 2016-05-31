package com.nate.moviebot5k;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.View;

import com.nate.moviebot5k.data.MovieTheaterContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// handles everything when FAB favorite icon is clicked, including updating icon drawable and db work,
// and saving or deleting local device files
class FabClickListener implements View.OnClickListener {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "FABClckLstnr";

    private Context mContext;
    private int mMovieId;
    private FloatingActionButton mFabFavorites;
    private int mNumCreditsImagesToStoreOffline;

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

//    private final String[] creditsProjection = {
//            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH
////            MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH,
//    };
//    private final int COLUMN_PROFILE_PATH = 0;
////    private final int COLUMN_PROFILE_FILE_PATH = 1;


    public FabClickListener(Context context, int movieId, FloatingActionButton fabFavorites) {
        mNumCreditsImagesToStoreOffline = context.getResources()
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
//            cursor.moveToFirst();
            boolean removeFromFavorites = Boolean.valueOf(cursor.getString(COLUMN_IS_FAVORITE));
            Log.i(LOGTAG, "  FAB listener.onClick, movieId is: " + mMovieId);
//            Log.i(LOGTAG, "    and before anything else, column is_favorite for that id is: " + initialFavState);

            cursor.close();
            
            // toggle the fab drawable
            int fabDrawable = removeFromFavorites ?
                    R.drawable.btn_star_off_normal_holo_light : R.drawable.btn_star_on_normal_holo_light;
//            Log.i(LOGTAG, "    and fabDrawable id is: " + fabDrawable);
            mFabFavorites.setImageDrawable(mContext.getResources().getDrawable(fabDrawable));
            
            toggleIsFavoriteInAllTables(removeFromFavorites);




            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            int initialNumFavorites = sharedPrefs.getInt(mContext.getString(R.string.key_num_favorites), 0);
            int currSelectedFavorite = sharedPrefs.getInt(mContext.getString(R.string.key_currently_selected_favorite_id), 0);
            Log.i(LOGTAG, "    and numFavorites when button clicked was: " + initialNumFavorites);
            Log.i(LOGTAG, "      and currently selected favoriteId stored in sharedPrefs is: " + currSelectedFavorite);

            // REMOVIE FROM FAVORITES:
            if(removeFromFavorites){
                if(initialNumFavorites == 1) {
                    // user just removed the last favorite, so now their favs list is empty
                    // I indicate 'no movie id' with -1
                    editor.putInt(mContext.getString(R.string.key_currently_selected_favorite_id), -1);
                } else if(currSelectedFavorite == mMovieId) {
                    // user just removed the favorite movie they had selected in favorites activity
                    // detail view, so just reset it to the first favorite
                    Cursor c = mContext.getContentResolver().query(
                            MovieTheaterContract.MoviesEntry.CONTENT_URI,
                            new String[]{ MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID },
                            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE + " = ?",
                            new String[]{ "true" }, null);

                    if(c != null && c.moveToFirst()) {
                        editor.putInt(mContext.getString(R.string.key_currently_selected_favorite_id),
                                c.getInt(0));
                        c.close();
                    }
                }

                // decrement the favorites counter and update in sharedPrefs
                editor.putInt(mContext.getString(R.string.key_num_favorites), initialNumFavorites - 1);
                editor.commit();
                Log.i(LOGTAG,  "numFavorites is now: " + (initialNumFavorites - 1));

                deleteSavedImaged();
            }
            // ADD TO FAVORITES:
            else {
                if(initialNumFavorites == 0) {
                    // the only way for this code to execute is if the user just added a movie to
                    // their favs list and they didn't have any favorites before, so this is the one
                    // and only favorite.. in which case set it to be their 'selected' favorite so
                    // that favorites activity can politely load a details fragment when the user
                    // navigates to there, other times the last selected favorite is loaded
//                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(mContext.getString(R.string.key_currently_selected_favorite_id), mMovieId);
                }
                editor.putInt(mContext.getString(R.string.key_num_favorites), initialNumFavorites + 1);
                editor.commit();
                Log.i(LOGTAG,  "numFavorites is now: " + (initialNumFavorites + 1));
                saveImagesLocally();
            }

        }
        
    }


    private void deleteSavedImaged() {
        Log.i(LOGTAG, "entered deleteSavedImaged");

        ArrayList<String> filePathsToDelete = new ArrayList<>();

        // get cursor pointed at relevant movies table row
        Cursor cursor = mContext.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                projection,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) }, null);

        if(cursor != null && cursor.moveToFirst()) {
            Uri uriPoster = Uri.parse(cursor.getString(COLUMN_POSTER_FILE_PATH));
            filePathsToDelete.add(uriPoster.getPath());
            Log.e(LOGTAG, "  and path extracted from db column for poster file is: " + uriPoster.getPath());
    
            Uri uriBackdrop = Uri.parse(cursor.getString(COLUMN_BACKDROP_FILE_PATH));
            filePathsToDelete.add(uriBackdrop.getPath());
            Log.e(LOGTAG, "  and path extracted from db column for poster file is: " + uriBackdrop.getPath());

            cursor.close();
        }
        
        // get cursor pointed at relevant credits table rows
        cursor = mContext.getContentResolver().query(
                MovieTheaterContract.CreditsEntry.CONTENT_URI,
                new String[]{ MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_FILE_PATH },
                MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) }, null);

        if(cursor != null && cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                // skip over db rows that do not have an image path.. there can be credits entries
                // that have text data but no image path, esp. for older movies.. for example:
                // "Freaks" (release date 1932)
                if(cursor.getString(0) == null) {
                    cursor.moveToNext();
                    continue;
                }

                // strip the file path from the URI and delete the file
                Uri uri = Uri.parse(cursor.getString(0));
                filePathsToDelete.add(uri.getPath());
                Log.e(LOGTAG, "  and path extracted from db column for credit file is: " + uri.getPath());
                cursor.moveToNext();
            }
            cursor.close();
        }

        
        
        
        for (String s : filePathsToDelete) {
            File file = new File(s);
            boolean wasDeleted = file.delete();
            Log.i(LOGTAG, "    file with path: " + s + " was deleted: " + wasDeleted);
        }

    }



    
    
    private void saveImagesLocally() {
        Log.i(LOGTAG, "entered saveImagesLocally");

        // update movies table file path columns for poster and backdrop images
        Cursor cursor = mContext.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                projection,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) }, null);

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
            for(int i = 0; i < mNumCreditsImagesToStoreOffline; i++) {
                if(creditsCursor.isAfterLast()) break;

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


                if(tableName.equals(MovieTheaterContract.MoviesEntry.TABLE_NAME)) {
                    mContext.getContentResolver().update(
                            MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                            contentValues,
                            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{String.valueOf(mMovieId)});
                }
                else {
                    // update the credits table: use the same image file name as the one we already
                    // got when we queried themoviedb server for more movie details, but now since
                    // the movie is being stored as a favorite, update the appropriate record with
                    // the locally stored file path
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

        int numVideosRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.VideosEntry.buildVideosUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
        Log.i(LOGTAG, "      num videos table records updated: " + numVideosRecordsUpdated);

        int numCreditsRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.CreditsEntry.buildCreditsUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
        Log.i(LOGTAG, "      num credits table records updated: " + numCreditsRecordsUpdated);

        int numReviewsRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.ReviewsEntry.buildReviewsUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
        Log.i(LOGTAG, "      num reviews table records updated: " + numReviewsRecordsUpdated);
        
    }
    
    
}