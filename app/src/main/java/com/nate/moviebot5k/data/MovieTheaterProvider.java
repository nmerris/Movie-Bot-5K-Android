package com.nate.moviebot5k.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.data.MovieTheaterContract.MoviesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CertsEntry;

import javax.security.auth.login.LoginException;

/**
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterProvider extends ContentProvider {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "MovieThtrProvdr";

    private MovieTheaterDbHelper mOpenHelper;

    // the URI matcher used by this ContentProvider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // and the constants that will be used in switch statements for various ContentProvider methods
    static final int MOVIES_ALL = 1; // used in MovieGridFragment to display all movie posters
    static final int MOVIE_WITH_MOVIE_ID = 2; // used in MovieDetailFragment to display 1 movie
    static final int FAVORITES_ALL = 3; // used in MovieGridFragment to display all favorites posters
    static final int FAVORITE_WITH_MOVIE_ID = 4; // used in MovieDetailFragment to display 1 movie
    static final int GENRES_ALL = 5; // used to populate genre spinner
    static final int CERTS_ALL = 6; // used to populate certifications spinner

    // SQL selection statements
    private static final String sMovieWithMovieId = MoviesEntry.TABLE_NAME + "." +
            MoviesEntry.COLUMN_MOVIE_ID + " = ? ";  // "movies.movie_id = ?"
    private static final String sFavoriteWithMovieId = FavoritesEntry.TABLE_NAME + "." +
            FavoritesEntry.COLUMN_MOVIE_ID + " = ? "; // "favorites.movie_id = ?"


    static UriMatcher buildUriMatcher() {
        Log.i(LOGTAG, "entered buildUriMatcher");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieTheaterContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieTheaterContract.PATH_MOVIES, MOVIES_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_MOVIES + "/#", MOVIE_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieTheaterContract.PATH_FAVORITES, FAVORITES_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_FAVORITES + "/#", FAVORITE_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieTheaterContract.PATH_GENRES, GENRES_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_CERTS, CERTS_ALL);

        return matcher;
    }


//    private Cursor getMovieByMovieId() {
//
//    }


    @Override
    public boolean onCreate() {
        Log.i(LOGTAG, "entered onCreate");

        // used to get writable and readable database objects throughout this ContentProvider
        mOpenHelper = new MovieTheaterDbHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Insert only allows this Uri:
     * <p>
     *     content://com.nate.moviebot5k/favorites/[movie_id] - writes a single record to the
     *     favorites table, the values passed in must contain only the poster_file_path and
     *     backdrop_file_path datum, so use values.put(MovieTheaterContract.COLUMN_POSTER_FILE_PATH,
     *     [file path to locally stored poster image]) and then again for the backdrop file path,
     *     this method will copy all the other data from the movies table to the corresponding
     *     record in the favorites table, and also add the poster and backdrop file paths.  The point
     *     here is that the user can come back and access their favorites even without internet.
     * </p>
     * Any other Uri passe in will throw an UnsupportedOperationException.  All other insert
     * operations in this app should be done with bulkInsert because they will almost always
     * be inserting multiple records.
     *
     * @param uri the Uri that will point to the new favorites table record
     * @param values the data to insert as described above
     * @return a Uri that points to the data that was just inserted
     * @throws UnsupportedOperationException
     * @see #bulkInsert(Uri, ContentValues[])
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(LOGTAG, "entered insert");

        if(sUriMatcher.match(uri) == FAVORITE_WITH_MOVIE_ID) {
            Log.i(LOGTAG, "  about to insert to favorites table: " + uri);
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            Uri returnUri;

            // TODO: fancy insert to favorites table

        }
        else {
            throw new UnsupportedOperationException("this db only allows inserts to the entire" +
                    " movies, genres, or certifications tables, or 1 single insert with a" +
                    " movie_id to the favorites table");
        }
    }


    // overriding to make it more efficient with beginTransaction and endTransaction
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.i(LOGTAG, "entered bulkInsert");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;
        int match = sUriMatcher.match(uri);

        if(match == MOVIES_ALL || match == GENRES_ALL || match == CERTS_ALL) {
            // is this okay?  using getLastPathSegment to get the table name?
            Log.i(LOGTAG, "  uri.getLastPathSegment, ie table name to insert into: " + uri.getLastPathSegment());
            db.beginTransaction();

            try {
                for (ContentValues value : values) {
                    long _id = db.insert(uri.getLastPathSegment(), null, value);
                    if (_id != -1) returnCount++;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            getContext().getContentResolver().notifyChange(uri, null);
            Log.i(LOGTAG, "    number records inserted: " + returnCount);
        }
        else {
            super.bulkInsert(uri, values);
        }
        return returnCount;






//        switch (sUriMatcher.match(uri)) {
//            case MOVIES_ALL:
//                Log.i(LOGTAG, "  about to beginTransaction to movies table");
//
//                db.beginTransaction();
//                int returnCount = 0;
//
//                try {
//                    for (ContentValues value : values) {
//                        long _id = db.insert(MoviesEntry.TABLE_NAME, null, value);
//                        if(_id != -1) returnCount++;
//                    }
//                    db.setTransactionSuccessful();
//                }
//                finally {
//                    db.endTransaction();
//                }
//
//                getContext().getContentResolver().notifyChange(uri, null);
//                Log.i(LOGTAG, "    number records inserted: " + returnCount);
//                return returnCount;
//                break;
//
//            case GENRES_ALL:
//                Log.i(LOGTAG, "  about to beginTransaction to genres table");
//
//                db.beginTransaction();
//                int returnCount = 0;
//
//                try {
//                    for (ContentValues value : values) {
//                        long _id = db.insert(MoviesEntry.TABLE_NAME, null, value);
//                        if(_id != -1) returnCount++;
//                    }
//                    db.setTransactionSuccessful();
//                }
//                finally {
//                    db.endTransaction();
//                }
//
//                getContext().getContentResolver().notifyChange(uri, null);
//                Log.i(LOGTAG, "    number records inserted: " + returnCount);
//                return returnCount;
//            break;
//                break;
//
//            case CERTS_ALL:
//
//                break;
//
//            default:
//                return super.bulkInsert(uri, values);


//        }
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
