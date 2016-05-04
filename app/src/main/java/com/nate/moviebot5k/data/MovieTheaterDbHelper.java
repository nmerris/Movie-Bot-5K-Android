package com.nate.moviebot5k.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nate.moviebot5k.SingleFragmentActivity;


/**
 * Manages a local database for movies, favorites, genres, and certifications data.
 *
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterDbHelper extends SQLiteOpenHelper {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "MovieTheaterDbHelper";


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie_theater.db";

    public MovieTheaterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOGTAG, "entered onCreate");

        // create a table to hold the currently 'showing' movies, used by MovieGridFragment
//        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
//                LocationEntry._ID + " INTEGER PRIMARY KEY," +
//                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
//                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
//                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
//                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " +
//                " );";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
