package com.nate.moviebot5k.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.data.MovieTheaterContract.MoviesEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.FavoritesEntry;


/**
 * Manages a local database for movies, favorites, genres, and certifications data.
 *
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterDbHelper extends SQLiteOpenHelper {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "MovTheatrDbHelper";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
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
                MoviesEntry.COLUMN_REVIEW_CONTENT1 + "TEXT, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR2 + " TEXT, " +
                // the view will have to deal with a movie with no review
                MoviesEntry.COLUMN_REVIEW_CONTENT2 + "TEXT, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR3 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_CONTENT3 + "TEXT, " +
                MoviesEntry.COLUMN_REVIEW_AUTHOR4 + " TEXT, " +
                MoviesEntry.COLUMN_REVIEW_CONTENT4 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY1 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME1 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE1 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE1 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY2 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME2 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE2 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE2 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY3 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME3 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE3 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE3 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_KEY4 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_NAME4 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_SITE4 + "TEXT, " +
                MoviesEntry.COLUMN_VIDEO_TYPE4 + "TEXT";


        // create a table to hold the currently 'showing' movies, used by MovieGridFragment
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + "(" + 
                SQL_MOVIES_COLUMNS + ");";

        Log.i(LOGTAG, "onCreate movies table SQL: " + SQL_CREATE_MOVIES_TABLE);
        //db.execSQL(SQL_CREATE_MOVIES_TABLE);
        
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + "(" +
                SQL_MOVIES_COLUMNS + ", " +
                FavoritesEntry.COLUMN_POSTER_FILE_PATH + " TEXT NOT NULL, " +
                FavoritesEntry.COLUMN_BACKDROP_FILE_PATH + " TEXT NOT NULL);";
        
        Log.i(LOGTAG, "onCreate favorites table SQL: " + SQL_CREATE_FAVORITES_TABLE);
        //db.execSQL(SQL_CREATE_FAVORITES_TABLE);
                
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
