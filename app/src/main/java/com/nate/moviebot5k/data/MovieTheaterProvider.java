package com.nate.moviebot5k.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.ActivitySingleFragment;
import com.nate.moviebot5k.data.MovieTheaterContract.MoviesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CertsEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CreditsEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.VideosEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.ReviewsEntry;


/**
 * A Homebrewed content provider.  I am aware that third pary libraries exist to make this
 * easier, but I needed the practice.
 *
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


    static UriMatcher buildUriMatcher() {
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
        // used to get writable and readable database objects throughout this ContentProvider
        mOpenHelper = new MovieTheaterDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIES_ALL:
            case GENRES_ALL:
            case CERTS_ALL:
            case VIDEOS_ALL:
            case REVIEWS_ALL:
            case CREDITS_ALL:
//                Log.i(LOGTAG, "  uri matched to switch statement case for ALL records from one of the tables, but selection and selectionArgs will be honored");
//                Log.i(LOGTAG, "    and table name being used for mOpenHelper.query, after extracting from uri," +
//                                " is: " + uri.getLastPathSegment());

                retCursor = mOpenHelper.getReadableDatabase().query(
                        uri.getLastPathSegment(),  // get table name from uri
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("I do not understand this URI: " + uri);

        }
        return retCursor;
    }


    @Override
    public String getType(Uri uri) {
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


    // due to the way this app is designed, there is no need to every do a single insert
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.e(LOGTAG, "NO IMPLEMENTATION FOR INSERT... SHOULD ALWAYS USE BULK INSERT IN THIS APP!!!");

        throw new UnsupportedOperationException("this dB only allows BULK inserts");
    }


    // overriding to make it more efficient by using beginTransaction and endTransaction
    // bulkInsert should be used for all insert operations on all tables..
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;

        switch (sUriMatcher.match(uri)){
            case MOVIES_ALL:
            case GENRES_ALL:
            case CERTS_ALL:
            case CREDITS_ALL:
            case VIDEOS_ALL:
            case REVIEWS_ALL:
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
                return returnCount;

            case CREDITS_WITH_MOVIE_ID:
            case VIDEOS_WITH_MOVIE_ID:
            case REVIEWS_WITH_MOVIE_ID:
                throw new UnsupportedOperationException("!!!! DO NOT BULKINSERT TO THE CREDITS, VIDEOS, OR " +
                        "REVIEWS TABLE WITH PATH ENDING IN MOVIE_ID B/C IN THESE TABLES MOVIE_ID DOES " +
                        "NOT HAVE A UNIQUE CONSTRAINT");

            default:
                Log.i(LOGTAG, "  UhOh.. somehow super.bulkInsert is about to be called, should never happen");
                return super.bulkInsert(uri, values);
        }
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case MOVIES_ALL:
            case CERTS_ALL:
            case GENRES_ALL:
            case CREDITS_ALL:
            case VIDEOS_ALL:
            case REVIEWS_ALL:
                rowsDeleted = db.delete(uri.getLastPathSegment(), selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("delete only supports wiping out the entire" +
                        "movies, genres, or certifications tables, OR a single record from favorites");

        }

        return rowsDeleted;
    }


    // the movies table is updated with a few more columns of data in MovieDetailsFetcher,
    // additionally ANY table can be updated when user selects or deselects a favorite movie, which
    // also triggers updates to all credits, reviews, and videos records with the same movie_id
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case MOVIES_ALL:
            case CREDITS_ALL:
//                Log.i(LOGTAG, "  and about to update MOVIES_ALL or CREDITS_ALL, all parameter being passed in will be honored");
                rowsUpdated = db.update(uri.getLastPathSegment(), values, selection, selectionArgs);
                break;

            case MOVIE_WITH_MOVIE_ID:
//                Log.i(LOGTAG, "  and about to update movies table after matching uri to MOVIE_WITH_MOVIE_ID");
//                Log.i(LOGTAG, "    and am ignoring selectionArgs, got if from URI, it is: " + uri.getLastPathSegment());
//                Log.i(LOGTAG, "      and static selection string is: " + sMovieWithMovieIdSelection);

                rowsUpdated =  db.update(
                        MoviesEntry.TABLE_NAME, values,
                        sMovieWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });
                break;

            case VIDEOS_WITH_MOVIE_ID:
//                Log.i(LOGTAG, "  and about to update videos table after matching uri to VIDEO_WITH_MOVIE_ID");
//                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                rowsUpdated =  db.update(
                        VideosEntry.TABLE_NAME, values,
                        sVideosWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });
                break;

            case CREDITS_WITH_MOVIE_ID:
//                Log.i(LOGTAG, "  and about to update movies table after matching uri to CREDIT_WITH_MOVIE_ID");
//                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                rowsUpdated = db.update(
                        CreditsEntry.TABLE_NAME, values,
                        sCreditsWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });
                break;

            case REVIEWS_WITH_MOVIE_ID:
//                Log.i(LOGTAG, "  and about to update movies table after matching uri to REVIEW_WITH_MOVIE_ID");
//                Log.i(LOGTAG, "    and am ignoring selection and selectionArgs, will just get movieId from URI");

                rowsUpdated = db.update(
                        ReviewsEntry.TABLE_NAME, values,
                        sReviewsWithMovieIdSelection, // "movie_id = ? "
                        new String[]{ uri.getLastPathSegment() });
                break;

            default:
                throw new UnsupportedOperationException("This DB only allows updates to the movies, credits, videos, or reviews tables" +
                        " and URI should end with movieId");

        }
        return rowsUpdated;
    }

}
