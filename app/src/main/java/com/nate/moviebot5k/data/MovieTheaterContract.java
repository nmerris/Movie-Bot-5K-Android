package com.nate.moviebot5k.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.nate.moviebot5k.SingleFragmentActivity;

/**
 * Defines the table and column names for movie_theater SQLite database.
 *
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterContract {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "MovieTheatrContract";

    public static final String CONTENT_AUTHORITY = "com.nate.moviebot5k";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "movies";
    public static final String PATH_LOCATION = "favorites";
    public static final String PATH_GENRES = "genres";
    public static final String PATH_CERTS = "certifications";


    /**
     * Defines the table contents that MovieGridFragment and MovieDetailFragment will access when
     * hosted by HomeActivity and MovieDetailPagerActivity (in phone mode).
     * Basically this will be the table that is used when user is NOT viewing their favorites.
     * It will only ever hold one set of data, and will be written over completely every time a
     * FetchMoviesTask returns at least 1 movie.
     */
    public static final class MoviesEntry implements BaseColumns {





    }


    /**
     * Defines the table contents that MovieGridFragment and MovieDetailFragment will access when
     * hosted by FavoritesActivity and FavoritesPagerActivity (in phone mode).
     * Basically this will be the table that is used when user is viewing their favorites, which
     * can be done with or without an internet connection.  The data in this table is identical to
     * MoviesEntry except that it has 2 additional columns to hold the file paths for the poster
     * and backdrop images, since they are stored locally.  The records in this table are inserted
     * or deleted one at a time.
     */
    public static final class FavoritesEntry implements BaseColumns {





    }


    /**
     * Defines the table contents that are used to populate the Genres movie filter spinner.
     * The data is updated once each time the app is started from dead, in StartupActivity.
     */
    public static final class GenresEntry implements BaseColumns {



    }


    /**
     * Defines the table contents that are used to populate the Certifications movie filter spinner.
     * The data is updated once each time the app is started from dead, in StartupActivity.
     */
    public static final class CertsEntry implements BaseColumns {



    }



}
