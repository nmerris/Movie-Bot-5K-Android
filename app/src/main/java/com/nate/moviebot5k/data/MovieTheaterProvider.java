package com.nate.moviebot5k.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nate.moviebot5k.SingleFragmentActivity;

/**
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterProvider extends ContentProvider {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "MovieThtrProvdr";

    // the URI matcher used by this ContentProvider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // and the constants that will be used in switch statements for various ContentProvider methods
    static final int MOVIES_ALL = 1; // used in MovieGridFragment to display all movie posters
    static final int MOVIE_WITH_MOVIE_ID = 2; // used in MovieDetailFragment to display 1 movie
    static final int FAVORITES_ALL = 3; // used in MovieGridFragment to display all favorites posters
    static final int FAVORITE_WITH_MOVIE_ID = 4; // used in MovieDetailFragment to display 1 movie
    static final int GENRES_ALL = 5; // used to populate genre spinner
    static final int CERTS_ALL = 6; // used to populate certifications spinner







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


    @Override
    public boolean onCreate() {
        return false;
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


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
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
