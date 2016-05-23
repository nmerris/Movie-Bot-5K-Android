package com.nate.moviebot5k.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.ActivitySingleFragment;
import com.nate.moviebot5k.data.MovieTheaterContract.MoviesEntry;
//import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CertsEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CreditsEntry;
//import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesCreditsEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.VideosEntry;
//import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesVideosEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.ReviewsEntry;
//import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesReviewsEntry;

import java.util.List;


/**
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterProvider extends ContentProvider {
    private static final String LOGTAG = ActivitySingleFragment.N8LOG + "MovieThtrProvdr";

    private MovieTheaterDbHelper mOpenHelper;

    // the URI matcher used by this ContentProvider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // and the constants that will be used in switch statements for various ContentProvider methods
    static final int MOVIES_ALL = 1; // used in FragmentMovieGrid to display all movie posters
    static final int MOVIE_WITH_MOVIE_ID = 2; // used in MovieDetailFragment to display 1 movie

    static final int GENRES_ALL = 5; // used to populate genre spinner
    static final int CERTS_ALL = 6; // used to populate certifications spinner

    static final int CREDITS_WITH_MOVIE_ID = 7;
    static final int VIDEOS_WITH_MOVIE_ID = 8;
    static final int REVIEWS_WITH_MOVIE_ID = 9;

    static final int CREDITS_ALL = 13;
    static final int VIDEOS_ALL = 14;
    static final int REVIEWS_ALL = 15;

    // SQL selection statements
    private static final String sMovieWithMovieIdSelection = MoviesEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String sCreditsWithMovieIdSelection = CreditsEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String sVideosWithMovieIdSelection = VideosEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String sReviewsWithMovieIdSelection = ReviewsEntry.COLUMN_MOVIE_ID + " = ? ";


//    private static final String sFavoritesReviewsWithMovieIdSelection = ReviewsEntry.COLUMN_MOVIE_ID + " = ? ";

//    private static final SQLiteQueryBuilder sMovieDetails;
//    private static final SQLiteQueryBuilder sFavoriteDetails;


//    // inner join of tables to be used to retrieve all movie data for the details view
//    static {
//        sMovieDetails = new SQLiteQueryBuilder();
//
//        sMovieDetails.setTables(
//                // "movies INNER JOIN reviews ON movies.movie_id = reviews.movie_id"
//                MoviesEntry.TABLE_NAME + " INNER JOIN " +
//                ReviewsEntry.TABLE_NAME + " ON " + MoviesEntry.TABLE_NAME + "." +
//                MoviesEntry.COLUMN_MOVIE_ID + " = " + ReviewsEntry.TABLE_NAME + "." +
//                ReviewsEntry.COLUMN_MOVIE_ID +
//                // "INNER JOIN credits ON movies.movie_id = credits.movie_id"
//                " INNER JOIN " + CreditsEntry.TABLE_NAME + " ON " + MoviesEntry.TABLE_NAME + "." +
//                MoviesEntry.COLUMN_MOVIE_ID + " = " + CreditsEntry.TABLE_NAME +
//                "." + CreditsEntry.COLUMN_MOVIE_ID +
//                // "INNER JOIN videos ON movies.movie_id = videos.movie_id"
//                " INNER JOIN " + VideosEntry.TABLE_NAME + " ON " + MoviesEntry.TABLE_NAME + "." +
//                MoviesEntry.COLUMN_MOVIE_ID + " = " + VideosEntry.TABLE_NAME +
//                "." + VideosEntry.COLUMN_MOVIE_ID);
//
//        // testing
//        Log.i(LOGTAG, "********* sMovieDetails SQL: " + MoviesEntry.TABLE_NAME + " INNER JOIN " +
//                ReviewsEntry.TABLE_NAME + " ON " + MoviesEntry.TABLE_NAME + "." +
//                MoviesEntry.COLUMN_MOVIE_ID + " = " + ReviewsEntry.TABLE_NAME + "." +
//                ReviewsEntry.COLUMN_MOVIE_ID +
//                // "INNER JOIN credits ON movies.movie_id = credits.movie_id"
//                " INNER JOIN " + CreditsEntry.TABLE_NAME + " ON " + MoviesEntry.TABLE_NAME + "." +
//                MoviesEntry.COLUMN_MOVIE_ID + " = " + CreditsEntry.TABLE_NAME +
//                "." + CreditsEntry.COLUMN_MOVIE_ID +
//                // "INNER JOIN videos ON movies.movie_id = videos.movie_id"
//                " INNER JOIN " + VideosEntry.TABLE_NAME + " ON " + MoviesEntry.TABLE_NAME + "." +
//                MoviesEntry.COLUMN_MOVIE_ID + " = " + VideosEntry.TABLE_NAME +
//                "." + VideosEntry.COLUMN_MOVIE_ID);
//
//    }
//
//    static {
//        sFavoriteDetails = new SQLiteQueryBuilder();
//
//        sFavoriteDetails.setTables(
//                // "favorites INNER JOIN reviews ON favorites.movie_id = reviews.movie_id"
//                FavoritesEntry.TABLE_NAME + " INNER JOIN " +
//                        ReviewsEntry.TABLE_NAME + " ON " + FavoritesEntry.TABLE_NAME + "." +
//                        FavoritesEntry.COLUMN_MOVIE_ID + " = " + ReviewsEntry.TABLE_NAME + "." +
//                        ReviewsEntry.COLUMN_MOVIE_ID +
//                        // "INNER JOIN credits ON favorites.movie_id = credits.movie_id"
//                        " INNER JOIN " + CreditsEntry.TABLE_NAME + " ON " + FavoritesEntry.TABLE_NAME + "." +
//                        FavoritesEntry.COLUMN_MOVIE_ID + " = " + CreditsEntry.TABLE_NAME +
//                        "." + CreditsEntry.COLUMN_MOVIE_ID +
//                        // "INNER JOIN videos ON favorites.movie_id = videos.movie_id"
//                        " INNER JOIN " + VideosEntry.TABLE_NAME + " ON " + FavoritesEntry.TABLE_NAME + "." +
//                        FavoritesEntry.COLUMN_MOVIE_ID + " = " + VideosEntry.TABLE_NAME +
//                        "." + VideosEntry.COLUMN_MOVIE_ID);
//
//        // testing
//        Log.i(LOGTAG, "********* sFavoriteDetails SQL: " + FavoritesEntry.TABLE_NAME + " INNER JOIN " +
//                ReviewsEntry.TABLE_NAME + " ON " + FavoritesEntry.TABLE_NAME + "." +
//                FavoritesEntry.COLUMN_MOVIE_ID + " = " + ReviewsEntry.TABLE_NAME + "." +
//                ReviewsEntry.COLUMN_MOVIE_ID +
//                // "INNER JOIN credits ON favorites.movie_id = credits.movie_id"
//                " INNER JOIN " + CreditsEntry.TABLE_NAME + " ON " + FavoritesEntry.TABLE_NAME + "." +
//                FavoritesEntry.COLUMN_MOVIE_ID + " = " + CreditsEntry.TABLE_NAME +
//                "." + CreditsEntry.COLUMN_MOVIE_ID +
//                // "INNER JOIN videos ON favorites.movie_id = videos.movie_id"
//                " INNER JOIN " + VideosEntry.TABLE_NAME + " ON " + FavoritesEntry.TABLE_NAME + "." +
//                FavoritesEntry.COLUMN_MOVIE_ID + " = " + VideosEntry.TABLE_NAME +
//                "." + VideosEntry.COLUMN_MOVIE_ID);
//
//    }


    static UriMatcher buildUriMatcher() {
        Log.i(LOGTAG, "entered buildUriMatcher");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieTheaterContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieTheaterContract.PATH_MOVIES, MOVIES_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_MOVIES + "/#", MOVIE_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieTheaterContract.PATH_GENRES, GENRES_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_CERTS, CERTS_ALL);

        matcher.addURI(authority, MovieTheaterContract.PATH_VIDEOS + "/#", VIDEOS_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieTheaterContract.PATH_CREDITS + "/#", CREDITS_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieTheaterContract.PATH_REVIEWS + "/#", REVIEWS_WITH_MOVIE_ID);

        matcher.addURI(authority, MovieTheaterContract.PATH_CREDITS, CREDITS_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_VIDEOS, VIDEOS_ALL);
        matcher.addURI(authority, MovieTheaterContract.PATH_REVIEWS, REVIEWS_ALL);

        return matcher;
    }


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
            case GENRES_ALL:
            case CERTS_ALL:
            case VIDEOS_ALL:
            case REVIEWS_ALL:
            case CREDITS_ALL:
                Log.i(LOGTAG, "  uri matched to switch statement case for ALL records from one of the tables, but selection and selectionArgs will be honored");
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

            default:
                throw new UnsupportedOperationException("I do not understand this URI: " + uri);

        }

        // notify whatever is using this cursor when the data it points to changes
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }


    @Override
    public String getType(Uri uri) {
        Log.i(LOGTAG, "entered getType");

        switch (sUriMatcher.match(uri)) {
            case MOVIES_ALL:
                return MoviesEntry.CONTENT_TYPE;
            case MOVIE_WITH_MOVIE_ID:
                return MoviesEntry.CONTENT_ITEM_TYPE;
            case GENRES_ALL:
                return GenresEntry.CONTENT_TYPE;
            case CERTS_ALL:
                return CertsEntry.CONTENT_TYPE;

            case CREDITS_WITH_MOVIE_ID:
                return CreditsEntry.CONTENT_ITEM_TYPE;
            case VIDEOS_WITH_MOVIE_ID:
                return VideosEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_WITH_MOVIE_ID:
                return ReviewsEntry.CONTENT_ITEM_TYPE;

            case CREDITS_ALL:
                return CreditsEntry.CONTENT_TYPE;
            case VIDEOS_ALL:
                return VideosEntry.CONTENT_TYPE;
            case REVIEWS_ALL:
                return ReviewsEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
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
        Log.i(LOGTAG, "NO IMPLEMENTATION FOR INSERT... SHOULD ALWAYS USE BULK INSERT IN THIS APP!!!");

//        if(sUriMatcher.match(uri) == FAVORITE_WITH_MOVIE_ID) {
//            Log.i(LOGTAG, "  about to insert to favorites table: " + uri);
//
//            // incoming ContentValues MUST contain a poster and backdrop image file path
//            // with the correct keys/column names so that the insert to favorites table will be ok
//            if(values.get(FavoritesEntry.COLUMN_POSTER_FILE_PATH) == null
//                    || values.get(FavoritesEntry.COLUMN_BACKDROP_FILE_PATH) == null) {
//                throw new UnsupportedOperationException("You must include a local poster and backdrop" +
//                        " image file path in the ContentValues passed to MovieTheaterProvider.insert." +
//                        "  If you want to insert anything else, use bulkInsert");
//            }
//
//
//            final SQLiteDatabase dbFavoritesWriter = mOpenHelper.getWritableDatabase();
//
//            // get the integer value of the movieId, which is the last path segment in the incoming Uri
//            String movieIdStr = uri.getLastPathSegment();
//
//            // the SQL command to copy a record from movies to favorites, minus the poster/backdrop paths
//            String SQL_COPY_MOVIE_RECORD_TO_FAVORITES_TABLE =
//                    "INSERT INTO " + FavoritesEntry.TABLE_NAME +
//                    " SELECT * FROM " + MoviesEntry.TABLE_NAME + " WHERE " +
//                    MoviesEntry.COLUMN_MOVIE_ID + " = " + movieIdStr + ";";
//
//            Log.i(LOGTAG, "    SQL that will be executed to copy record from movies table to" +
//                    " favorites table: " + SQL_COPY_MOVIE_RECORD_TO_FAVORITES_TABLE);
//            dbFavoritesWriter.execSQL(SQL_COPY_MOVIE_RECORD_TO_FAVORITES_TABLE);
//
//
//            // now update the record that was just added to include the poster and backdrop file paths
//            int rowsUpdated;
//            rowsUpdated = dbFavoritesWriter.update(
//                    FavoritesEntry.TABLE_NAME,      // "favorites"
//                    values,                         // date for poster and backdrop file paths
//                    FavoritesEntry.COLUMN_MOVIE_ID, // WHERE clause so only the correct record is updated
//                    new String[] {movieIdStr}       // WHERE arg
//            );
//
//            // maybe I should bail out here if rowsUpdated is not 1??
//            Log.i(LOGTAG, "      the number of rows updated after adding poster and backdrop" +
//                    " path ContentValues data (should be 1) is: " + rowsUpdated);
//
//
//            // TODO: similar to delete, need to figure out if should call notifyChange on the entire table,
//            // or just the individual record pointed to by the uri passed in.. in this case since we
//            // are inserting a record, maybe this is the way to go (notifying the entire table) ???
//
//
//            // notify any content observers that a row was inserted to favorites table
//            getContext().getContentResolver().notifyChange(MoviesEntry.CONTENT_URI, null);
//            // return the URI of the new favorites record
//            return FavoritesEntry.buildFavoriteUriFromMovieId(Long.valueOf(movieIdStr));
//
//        }

        throw new UnsupportedOperationException("this dB only allows BULK inserts");

    }


    // overriding to make it more efficient with beginTransaction and endTransaction
    // bulkInsert should be used every time for movies, certs, genres, reviews, videos, credits..
    // basically everything except when a single record is being inserted to the favorites table
    // unlike insert, here bukInsert does not do any special copying, it's up to the caller to
    // figure out what ContentValues to insert, and where
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.i(LOGTAG, "entered bulkInsert");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;

        switch (sUriMatcher.match(uri)){

            case MOVIES_ALL:
            case GENRES_ALL:
            case CERTS_ALL:
            case CREDITS_ALL:
            case VIDEOS_ALL:
            case REVIEWS_ALL:
                Log.i(LOGTAG, "  uri.getLastPathSegment, ie table name to bulkInsert into: " + uri.getLastPathSegment());

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
                break;

            case CREDITS_WITH_MOVIE_ID:
            case VIDEOS_WITH_MOVIE_ID:
            case REVIEWS_WITH_MOVIE_ID:
                throw new UnsupportedOperationException("!!!! DO NOT BULKINSERT TO THE CREDITS, VIDEOS, OR" +
                        "REVIEWS TABLE WITH PATH ENDING IN MOVIE ID B/C EACH RECORD ALREADY HAS THE MOVIE_ID IN IT");

            default:
                Log.i(LOGTAG, "  UhOh.. somehow super.bulkInsert is about to be called, should never happen");
                super.bulkInsert(uri, values);
        }

        // notify any content observers that the table data has changed, will be either
        // movies, genres, or certifications in this case
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;

    }


//    /**
//     * Only allowed to delete entire movies, certifications, or genres tables, for example:
//     * "content://com.nate.moviebot5k/movies" will wipe out the entire movies table.
//     *
//     * Only other allowed uri is to delete a single favorites table record with a movieId as the last segment
//     * in the uri, for example:
//     * "content://com.nate.moviebot5k/favorites/[movieId]"
//     *
//     * All other uri's will throw an unsupported operation exception.
//     *
//     * @param uri the uri of the data to delete
//     * @param selection not used, ok to always be null
//     * @param selectionArgs not used, ok to always be null
//     * @return the number of records deleted
//     * @throws UnsupportedOperationException
//     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(LOGTAG, "entered delete");

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case MOVIES_ALL:
            case CERTS_ALL:
            case GENRES_ALL:
            case CREDITS_ALL:
            case VIDEOS_ALL:
            case REVIEWS_ALL:
                Log.i(LOGTAG, "  about to wipe out records from table: " + uri.getLastPathSegment());

                rowsDeleted = db.delete(uri.getLastPathSegment(), selection, selectionArgs);

                // in these cases, the uri points to the entire table
                getContext().getContentResolver().notifyChange(uri, null);
                break;

            default:
                throw new UnsupportedOperationException("delete only supports wiping out the entire" +
                        "movies, genres, or certifications tables, OR a single record from favorites");

        }


        // TODO: if it turns out that calling notifyChange on the uri exactly as it is passed in to
        // this method is the way to go, just move the notifyChange code out of the switch to here,
        // and do a quick check if(rowsDeleted != 0) before calling it


        Log.i(LOGTAG, "  number of rowsDeleted: " + rowsDeleted);
        return rowsDeleted;
    }


    // the movies table is update with a few more columns of data in MovieDetailsFetcher,
    // additionally ANY table can be updated when user selects or deselects a favorte movie, which
    // also triggers updates to all credits, reviews, and videos records with the same movie_id
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(LOGTAG, "entered update");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case MOVIE_WITH_MOVIE_ID:
                Log.i(LOGTAG, "  and about to update movies table after matching uri to MOVIE_WITH_MOVIE_ID");
                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                return db.update(
                        MoviesEntry.TABLE_NAME, values,
                        sMovieWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });

            case VIDEOS_WITH_MOVIE_ID:
                Log.i(LOGTAG, "  and about to update videos table after matching uri to VIDEO_WITH_MOVIE_ID");
                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                return db.update(
                        VideosEntry.TABLE_NAME, values,
                        sVideosWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });

            case CREDITS_WITH_MOVIE_ID:
                Log.i(LOGTAG, "  and about to update movies table after matching uri to CREDIT_WITH_MOVIE_ID");
                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                return db.update(
                        CreditsEntry.TABLE_NAME, values,
                        sCreditsWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });

            case REVIEWS_WITH_MOVIE_ID:
                Log.i(LOGTAG, "  and about to update movies table after matching uri to REVIEW_WITH_MOVIE_ID");
                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                return db.update(
                        ReviewsEntry.TABLE_NAME, values,
                        sReviewsWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });

            default:
                throw new UnsupportedOperationException("This DB only allows updates to the movies, credits, videos, or reviews tables" +
                        " and URI should end with movieId");

        }

    }


    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
