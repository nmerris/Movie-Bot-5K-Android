package com.nate.moviebot5k.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.data.MovieTheaterContract.MoviesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CertsEntry;


/**
 * Manages a local database for movies, favorites, genres, and certifications data.
 *
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterDbHelper extends SQLiteOpenHelper {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "MovTheatrDbHelper";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movie_theater.db";


    public MovieTheaterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOGTAG, "entered onCreate");

        // generally, only absolutely critical data columns use NOT NULL constraint because
        // I can just make the views work around any movies that lack some of the data
        // this SQL is used in both movies and favorites table, must be identical
        final String SQL_MOVIES_COLUMNS =
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // from BaseColumns
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MoviesEntry.COLUMN_ADULT + " INTEGER, " +
                MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                // it's okay if a movie has no associated genre, the view will just work around it
                MoviesEntry.COLUMN_GENRE_ID1 + " INTEGER, " +
                MoviesEntry.COLUMN_GENRE_NAME1 + " TEXT, " +
                MoviesEntry.COLUMN_GENRE_ID2 + " INTEGER, " +
                MoviesEntry.COLUMN_GENRE_NAME2 + " TEXT, " +
                MoviesEntry.COLUMN_GENRE_ID3 + " INTEGER, " +
                MoviesEntry.COLUMN_GENRE_NAME3 + " TEXT, " +
                MoviesEntry.COLUMN_GENRE_ID4 + " INTEGER, " +
                MoviesEntry.COLUMN_GENRE_NAME4 + " TEXT, " +
                MoviesEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
                MoviesEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
                MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MoviesEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
                MoviesEntry.COLUMN_HAS_VIDEO + " INTEGER, " +
                MoviesEntry.COLUMN_VOTE_AVG + " REAL NOT NULL, " +
                MoviesEntry.COLUMN_BUDGET + " INTEGER, " +
                MoviesEntry.COLUMN_REVENUE + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_RUNTIME + " INTEGER, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR1 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_CONTENT1 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR2 + " TEXT, " +
                // the view will have to deal with a movie with no review or video
                MoviesEntry.COLUMN_REVIEW_CONTENT2 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR3 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_CONTENT3 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR4 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_CONTENT4 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY1 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME1 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE1 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE1 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY2 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME2 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE2 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE2 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY3 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME3 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE3 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE3 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY4 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME4 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE4 + " TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE4 + " TEXT";


        // create a table to hold the currently 'showing' movies
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + "(" + 
                SQL_MOVIES_COLUMNS + ");";
        Log.i(LOGTAG, "onCreate movies table SQL: " + SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_MOVIES_TABLE);

        // create a table to hold favorites, same as movies except also has local poster
        // and backdrop paths for offline access, used by
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + "(" +
                SQL_MOVIES_COLUMNS + ", " +
                // must allow null here because when copying record from movies to favorites
                // in MovieProvider.insert, the initial sql command will leave these columns null
                // but then immediately after that they will be filled in, so you can still enforce
                // NOT NULL over there, just not directly in the database
                FavoritesEntry.COLUMN_POSTER_FILE_PATH + " TEXT, " +
                FavoritesEntry.COLUMN_BACKDROP_FILE_PATH + " TEXT);";
        Log.i(LOGTAG, "onCreate favorites table SQL: " + SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);

        // create a table to hold all available movie genres that can be used to filter
        // themoviedb API discover calls
        final String SQL_CREATE_GENRES_TABLE = "CREATE TABLE " + GenresEntry.TABLE_NAME + "(" +
                GenresEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // from BaseColumns
                GenresEntry.COLUMN_GENRE_ID + " INTEGER NOT NULL, " +
                GenresEntry.COLUMN_GENRE_NAME + " TEXT NOT NULL);";
        Log.i(LOGTAG, "onCreate genres table SQL: " + SQL_CREATE_GENRES_TABLE);
        db.execSQL(SQL_CREATE_GENRES_TABLE);

        // create a table for certifications, similar to genres table (like G, PG, PG-13, etc)
        final String SQL_CREATE_CERTS_TABLE = "CREATE TABLE " + CertsEntry.TABLE_NAME + "(" +
                CertsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // from BaseColumns
                CertsEntry.COLUMN_CERT_ORDER + " INTEGER NOT NULL, " +
                CertsEntry.COLUMN_CERT_NAME + " TEXT NOT NULL, " +
                CertsEntry.COLUMN_CERT_MEANING + " TEXT);";
        Log.i(LOGTAG, "onCreate certifications table SQL: " + SQL_CREATE_CERTS_TABLE);
        db.execSQL(SQL_CREATE_CERTS_TABLE);
                
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this is not really an upgrade strategy, it just wipes out the tables
        // so do not change the DATABASE_VERSION or everything will get wiped out!
        // you would need a strategy to preserve the users favorites here
        db.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GenresEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CertsEntry.TABLE_NAME);

        // and recreate them from scratch
        onCreate(db);
    }
}
