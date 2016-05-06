package com.nate.moviebot5k.api_fetching;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.SingleFragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nathan Merris on 5/6/2016.
 */
public class FetchGenresTask {

    private final String LOGTAG = SingleFragmentActivity.N8LOG + "FetchGenresTask";

    private Context mContext; // used to retrieve String resources for API queries

    public FetchGenresTask(Context context) { mContext = context; }



    /**
     * Fetches all of the available genres from themoviedb.  The resulting json body is passed to
     * parseGenres, which converts updates the genres db table.
     *
     */
    public void fetchAvailableGenres() {

        try { // build the URL for themoviedb GET for genres

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("genre")
                    .appendPath("movie")
                    .appendPath("list") // https://api.themoviedb.org/3/genre/movie/list
                    .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY);

            String url = builder.build().toString();
            String jsonString = getUrlString(url); // call getUrlString, which will query themoviedb API
            //Log.i(LOGTAG, "Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString); // convert the returned data to a JSON object

            // parseGenres fills availableGenres, all it needs is a reference to it and a JSONObject
            parseGenres(availableGenres, jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

        return availableGenres;
    }

    /**
     * Takes a json body containing all available themoviedb genres, parses it, and packages it all
     * into a list of Genre objects.  A Genre object representing 'Any Genre' is created here.  The
     * moviedb genre for 'Foreign' is stripped out here.
     *
     * @param availableGenres the list of Genres that this method should put the data after parsing it
     * @param jsonBody the unparsed json body to devour
     */
    private void parseGenres(List<MovieTheater.Genre> availableGenres, JSONObject jsonBody)
            throws IOException, JSONException {

        // themoviedb doesn't give a 'search all genres' json object, so make one here
        MovieTheater.Genre anyGenreObject = new MovieTheater.Genre(
                -1, // -1 is the id for 'Any Genre'
                mContext.getString(R.string.themoviedb_any_genre_filter_name_value));
        availableGenres.add(anyGenreObject);


        JSONArray genresJsonArray = jsonBody.getJSONArray("genres");

        for (int i = 0; i < genresJsonArray.length(); i++) {
            // get a single moviedb genre JSON object from jsonBody
            JSONObject genreJsonObject = genresJsonArray.getJSONObject(i);
            // create a new MovieTheater.Genre object and provide it with a genre id and name
            MovieTheater.Genre genreObject = new MovieTheater.Genre(
                    genreJsonObject.getInt("id"),
                    genreJsonObject.getString("name"));

            // I'm only searching for US movies in this app, so don't add the Foreign genre to the list
            if(genreObject.name.equals("Foreign"))
                continue;

            // add the just created object to the List
            availableGenres.add(genreObject);
            //Log.i(LOGTAG, "just put genre-id: " + genreJsonObject.getInt("id") + ", and genre-name: " + genreJsonObject.getString("name"));
        }


        // UDACITY REVIEWER, READ THIS AND TEST IF YOU WISH:
        // to test what happens if themoviedb were to change a genre name, AND it happened
        // to be the same genre that the user had currently selected in their sharedPrefs,
        // uncomment the following block of code, rerun the app, select one of the bogus genre names
        // that are now present, kill the app, recomment the lines, and run the app again..
        // the genre pref should default back to 'Any Rating'
        // if the user had any other pref selected, their selection is retained and the list
        // simply updates with the new genres from themoviedb

/*
        MovieTheater.Genre testGenreObj1 = new MovieTheater.Genre(
                "test moviedb id",
                "test if themoviedb changed this genre name");
        MovieTheater.Genre testGenreObj2 = new MovieTheater.Genre(
                "test moviedb id",
                "test if themoviedb changed this other genre name");
        availableGenres.add(testGenreObj1);
        availableGenres.add(testGenreObj2);
*/


    }
}
