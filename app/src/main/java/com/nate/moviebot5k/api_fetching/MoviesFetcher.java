package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.R;
import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.data.MovieTheaterContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Nathan Merris on 5/9/2016.
 */
public class MoviesFetcher {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MoviesFetcher";

    private Context mContext; // used to retrieve String resources for API queries

    public MoviesFetcher(Context context) { mContext = context; }



    public int fetchMovies() {
        Log.i(LOGTAG, "entered fetchMovies");

        int numMoviesFetched = 0;

        // compile a list to use as query params for the discover movie endpoint
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String selectedCert = sharedPrefs
                .getString(mContext.getString(R.string.key_movie_filter_cert), "");
        int selectedYear = sharedPrefs
                .getInt(mContext.getString(R.string.key_movie_filter_year), -1);
        int selectedGenre = sharedPrefs
                .getInt(mContext.getString(R.string.key_movie_filter_genre_id), -1);
        String selectedSortBy = sharedPrefs
                .getString(mContext.getString(R.string.key_movie_filter_sortby), "");

        try { // build the URL for themoviedb GET for genres
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_authority))
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie") // https://api.themoviedb.org/3/discover/movie/
                    .appendQueryParameter("certification_country", "US"); // US movies only

            if(!selectedCert.equals(mContext.getString(R.string.default_movie_filter_cert, ""))) {
                // if "Any Certification" is not currently selected, query by whatever is selected
                builder.appendQueryParameter("certification", selectedCert);
            }

            if(selectedYear != -1) {
                // if "Any Year" (which is represented by -1) is not currently selected, then query
                // by whatever is selected
                builder.appendQueryParameter("primary_release_year", String.valueOf(selectedYear));
            }

            if(selectedGenre != -1) {
                // if "Any Genre" is not currently selected, query by the genre id that is selected
                builder.appendQueryParameter("with_genres", String.valueOf(selectedGenre));
            }

            // if you don't specify a min number of votes, you end up with really bogus
            // results, esp when querying by highest rated, because even a single vote of
            // 10/10 for some oddball movie will be returned..
            if(selectedCert.equals("NC-17")) // there are few NC-17 movies, so lower min vote count
                builder.appendQueryParameter("vote_count.gte", "15");
            else // arbitrarily set the min num votes for all other
                builder.appendQueryParameter("vote_count.gte", "20");

            // every query will have a sort by parameter
            builder.appendQueryParameter("sort_by", selectedSortBy);

            // every query will have an API key
            builder.appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY);

            String url = builder.build().toString();
            Log.i(LOGTAG, "  just built URL: " + url);


            String jsonString = Utility.getUrlString(url); // call getUrlString, which will query themoviedb API
            Log.i(LOGTAG, "    Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString); // convert the returned data to a JSON object
            numMoviesFetched = parseMoviesAndInsertToDb(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

        return numMoviesFetched;

    }


    private int parseMoviesAndInsertToDb(JSONObject jsonBody) throws JSONException {
        Log.i(LOGTAG, "entered parseMoviesAndInsertToDb");
        
        JSONArray moviesJsonArray = jsonBody.getJSONArray("results");
        int numMovies = moviesJsonArray.length();
        Vector<ContentValues> valuesVector = new Vector<>(numMovies);

        for (int i = 0; i < numMovies; i++) {
            // get a single JSON object from jsonBody
            JSONObject jsonObject = moviesJsonArray.getJSONObject(i);
            ContentValues values = new ContentValues();

            // extract the data from the json object and put it in a single ContentValues object
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID, jsonObject.getInt("id"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_ADULT, jsonObject.getInt("adult"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_OVERVIEW, jsonObject.getString("overview"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_RELEASE_DATE, jsonObject.getString("release_date"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_ORIGINAL_TITLE, jsonObject.getString("original_title"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_TITLE, jsonObject.getString("title"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_BACKDROP_PATH, jsonObject.getString("backdrop_path"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_POPULARITY, jsonObject.getLong("popularity"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_VOTE_COUNT, jsonObject.getInt("vote_count"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_HAS_VIDEO, jsonObject.getInt("video"));
            values.put(MovieTheaterContract.MoviesEntry.COLUMN_VOTE_AVG, jsonObject.getLong("vote_average"));

            // and get the genre ids out of the nested json array
            JSONArray genresJsonArray = jsonObject.getJSONArray("genre_ids");
            try {
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID1, genresJsonArray.getInt(0));
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID2, genresJsonArray.getInt(1));
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID3, genresJsonArray.getInt(2));
                values.put(MovieTheaterContract.MoviesEntry.COLUMN_GENRE_ID4, genresJsonArray.getInt(3));
            } catch (JSONException e) {
                Log.i(LOGTAG, "  there were less than 4 genres associated with this movie, not a problem");
            }
            
            // add the single object to the ContentValues Vector
            valuesVector.add(values);

            // arbitrarily print out some data for debug
            Log.d(LOGTAG, "  added movie id: " + jsonObject.getString("certification"));
            Log.d(LOGTAG, "  and movie title: " + jsonObject.getInt("order"));
            Log.d(LOGTAG, "  and genre_id_1: " + genresJsonArray.getInt(0));
        }


        return 0;
//        return numInserted;
    }


}
