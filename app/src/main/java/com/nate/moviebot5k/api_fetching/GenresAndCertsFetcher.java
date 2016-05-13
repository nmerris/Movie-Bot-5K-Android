package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.R;
import com.nate.moviebot5k.SingleFragmentActivity;
import com.nate.moviebot5k.Utility;
import com.nate.moviebot5k.data.MovieTheaterContract.GenresEntry;
import com.nate.moviebot5k.data.MovieTheaterContract.CertsEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Nathan Merris on 5/6/2016.
 */
public class GenresAndCertsFetcher {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "GnresCertsFtcher";

    private Context mContext; // used to retrieve String resources for API queries

    public GenresAndCertsFetcher(Context context) { mContext = context; }


    /**
     * Fetches all of the available genres from themoviedb.  The resulting json body is passed to
     * parseGenresAndInsertToDb, which converts updates the genres db table.  It is up to the caller
     * to determine what should happen if 0 items are returned.
     *
     * @return the number of genres that were fetched, 0 if there was a problem
     *
     */
    public int fetchAvailableGenres() {
        Log.i(LOGTAG, "entered fetchAvailableGenres");

        int numGenresFetched = 0;


        try { // build the URL for themoviedb GET for genres
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_authority))
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
            numGenresFetched = parseGenresAndInsertToDb(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

        return numGenresFetched;

    }


    public void fetchAvailableCertifications() {
        Log.i(LOGTAG, "entered fetchAvailableCertifications");

        try { // build the URL for themoviedb GET for certs
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_authority))
                    .appendPath("3")
                    .appendPath("certification")
                    .appendPath("movie")
                    .appendPath("list") // https://api.themoviedb.org/3/certification/movie/list
                            // grab the api key from the gradle 'app' build file
                    .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY);

            String url = builder.build().toString();
            String jsonString = Utility.getUrlString(url); // call getUrlString, which will query themoviedb API
            Log.i(LOGTAG, "  Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString); // convert the returned data to a JSON object
            parseCertsAndInsertToDb(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
        }

    }


    private void parseCertsAndInsertToDb(JSONObject jsonBody)
            throws IOException, JSONException {
        Log.i(LOGTAG, "entered parseCertsAndInsertToDb");

        // get the US certs array out of the certifications json object
        JSONArray certsJsonArray = jsonBody.getJSONObject("certifications").getJSONArray("US");
        int numCerts = certsJsonArray.length();

        // Vector is synchronized, prob. not necessary here because this code should only be reached
        // once each time StartupActivity runs, unlike when the movies table is updated which is
        // more likely to have simultaneous movies table writes
        Vector<ContentValues> valuesVector = new Vector<>(numCerts);

        // first add the 'Any Rating' record
        ContentValues anyCertCV = new ContentValues();
        anyCertCV.put(CertsEntry.COLUMN_CERT_ORDER, 0); // want it to be at top of list of certs
        anyCertCV.put(CertsEntry.COLUMN_CERT_NAME, mContext.getString(R.string.default_movie_filter_cert));
        anyCertCV.put(CertsEntry.COLUMN_CERT_MEANING, mContext.getString(R.string.default_movie_filter_cert));
        valuesVector.add(anyCertCV);

        // iterate through all the genres and convert each one to a ContentValues that certs
        // table will understand
        for (int i = 0; i < numCerts; i++) {
            // get a single moviedb genre JSON object from jsonBody
            JSONObject certJsonObject = certsJsonArray.getJSONObject(i);
            ContentValues values = new ContentValues();

            // extract the data from the json object and put it in a single ContentValues object
            values.put(CertsEntry.COLUMN_CERT_ORDER, certJsonObject.getInt("order"));
            values.put(CertsEntry.COLUMN_CERT_NAME, certJsonObject.getString("certification"));
            values.put(CertsEntry.COLUMN_CERT_MEANING, certJsonObject.getString("meaning"));

            // add the single object to the ContentValues Vector
            valuesVector.add(values);

//            Log.d(LOGTAG, "  added certification name: " + certJsonObject.getString("certification"));
//            Log.d(LOGTAG, "  and certification order: " + certJsonObject.getInt("order"));
        }

        // greater than 1 below because there will always be a single 'Any Rating' in valuesVector
        if(valuesVector.size() > 1) { // no point in doing anything if no genres could be obtained
            // TODO: can get rid of numDeleted and numInserted after testing

            Log.i(LOGTAG, "  about to wipe out old certification table data, calling delete with uri: " + CertsEntry.CONTENT_URI);
            // wipe out the old data
            int numDeleted = mContext.getContentResolver()
                    .delete(CertsEntry.CONTENT_URI, null, null);

            Log.i(LOGTAG, "    number or records deleted: " + numDeleted);

            Log.i(LOGTAG, "      about to call bulkInsert with the same URI");
            // insert the new data
            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
            valuesVector.toArray(valuesArray);

            int numInserted = mContext.getContentResolver()
                    .bulkInsert(CertsEntry.CONTENT_URI, valuesArray);

            Log.i(LOGTAG, "        and number of records inserted: " + numInserted);

        }

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
        
        // first add the 'Any Genre' record
        ContentValues anyGenreCV = new ContentValues();
        anyGenreCV.put(GenresEntry.COLUMN_GENRE_ID, mContext.getString(R.string.default_movie_filter_genre_id));
        anyGenreCV.put(GenresEntry.COLUMN_GENRE_NAME, mContext.getString(R.string.spinner_genre_any_genre_label));
        valuesVector.add(anyGenreCV);

        // iterate through all the genres and convert each one to a ContentValues that genres
        // table will understand
        for (int i = 0; i < numGenres; i++) {
            // get a single moviedb genre JSON object from jsonBody
            JSONObject genreJsonObject = genresJsonArray.getJSONObject(i);
            ContentValues cv = new ContentValues();

            // ignore "Foreign" genre, there are too few results to be useful
            if(genreJsonObject.getString("name").equals("Foreign")) continue;
            
            // extract the data from the json object and put it in a single ContentValues object
            cv.put(GenresEntry.COLUMN_GENRE_ID, genreJsonObject.getInt("id"));
            cv.put(GenresEntry.COLUMN_GENRE_NAME, genreJsonObject.getString("name"));

            // add the single object to the ContentValues Vector
            valuesVector.add(cv);

            Log.d(LOGTAG, "  added genre id: " + genreJsonObject.getInt("id"));
            Log.d(LOGTAG, "  and genre name: " + genreJsonObject.getString("name"));
        }


        // conditioning on greater than 1 because there will always be a single 'Any Genre' in the Vector
        if(valuesVector.size() > 1) { // no point in doing anything if no genres could be obtained
            // TODO: can get rid of numDeleted after testing

            Log.i(LOGTAG, "  about to wipe out old genre table data, calling delete with uri: " + GenresEntry.CONTENT_URI);
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
        }

        Log.d(LOGTAG, "        before return from parseGenresAndInsertToDb, numInserted is: " + numInserted);
        return numInserted;
    }

}
