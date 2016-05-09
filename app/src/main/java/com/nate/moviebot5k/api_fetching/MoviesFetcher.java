package com.nate.moviebot5k.api_fetching;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.R;
import com.nate.moviebot5k.SingleFragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nathan Merris on 5/9/2016.
 */
public class MoviesFetcher {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MoviesFtcher";

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
//            numMoviesFetched = parseMovies(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

        return numMoviesFetched;

    }


    private int parseMovies(JSONObject jsonBody) {


        return 0;
    }


}
