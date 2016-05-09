package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Nathan Merris on 5/6/2016.
 */
public class GenresFetcher {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "GenresFetcher";

    private Context mContext; // used to retrieve String resources for API queries

    public GenresFetcher(Context context) { mContext = context; }


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
                    // grab the api key from the gradle 'app' build file
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


        JSONArray genresJsonArray = jsonBody.getJSONArray("genres");
        int numInserted = 0;
        int numGenres = genresJsonArray.length();

        // Vector is synchronized, prob. not necessary here because this code should only be reached
        // once each time StartupActivity runs, unlike when the movies table is updated which is
        // more likely to have simultaneous movies table writes
        Vector<ContentValues> valuesVector = new Vector<>(numGenres);


        // iterate through all the genres and convert each one to a ContentValues that genres
        // table will understand
        for (int i = 0; i < numGenres; i++) {
            // get a single moviedb genre JSON object from jsonBody
            JSONObject genreJsonObject = genresJsonArray.getJSONObject(i);
            ContentValues values = new ContentValues();

            // extract the data from the json object and put it in a single ContentValues object
            values.put(GenresEntry.COLUMN_GENRE_ID, genreJsonObject.getInt("id"));
            values.put(GenresEntry.COLUMN_GENRE_NAME, genreJsonObject.getString("name"));

            // add the single object to the ContentValues Vector
            valuesVector.add(values);

            Log.d(LOGTAG, "  added genre id: " + genreJsonObject.getInt("id"));
            Log.d(LOGTAG, "  and genre name: " + genreJsonObject.getString("name"));
        }



        if(valuesVector.size() > 0) { // no point in doing anything if no genres could be obtained
            // TODO: can get rid of numDeleted after testing


            Log.i(LOGTAG, "  about to wipe out old genre data, calling delete with uri: " + GenresEntry.CONTENT_URI);
            // wipe out the old data
            int numDeleted = mContext.getContentResolver()
                    .delete(GenresEntry.CONTENT_URI, null, null);

            Log.i(LOGTAG, "    number or records deleted: " + numDeleted);


            Log.i(LOGTAG, "      about to call bulkInsert with the same URI");
            // insert the new data
            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
            valuesVector.toArray(valuesArray);

            numInserted = mContext.getContentResolver()
                    .bulkInsert(GenresEntry.CONTENT_URI, valuesArray);

            Log.i(LOGTAG, "        and number of records inserted: " + numInserted);

        }

        Log.d(LOGTAG, "        before return from parseGenresAndInsertToDb, numInserted (genres) is: " + numInserted);
        return numInserted;
    }

}
