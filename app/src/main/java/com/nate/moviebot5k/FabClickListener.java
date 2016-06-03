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
import android.util.Log;
import android.view.View;

import com.nate.moviebot5k.data.MovieTheaterContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



/**
 * Handles everything when Floating Action Button (FAB) favorite icon is clicked,
 * including updating icon drawable, db work, and saving or deleting local device files.
 * Note that FragmentMovieDetail is responsible for drawing the correct icon in the FAB when it
 * initially loads all the views; however, this class takes care of updating the icon when the
 * user actually CLICKS on the FAB.
 *
 */
class FabClickListener implements View.OnClickListener {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "FABClckLstnr";
    private Context mContext;
    private int mMovieId;
    private FloatingActionButton mFabFavorites;
    private int mNumCreditsImagesToStoreOffline; // only storing a limited number of profile images on device

    // the order of the Strings in projection must match the ints below it
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


    /**
     * Creates a new listener to handle what happens when a user clicks the Floating Action Button
     * to add or remove a favorite movie.
     *
     * @param movieId The movieId that was just added or removed as a favorite
     * @param fabFavorites The actual FAB object that was just clicked
     */
    public FabClickListener(Context context, int movieId, FloatingActionButton fabFavorites) {
        // going with 4 offline profile images for now... but with enough time anything is possible!
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
            Log.i(LOGTAG, "just entered FAB onClick, movieId is: " + mMovieId);

            // if this movie is ALREADY a favorite, then user must want to remove it from favs,
            // so set removeFromFavorites to true, or vice-versa
            boolean removeFromFavorites = Boolean.valueOf(cursor.getString(COLUMN_IS_FAVORITE));
            cursor.close();
            
            // toggle the fab drawable
            int fabDrawable = removeFromFavorites ?
                    R.drawable.btn_star_off: R.drawable.btn_star_on;
            mFabFavorites.setImageDrawable(mContext.getResources().getDrawable(fabDrawable));

            // update all the db records in all tables so they reflect the correct favorite status
            toggleIsFavoriteInAllTables(removeFromFavorites);


            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPrefs.edit();

            // get the number of favorites saved before the FAB was clicked
            int initialNumFavorites = sharedPrefs.getInt(mContext.getString(R.string.key_num_favorites), 0);

            // get the currently selected favorite from sharedPrefs
            int currSelectedFavorite = sharedPrefs.getInt(mContext.getString(R.string.key_currently_selected_favorite_id), 0);

            Log.i(LOGTAG, "  and numFavorites when button clicked was: " + initialNumFavorites);
            Log.i(LOGTAG, "    and currently selected favoriteId stored in sharedPrefs is: " + currSelectedFavorite);

            // REMOVIE FROM FAVORITES:
            if(removeFromFavorites){
                if(initialNumFavorites == 1) {
                    Log.i(LOGTAG, "      and you just removed the last favorite");
                    // user just removed the last favorite, so now their favs list is empty
                    // I indicate 'no movie id' with -1
                    editor.putInt(mContext.getString(R.string.key_currently_selected_favorite_id), -1);
                } else if(currSelectedFavorite == mMovieId) {
                    Log.i(LOGTAG, "      and you just removed a favorite that happened to be the same " +
                            "as the last favorite you viewed in details fragment, so now resetting " +
                            "the 'last viewed favorite' in sharedPrefs");
                    // user just removed the favorite movie they had selected in favorites activity
                    // detail view, so just reset it to the first favorite
                    Cursor c = mContext.getContentResolver().query(
                            MovieTheaterContract.MoviesEntry.CONTENT_URI,
                            new String[]{ MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID },
                            MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE + " = ?",
                            new String[]{ "true" }, null);

                    if(c != null && c.moveToFirst()) {
                        editor.putInt(mContext.getString(R.string.key_currently_selected_favorite_id),
                                // just grab the first record in the db that is a favorite
                                c.getInt(0));
                        c.close();
                    }
                }

                // decrement the favorites counter and update in sharedPrefs
                editor.putInt(mContext.getString(R.string.key_num_favorites), initialNumFavorites - 1);
                editor.commit();
                Log.i(LOGTAG,  "        numFavorites was decremented, now: " + (initialNumFavorites - 1));

                // delete images from local storage
                deleteSavedImaged();
            }
            // ADD TO FAVORITES:
            else {
                if(initialNumFavorites == 0) {
                    // the only way for this code to execute is if the user just added a movie to
                    // their favs list and they didn't have any favorites before, so this is their one
                    // and only favorite.. in which case set it to be their 'selected' favorite so
                    // that favorites activity can politely load a details fragment when the user
                    // navigates to there, other times the last selected favorite is loaded
                    editor.putInt(mContext.getString(R.string.key_currently_selected_favorite_id), mMovieId);
                }
                editor.putInt(mContext.getString(R.string.key_num_favorites), initialNumFavorites + 1);
                editor.commit();
                Log.i(LOGTAG,  "      after incrementing, numFavorites is now: " + (initialNumFavorites + 1));

                // save images to local device storage
                saveImagesLocally();
            }

        }
        
    }


    /**
     * Queries the movies and credits tables in the db for all records matching mMovieId, grabs the file
     * path from each entry, adds it to a list, and then deletes all the locally stored images.
     */
    private void deleteSavedImaged() {
        // build a list of file paths to delete and then wipe them all out at once
        ArrayList<String> filePathsToDelete = new ArrayList<>();

        // get cursor pointed at relevant movies table row
        Cursor cursor = mContext.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                projection,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) }, null);

        if(cursor != null && cursor.moveToFirst()) {
            // the poster URI is stored in the db with 'file:' at beginning because that's the way
            // Picasso wants it, so strip that out to get the simple file path that android will
            // understand in order to delete the file
            try {
                Uri uriPoster = Uri.parse(cursor.getString(COLUMN_POSTER_FILE_PATH));
                filePathsToDelete.add(uriPoster.getPath());
//            Log.e(LOGTAG, "  and path extracted from db column for poster image is: " + uriPoster.getPath());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            // similar for backdrop image path

            try {
                Uri uriBackdrop = Uri.parse(cursor.getString(COLUMN_BACKDROP_FILE_PATH));
                filePathsToDelete.add(uriBackdrop.getPath());
//            Log.e(LOGTAG, "  and path extracted from db column for backdrop image is: " + uriBackdrop.getPath());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

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

                // strip the file path from the URI and add it to the list of paths to delete
                try {
                    Uri uri = Uri.parse(cursor.getString(0));
                    filePathsToDelete.add(uri.getPath());
//                Log.e(LOGTAG, "  and path extracted from db column for credit file is: " + uri.getPath());
                } catch (Exception npe) {
                    npe.printStackTrace();
                }
                cursor.moveToNext();
            }
            cursor.close();
        }

        // and finally delete all the files
        for (String s : filePathsToDelete) {
            File file = new File(s);
            boolean wasDeleted = file.delete();
            Log.i(LOGTAG, "    file with path: " + s + " was deleted: " + wasDeleted);
        }

    }


    /**
     * Queries the movies and credits tables to get themoviedb image URL for all records that match
     * mMovieId, then uses Picasso to save the images locally, using a custom Picasso Target.
     * Picasso automatically grabs images from it's cache, so typically no additional network
     * activity is necessary.
     */
    private void saveImagesLocally() {
        // update movies table file path columns for poster and backdrop images
        Cursor cursor = mContext.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                projection,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) }, null);

        if (cursor != null && cursor.moveToFirst()) {
            // save the poster image
            Picasso.with(mContext)
                    .load(cursor.getString(COLUMN_POSTER_PATH))
                    .into(new PicassoTarget(cursor.getString(COLUMN_POSTER_PATH),
                            MovieTheaterContract.MoviesEntry.COLUMN_POSTER_FILE_PATH,
                            MovieTheaterContract.MoviesEntry.TABLE_NAME));

            // save the backdrop image
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
                // some movies have less than 4 credits, so bail out in that case
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


    /**
     * Implements Picasso Target interface such that when onBitmapLoaded is called, the bitmap
     * is converted to a jpg and stored on the local device.  The appropriate db tables are updated
     * to point to the newly created files.
     */
    private class PicassoTarget implements Target {
        String fileName;
        String dbColumnToInsert;
        String tableName;
        String theMovieDbImagePath;


        /**
         * Creates a new PicassoTarget which will save the bitmap that Picasso loads to local
         * device storage.  The file NAME will be the same as themoviedb uses, but the file PATH
         * will point to the image on the local device.
         *
         * @param theMovieDbImagePath The URL of the image on themoviedb server
         * @param dbColumnNameToInsert The db column name to update with the new file's path
         * @param tableName The db table name to update (either movies or credits)
         */
        public PicassoTarget(String theMovieDbImagePath, String dbColumnNameToInsert, String tableName) {
//            Uri uri = null;
            try {
                // extract the filename from themoviedb's URL for the image, makes sense to use the same
                // filename here, it's just a bunch of random alphanumeric characters
                fileName = Uri.parse(theMovieDbImagePath).getLastPathSegment();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }


//            fileName = uri.getLastPathSegment();

            dbColumnToInsert = dbColumnNameToInsert;
            this.tableName = tableName;
            this.theMovieDbImagePath = theMovieDbImagePath;
        }


        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            try {
                // create a new File object
                File file = new File(mContext.getExternalFilesDir(null), fileName);
                FileOutputStream ostream = new FileOutputStream(file);

                // this actually writes the file to local device storage
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                ostream.flush();
                ostream.close();

                // get the file path of the just created file and update the appropriate db table and column
                String localFilePath = file.getPath();
                Log.i(LOGTAG, "saved image, the device local file path is: " + localFilePath);

                ContentValues contentValues = new ContentValues();
                // Picasso need the file path to start with 'file:' when it loads favorites
                contentValues.put(dbColumnToInsert, "file:" + localFilePath);

                if(tableName.equals(MovieTheaterContract.MoviesEntry.TABLE_NAME)) {
                    // update the movies table so poster and backdrop file path columns point to
                    // the correct place on the local device
                    mContext.getContentResolver().update(
                            MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                            contentValues,
                            MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{String.valueOf(mMovieId)});
                }
                else {
                    // update the credits table similarly, except the credits table can have
                    // multiple entries with the same movieId, so query for the one record that
                    // has the same themoviedb image URL, but UPDATE the profile_file_path column
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
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}

    }


    /**
     * Toggles the column is_favorite in all db tables for records that match mMovieId.  The records
     * will be updated such that their is_favorite column will all be changed to !initialFavState.
     *
     * @param initialFavState pass true if this movie was already a favorite before the user
     *                        clicked the favorite FAB, pass false otherwise
     */
    private void toggleIsFavoriteInAllTables(boolean initialFavState) {
        // I am not particularly proud of this... it's slow and obnoxious and really it would be nice
        // if all the db work in this class was done off the main thread.  I just need to get this
        // project done so I'm leaving it as-is for now.

        ContentValues contentValues = new ContentValues();
        contentValues.put("is_favorite", String.valueOf(!initialFavState));
        
        int numMovieRecordsUpated = mContext.getContentResolver().update(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                contentValues,
                MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{ String.valueOf(mMovieId) });
//        Log.i(LOGTAG, "      num movies table records updated: " + numMovieRecordsUpated);

        int numVideosRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.VideosEntry.buildVideosUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
//        Log.i(LOGTAG, "      num videos table records updated: " + numVideosRecordsUpdated);

        int numCreditsRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.CreditsEntry.buildCreditsUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
//        Log.i(LOGTAG, "      num credits table records updated: " + numCreditsRecordsUpdated);

        int numReviewsRecordsUpdated = mContext.getContentResolver().update(
                MovieTheaterContract.ReviewsEntry.buildReviewsUriFromMovieId(mMovieId),
                contentValues,
                null,
                null);
//        Log.i(LOGTAG, "      num reviews table records updated: " + numReviewsRecordsUpdated);
        
    }
    
    
}