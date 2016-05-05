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
     * Insert only allows these Uris:
     * <p>
     *     1. content://com.nate.moviebot5k/movies - wipes out every record and writes in values
     * </p>
     *     2. content://com.nate.moviebot5k/genres - same as movies
     * <p>
     *     3. content://com.nate.moviebot5k/certifications - same as movies
     * </p>
     *     4. content://com.nate.moviebot5k/favorites/[movie_id] - writes a single record to the
     *     favorites table, the values passed in must contain only the poster_file_path and
     *     backdrop_file_path datum, so use values.put(MovieTheaterContract.COLUMN_POSTER_FILE_PATH,
     *     [file path to locally stored poster image]) and then again for the backdrop file path,
     *     this method will copy all the other data from the movies table to the corresponding
     *     record in the favorites table, and also add the poster and backdrop file paths.  The point
     *     here is that the user can come back and access their favorites even without internet.
     *
     * <p>
     *     Any other Uri passe in will throw an UnsupportedOperationException
     * </p>
     *
     * @param uri where to insert the incoming values
     * @param values the data to insert
     * @return a Uri that points to the data that was just inserted
     * @throws UnsupportedOperationException
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(LOGTAG, "entered insert");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES_ALL:

                break;

            case FAVORITE_WITH_MOVIE_ID:

                break;

            case GENRES_ALL:

                break;

            case CERTS_ALL:

                break;

            default:
                throw new UnsupportedOperationException("this db only allows inserts to the entire" +
                        " movies, genres, or certifications tables, or 1 single insert with a" +
                        " movie_id to the favorites table");

        }


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
