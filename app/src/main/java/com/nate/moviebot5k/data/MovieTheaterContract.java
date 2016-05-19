package com.nate.moviebot5k.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.nate.moviebot5k.ActivitySingleFragment;

/**
 * Defines the table and column names for movie_theater SQLite database.
 *
 * Created by Nathan Merris on 5/4/2016.
 */
public class MovieTheaterContract {
    private static final String LOGTAG = ActivitySingleFragment.N8LOG + "MovieThetrContrct";

    public static final String CONTENT_AUTHORITY = "com.nate.moviebot5k";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_GENRES = "genres";
    public static final String PATH_CERTS = "certifications";
    public static final String PATH_CREDITS = "credits";
    public static final String PATH_VIDEOS = "videos";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_FAVORITES_CREDITS = "favorites_credits";
    public static final String PATH_FAVORITES_VIDEOS = "favorites_videos";
    public static final String PATH_FAVORITES_REVIEWS = "favorites_reviews";
    
//    public static final String PATH_MOVIE_DETAILS = PATH_MOVIES + "/details";
//    public static final String PATH_FAVORITE_DETAILS = PATH_FAVORITES + "/details";





    // private but not final: private to protect it from malevolent code goblins outside this class,
    // not final because MoviesEntry and FavoritesEntry extend it
    private static class MoviesEntryColumns {

        // populated in FragmentMovieGrid from /discover/movie themoviedb API endpoint
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_GENRE_ID1 = "genre_id1";
        public static final String COLUMN_GENRE_NAME1 = "genre_name1";
        public static final String COLUMN_GENRE_ID2 = "genre_id2";
        public static final String COLUMN_GENRE_NAME2 = "genre_name2";
        public static final String COLUMN_GENRE_ID3 = "genre_id3";
        public static final String COLUMN_GENRE_NAME3 = "genre_name3";
        public static final String COLUMN_GENRE_ID4 = "genre_id4";
        public static final String COLUMN_GENRE_NAME4 = "genre_name4";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_HAS_VIDEO = "has_video";
        public static final String COLUMN_VOTE_AVG = "vote_average";

        // populated in MovieDetailFragment from /movie/id themoviedb API endpoint
        public static final String COLUMN_BUDGET = "budget";
        public static final String COLUMN_REVENUE = "revenue";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_TAGLINE = "tagline";
    }
    
    
    // both FavoritesReviewsEntry and ReviewsEntry use these same columns
    private static class ReviewsEntryColumns {
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
    }
    
    
    // both FavoritesCreditsEntry and CreditsEntry use these same columns
    private static class CreditsEntryColumns {
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_CHARACTER = "character";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ORDER = "credits_order";
        public static final String COLUMN_PROFILE_PATH = "profile_path";
    }
    
    
    // both FavoritesVideosEntry and VideosEntry use these same columsn
    private static class VideosEntryColumns {
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";
    }

    
    /**
     * Defines the table contents that FragmentMovieGrid and MovieDetailFragment will access when
     * hosted by ActivityHome and MovieDetailPagerActivity (in phone mode).
     * Basically this will be the table that is used when user is NOT viewing their favorites.
     * It will only ever hold one set of data, and will be written over completely every time a
     * FetchMoviesTask returns at least 1 movie.
     */
    public static final class MoviesEntry extends MoviesEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/movies"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String TABLE_NAME = "movies";
        
        public static final String DETAILS_PATH = "details";

        
        // returns a movie Uri (a single record in this table) given a MOVIE id
        // MovieTheaterProvider's uri matcher will know what to do if any of it's db related
        // methods receive a call with a uri as described below, which would look like:
        // "content://com.nate.moviebot5k/movies/[movieId]"
        // used by MovieDetailFragment to get a cursor that points to a movie with id = movieId
        // NOTE: movieId is not the same as the primary key, which is just _id
        public static Uri buildMovieUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }
        
        // use to get the joined table of movie details joined with videos, reviews, and credits tables
        public static Uri buildMovieDetailsUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId).buildUpon()
                    .appendPath(DETAILS_PATH).build();
        }
        
    }


    /**
     * Defines the table contents that FragmentMovieGrid and MovieDetailFragment will access when
     * hosted by FavoritesActivity and FavoritesPagerActivity (in phone mode).
     * Basically this will be the table that is used when user is viewing their favorites, which
     * can be done with or without an internet connection.  The data in this table is identical to
     * MoviesEntry except that it has 2 additional columns to hold the file paths for the poster
     * and backdrop images, since they are stored locally.  The records in this table are inserted
     * or deleted one at a time.
     */
    public static final class FavoritesEntry extends MoviesEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/favorites"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String TABLE_NAME = "favorites";

        public static final String DETAILS_PATH = "details";


        // 2 additional columns needed to store local poster and backdrop image file paths
        // so that the favorites table can be accessed while offline
        public static final String COLUMN_POSTER_FILE_PATH = "poster_file_path";
        public static final String COLUMN_BACKDROP_FILE_PATH = "backdrop_file_path";


        // returns a favorte Uri (a single record in this table) given a MOVIE id
        // TODO: this is not going to work, needs to use .appendQueryParameter (??)
        // .withAppendedId will only work for the primary key _id, I think
        public static Uri buildFavoriteUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        // use to get the joined table of favorite details joined with videos, reviews, and credits tables
        public static Uri buildFavoriteDetailsUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId).buildUpon()
                    .appendPath(DETAILS_PATH).build();
        }

    }

    

    // I am choosing not to store performer profile images locally, so if in favorites mode and
    // no internet, there will not be an image of the actor/actress.. I need to get this project done
    // with enough time, that could be implemented
    public static final class CreditsEntry extends CreditsEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/credits"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CREDITS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CREDITS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CREDITS;
        public static final String TABLE_NAME = "credits";

        public static Uri buildCreditsUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

    }

    public static final class FavoritesCreditsEntry extends CreditsEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/favorites_credits"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES_CREDITS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES_CREDITS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES_CREDITS;
        public static final String TABLE_NAME = "favorites_credits";

        // 1 additional column needed to store the local file location for the image associated
        // with the actor/actress for each credits record
        public static final String COLUMN_PROFILE_FILE_PATH = "profile_file_path";

        public static Uri buildFavoritesCreditsUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

    }



    public static final class VideosEntry extends VideosEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/videos"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_VIDEOS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;
        public static final String TABLE_NAME = "videos";

        public static Uri buildVideosUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

    }

    public static final class FavoritesVideosEntry extends VideosEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/favorites_videos"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES_VIDEOS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES_VIDEOS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES_VIDEOS;
        public static final String TABLE_NAME = "favorites_videos";

        public static Uri buildFavoritesVideosUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

    }



    public static final class ReviewsEntry extends ReviewsEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/reviews"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEWS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String TABLE_NAME = "reviews";

        public static Uri buildReviewsUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

    }

    public static final class FavoritesReviewsEntry extends ReviewsEntryColumns implements BaseColumns {

        // "content://com.nate.moviebot5k/favorites_reviews"
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES_REVIEWS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES_REVIEWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES_REVIEWS;
        public static final String TABLE_NAME = "favorites_reviews";

        public static Uri buildFavoritesReviewsUriFromMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

    }
    
    
    
    
    /**
     * Defines the table contents that are used to populate the Genres movie filter spinner.
     * The data is updated once each time the app is started from dead, in StartupActivity.
     */
    public static final class GenresEntry implements BaseColumns {
        
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_GENRES).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GENRES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GENRES;
        public static final String TABLE_NAME = "genres";

        public static final String COLUMN_GENRE_ID = "genre_id";
        public static final String COLUMN_NAME = "genre_name";


        // should not need any build methods.. genres table is only ever wiped out completely and
        // then written over with a bulk insert

//        public static Uri buildGenreUriFromGenreName(String genreName) {
//            return CONTENT_URI.buildUpon()
//                    .appendQueryParameter(COLUMN_NAME, genreName).build();
//        }

    }


    /**
     * Defines the table contents that are used to populate the Certifications movie filter spinner.
     * The data is updated once each time the app is started from dead, in StartupActivity.
     */
    public static final class CertsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CERTS).build();
        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CERTS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CERTS;
        public static final String TABLE_NAME = "certifications";

        public static final String COLUMN_ORDER = "certification_order";
        public static final String COLUMN_NAME = "certification_name";
        public static final String COLUMN_MEANING = "certification_meaning";

        // should not need any build methods, same reason as GenresEntry

    }
    
}
