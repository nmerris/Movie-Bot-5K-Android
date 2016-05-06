package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;
import com.nate.moviebot5k.data.MovieTheaterProvider;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nathan Merris on 5/6/2016.
 */
public class FetchGenresTask {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "FetchGenresTask";

    private Context mContext; // used to retrieve String resources for API queries

    public FetchGenresTask(Context context) { mContext = context; }


    /**
     * Fetches all of the available genres from themoviedb.  The resulting json body is passed to
     * parseGenresAndInsertToDb, which converts updates the genres db table.
     *
     * @return the number of genres that were fetched, 0 if there was a problem
     *
     */
    public int fetchAvailableGenres() {
        Log.i(LOGTAG, "entered fetchAvailableGenres");

        int numGenresFetched = 0;

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
            String jsonString = Utility.getUrlString(url); // call getUrlString, which will query themoviedb API
            Log.i(LOGTAG, "  Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString); // convert the returned data to a JSON object

            // parseGenresAndInsertToDb fills availableGenres, all it needs is a reference to it and a JSONObject
            numGenresFetched = parseGenresAndInsertToDb(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

        return numGenresFetched;

    }


    /**
     * Takes a json body containing all available themoviedb genres, parses it, and updates the
     * genres table in the database via MovieTheaterProvider.
     * The moviedb genre for 'Foreign' is stripped out here.
     *
     * @param jsonBody the unparsed json body to devour
     * @return the number of genres that were successfully inserted to the db
     */
    private int parseGenresAndInsertToDb(JSONObject jsonBody)
            throws IOException, JSONException {
        Log.i(LOGTAG, "entered parseGenresAndInsertToDb");

        // START HERE: need to the the ContentValues Vector<> thing here, fill it

        ContentValues cv = new ContentValues();
        JSONArray genresJsonArray = jsonBody.getJSONArray("genres");

        for (int i = 0; i < genresJsonArray.length(); i++) {
            // get a single moviedb genre JSON object from jsonBody
            JSONObject genreJsonObject = genresJsonArray.getJSONObject(i);

            cv.put();

                    genreJsonObject.getInt("id"),
                    genreJsonObject.getString("name")


        }

        // TODO: can get rid of numDeleted and numInserted after this has been tested

        Log.i(LOGTAG, "  about to wipe out old genre data, calling delete with uri: " + GenresEntry.CONTENT_URI);
        // wipe out the old data
        int numDeleted = mContext.getContentResolver()
                .delete(GenresEntry.CONTENT_URI, null, null);

        Log.i(LOGTAG, "    number or records deleted: " + numDeleted);

        Log.i(LOGTAG, "      about to call bulkInsert with the same URI");
        // insert the new data
        int numInserted =  mContext.getContentResolver()
                .bulkInsert(GenresEntry.CONTENT_URI, ?????);

        Log.i(LOGTAG, "        and number of records inserted: " + numInserted);

        return numInserted;
    }

}
