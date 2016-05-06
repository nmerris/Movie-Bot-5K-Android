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
//    private static final String sMovieWithMovieIdSelection = MoviesEntry.TABLE_NAME + "." +
//            MoviesEntry.COLUMN_MOVIE_ID + " = ? ";  // "movies.movie_id = ?"
//    private static final String sFavoriteWithMovieIdSelection = FavoritesEntry.TABLE_NAME + "." +
//            FavoritesEntry.COLUMN_MOVIE_ID + " = ? "; // "favorites.movie_id = ?"
    private static final String sMovieWithMovieIdSelection = MoviesEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String sFavoriteWithMovieIdSelection = FavoritesEntry.COLUMN_MOVIE_ID + " = ? ";


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


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(LOGTAG, "entered query");
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIES_ALL:
            case FAVORITES_ALL:
            case GENRES_ALL:
            case CERTS_ALL:
                Log.i(LOGTAG, "  uri matched to switch statement case for either MOVIES_ALL," +
                        " FAVORITES_ALL, GENRES_ALL, or CERTS_ALL");
                Log.i(LOGTAG, "    and table name being used for mOpenHelper.query, after extracting from uri," +
                                " is: " + uri.getLastPathSegment());

                retCursor = mOpenHelper.getReadableDatabase().query(
                        uri.getLastPathSegment(),  // get table name from uri
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIE_WITH_MOVIE_ID:
                Log.i(LOGTAG,"  uri matched to switch statement MOVIE_WITH_MOVIE_ID");
                Log.i(LOGTAG,"    ignoring selection passed in, instead using: " + sMovieWithMovieIdSelection);
                Log.i(LOGTAG,"    with the selectionArg (should be movieId) passed in: " + selectionArgs);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        sMovieWithMovieIdSelection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITE_WITH_MOVIE_ID:
                Log.i(LOGTAG,"  uri matched to switch statement FAVORITE_WITH_MOVIE_ID");
                Log.i(LOGTAG,"    ignoring selection passed in, instead using: " + sFavoriteWithMovieIdSelection);
                Log.i(LOGTAG,"    with the selectionArg (should be movieId) passed in: " + selectionArgs);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavoritesEntry.TABLE_NAME,
                        projection,
                        sFavoriteWithMovieIdSelection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("I do not understand this URI: " + uri);

        }

        // notify whatever is using this cursor when the data it points to changes
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;


//        Log.i(LOGTAG, "entered query");
//        Cursor retCursor;
//
//        switch (sUriMatcher.match(uri)) {
//            case MOVIES_ALL:
//                Log.i(LOGTAG, "  matched uri to case MOVIES_ALL");
//
//
//
//                Log.i(LOGTAG, "    and table name being used for mOpenHelper.query, after extracting from uri," +
//                        " is: " + uri.getLastPathSegment());
//                retCursor = mOpenHelper.getReadableDatabase().query( // get table name from uri
//                        uri.getLastPathSegment(),
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder);
//
//                break;
//
//            case FAVORITES_ALL:
//
//
//
//                break;
//
//
//            case MOVIE_WITH_MOVIE_ID:
//
//
//                break;
//
//            case FAVORITE_WITH_MOVIE_ID:
//
//
//
//                break;
//
//            case GENRES_ALL:
//
//
//                break;
//
//            case CERTS_ALL:
//
//
//                break;
//
//            default:
//                throw new UnsupportedOperationException("I do not understand this URI: " + uri);
//
//        }
//
//        // notify whatever is using this cursor when the data it points to changes
//        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
//        return retCursor;

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

            // incoming ContentValues MUST contain a poster and backdrop image file path
            // with the correct keys/column names so that the insert to favorites table will be ok
            if(values.get(FavoritesEntry.COLUMN_POSTER_FILE_PATH) == null
                    || values.get(FavoritesEntry.COLUMN_BACKDROP_FILE_PATH) == null) {
                throw new UnsupportedOperationException("You must include a local poster and backdrop" +
                        " image file path in the ContentValues passed to MovieTheaterProvider.insert." +
                        "  If you want to insert anything else, use bulkInsert");
            }


            final SQLiteDatabase dbFavoritesWriter = mOpenHelper.getWritableDatabase();

            // get the integer value of the movieId, which is the last path segment in the incoming Uri
            String movieIdStr = uri.getLastPathSegment();

            // the SQL command to copy a record from movies to favorites, minus the poster/backdrop paths
            String SQL_COPY_MOVIE_RECORD_TO_FAVORITES_TABLE =
                    "INSERT INTO " + FavoritesEntry.TABLE_NAME +
                    " SELECT * FROM " + MoviesEntry.TABLE_NAME + " WHERE " +
                    MoviesEntry.COLUMN_MOVIE_ID + " = " + movieIdStr + ";";

            Log.i(LOGTAG, "    SQL that will be executed to copy record from movies table to" +
                    " favorites table: " + SQL_COPY_MOVIE_RECORD_TO_FAVORITES_TABLE);
            dbFavoritesWriter.execSQL(SQL_COPY_MOVIE_RECORD_TO_FAVORITES_TABLE);


            // now update the record that was just added to include the poster and backdrop file paths
            int rowsUpdated;
            rowsUpdated = dbFavoritesWriter.update(
                    FavoritesEntry.TABLE_NAME,      // "favorites"
                    values,                         // date for poster and backdrop file paths
                    FavoritesEntry.COLUMN_MOVIE_ID, // WHERE clause so only the correct record is updated
                    new String[] {movieIdStr}       // WHERE arg
            );

            // maybe I should bail out here if rowsUpdated is not 1??
            Log.i(LOGTAG, "      the number of rows updated after adding poster and backdrop" +
                    " path ContentValues data (should be 1) is: " + rowsUpdated);

            // notify any content observers that a row was inserted to favorites table
            getContext().getContentResolver().notifyChange(MoviesEntry.CONTENT_URI, null);
            // return the URI of the new favorites record
            return FavoritesEntry.buildFavoriteUriFromMovieId(Long.valueOf(movieIdStr));

        }
        else {
            throw new UnsupportedOperationException("this db only allows individual inserts to the" +
                    " favorites table, use bulkInsert for everything else");
        }
    }


    // overriding to make it more efficient with beginTransaction and endTransaction
    // bulkInsert should be used every time for movies, certs, and genres tables
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

            Log.i(LOGTAG, "    number records inserted: " + returnCount);
        }
        else {
            Log.i(LOGTAG, "  UhOh.. somehow super.bulkInsert is about to be called, should never happen");
            super.bulkInsert(uri, values);
        }

        // notify any content observers that the table data has changed, will be either
        // movies, genres, or certifications in this case
        getContext().getContentResolver().notifyChange(uri, null);
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
