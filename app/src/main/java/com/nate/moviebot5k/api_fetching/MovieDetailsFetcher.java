package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nate.moviebot5k.ActivitySingleFragment;
import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.R;
import com.nate.moviebot5k.Utility;
import com.nate.moviebot5k.data.MovieTheaterContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Nathan Merris on 5/17/2016.
 */
public class MovieDetailsFetcher {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovDetlsFetchr";

    private Context mContext; // used to retrieve String resources for API queries
    private int mMovieId; // the movieId this fetcher be fetchin'

    public MovieDetailsFetcher(Context context, int movieId) {
        mContext = context;
        mMovieId = movieId;
    }


    // I can't believe that Void and void are different.......
    // needs to be Void because the async task that launches it needs it to return Void
    public Void fetchMovieDetails() {
        Log.i(LOGTAG, "entered fetchMovieDetails");

        // get the currently selected movieId to use for API queries
        int movieId = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(mContext.getString(R.string.key_currently_selected_movie_id), 0);
        Log.i(LOGTAG, "  and the movieId to be used to fetch movie details is: " + movieId);

        try { // build the URL for themoviedb GET
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_authority))
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(movieId)) // https://api.themoviedb.org/3/movie/{id}
                    .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY)

                    // use the convenient append_to_response parameter to get all the json in one shot
                    .appendQueryParameter("append_to_response", "videos,reviews,credits");

            String url = builder.build().toString();
            Log.i(LOGTAG, "  just built URL: " + url);

            String jsonString = Utility.getUrlString(url); // call getUrlString, which will query themoviedb API
            Log.i(LOGTAG, "    Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString); // convert the returned data to a JSON object

            // parse and insert the movie details to the appropriate db tables
            // ie the movies table will get 4 more columsn of data, and the credits, reviews, and
            // videos tables will be updated with data, everything is tied to it's movieId as usual
            parseJsonAndInsertToDb(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

        return null;
    }




    private void parseJsonAndInsertToDb(JSONObject jsonBody) throws JSONException {
        Log.i(LOGTAG, "entered parseJsonAndInsertToDb (in movie details fetcher)");

//        Vector<ContentValues> valuesVector = new Vector<>();
        ContentValues values = new ContentValues();
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_BUDGET, "budget");
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_REVENUE, "revenue");
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_RUNTIME, "runtime");
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_TAGLINE, "tagline");
        int numInserted = mContext.getContentResolver()
                .update(MovieTheaterContract.MoviesEntry.CONTENT_URI,
                        new ContentValues[]{ values });






        JSONArray moviesJsonArray = jsonBody.getJSONArray("results");

        int numMovies = moviesJsonArray.length();
        int numInserted = 0;
        Vector<ContentValues> valuesVector = new Vector<>(numMovies);


        for (int i = 0; i < numMovies; i++) {
            // get a single JSON object from jsonBody
            JSONObject jsonObject = moviesJsonArray.getJSONObject(i);
            ContentValues values = new ContentValues();

            // extract the data from the json object and put it in a single ContentValues object
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID, jsonObject.getLong("id")); // NOT NULL COLUMN
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_VOTE_COUNT, jsonObject.getLong("vote_count"));

            values.put(MovieTheaterContract.MoviesEntry.COLUMN_OVERVIEW, jsonObject.getString("overview")); // NOT NULL COLUMN
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_RELEASE_DATE, jsonObject.getString("release_date")); // NOT NULL COLUMN
//            values.put(MoviesEntry.COLUMN_ORIGINAL_TITLE, jsonObject.getString("original_title"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_TITLE, jsonObject.getString("title")); // NOT NULL COLUMN

//            values.put(MoviesEntry.COLUMN_ORIGINAL_LANGUAGE, jsonObject.getString("original_language"));

//            values.put(MoviesEntry.COLUMN_HAS_VIDEO, jsonObject.getString("video"));
//            values.put(MoviesEntry.COLUMN_ADULT, jsonObject.getString("adult"));

            values.put(MovieTheaterContract.MoviesEntry.COLUMN_POPULARITY, jsonObject.getDouble("popularity")); // NOT NULL COLUMN
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_VOTE_AVG, jsonObject.getDouble("vote_average")); // NOT NULL COLUMN

//            values.put(MoviesEntry.COLUMN_BACKDROP_PATH, jsonObject.getString("backdrop_path")); // NOT NULL COLUMN
//            values.put(MoviesEntry.COLUMN_POSTER_PATH, jsonObject.getString("poster_path")); // NOT NULL COLUMN


            // put the fully formed image URL's in the db, this URL will point to an image size that
            // is appropriate for the device this app is running on
            Uri.Builder backdropImageUrlBuilder = new Uri.Builder();
            backdropImageUrlBuilder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_image_authority))
                    .appendPath("t").appendPath("p")
                    .appendPath(mContext.getString(R.string.themoviedb_backdrop_size))
                    .build(); // https://image.tmdb.org/t/p/[image_size] to this point

            String backdropImageUrl = backdropImageUrlBuilder.build().toString()
                    + jsonObject.getString("backdrop_path");
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_PATH, backdropImageUrl); // NOT NULL COLUMN

            Uri.Builder posterImageUrlBuilder = new Uri.Builder();
            posterImageUrlBuilder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_image_authority))
                    .appendPath("t").appendPath("p")
                    .appendPath(mContext.getString(R.string.themoviedb_poster_size))
                    .build(); // https://image.tmdb.org/t/p/[image_size] to this point
            String posterImageUrl = posterImageUrlBuilder.build().toString()
                    + jsonObject.getString("poster_path");
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_POSTER_PATH, posterImageUrl); // NOT NULL COLUMN


            // and get the genre ids out of the nested json array, ok to be NULL
            JSONArray genresJsonArray = jsonObject.getJSONArray("genre_ids");
            try {
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID1, genresJsonArray.getInt(0));
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID2, genresJsonArray.getInt(1));
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID3, genresJsonArray.getInt(2));
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID4, genresJsonArray.getInt(3));
            } catch (JSONException e) {
//                Log.i(LOGTAG, "  there were less than 4 genres associated with this movie, this is not an error");
            }


            // print the data for all the NON NULL columns
            Log.d(LOGTAG, "  added movie id: " + jsonObject.getLong("id"));
//            Log.d(LOGTAG, "  and overview: " + jsonObject.getString("overview"));
//            Log.d(LOGTAG, "  and release_date: " + jsonObject.getString("release_date"));
//            Log.d(LOGTAG, "  and movie title: " + jsonObject.getString("title"));
//            Log.d(LOGTAG, "  and backdrop_path: " + jsonObject.getString("backdrop_path"));
//            Log.d(LOGTAG, "  and poster_path: " + jsonObject.getString("poster_path"));
//            Log.d(LOGTAG, "  and popularity: " + jsonObject.getDouble("popularity"));
//            Log.d(LOGTAG, "  and vote_avg: " + jsonObject.getDouble("vote_average"));
//
//            // print the data for genre id(s)
//            Log.d(LOGTAG, "  and genre_id_1: " + genresJsonArray.getInt(0));
////            Log.d(LOGTAG, "  and genre_id_2: " + genresJsonArray.getInt(1));
////            Log.d(LOGTAG, "  and genre_id_3: " + genresJsonArray.getInt(2));
////            Log.d(LOGTAG, "  and genre_id_4: " + genresJsonArray.getInt(3));


            // add the single object to the ContentValues Vector
            valuesVector.add(values);
        }

        if(valuesVector.size() > 0) { // no point in doing anything if no data could be obtained
            // TODO: can get rid of numDeleted after testing

            Log.i(LOGTAG, "  about to wipe out old movies table data, calling delete with uri: " + MovieTheaterContract.MoviesEntry.CONTENT_URI);
            // wipe out the old data
            int numDeleted = mContext.getContentResolver()
                    .delete(MovieTheaterContract.MoviesEntry.CONTENT_URI, null, null);

            Log.i(LOGTAG, "    number or records deleted: " + numDeleted);


            Log.i(LOGTAG, "      about to call bulkInsert with the same URI");
            // insert the new data
            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
            valuesVector.toArray(valuesArray);

            numInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.MoviesEntry.CONTENT_URI, valuesArray);
        }

        Log.d(LOGTAG, "        before return from parseMoviesAndInsertToDb, numInserted is: " + numInserted);
    }




}
