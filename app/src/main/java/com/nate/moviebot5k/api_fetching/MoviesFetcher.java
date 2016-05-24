package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nate.moviebot5k.BuildConfig;
import com.nate.moviebot5k.R;
import com.nate.moviebot5k.ActivitySingleFragment;
import com.nate.moviebot5k.Utility;
import com.nate.moviebot5k.data.MovieTheaterContract.MoviesEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Nathan Merris on 5/9/2016.
 */
public class MoviesFetcher {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MoviesFetcher";

    private Context mContext; // used to retrieve String resources for API queries

    public MoviesFetcher(Context context) { mContext = context; }



    public ArrayList<Integer> fetchMovies() {
        Log.i(LOGTAG, "entered fetchMovies, only possible to get here if key_fetch_new_movies in sharedPrefs is TRUE");

//        int numMoviesFetched = 0;
        ArrayList<Integer> movieIdList = new ArrayList<>();


        // compile a list to use as query params for the discover movie endpoint
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        Log.i(LOGTAG, "  but just to check, here is it's current value: "
                + sharedPrefs.getBoolean(mContext.getString(R.string.key_fetch_new_movies), false));

        String selectedCert = sharedPrefs
                .getString(mContext.getString(R.string.key_movie_filter_cert), "");
        String selectedYear = sharedPrefs
                .getString(mContext.getString(R.string.key_movie_filter_year), "");
        String selectedGenre = sharedPrefs
                .getString(mContext.getString(R.string.key_movie_filter_genre_id), "");
        String selectedSortBy = sharedPrefs
                .getString(mContext.getString(R.string.key_movie_filter_sortby_value), "");

        try { // build the URL for themoviedb GET
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_authority))
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie") // https://api.themoviedb.org/3/discover/movie/
                    .appendQueryParameter("certification_country", "US"); // US movies only

            if(!selectedCert.equals(mContext.getString(R.string.default_movie_filter_cert))) {
                // if "Any Certification" is not currently selected, query by whatever is selected
                builder.appendQueryParameter("certification", selectedCert);
            }

            if(!selectedYear.equals(mContext.getString(R.string.default_movie_filter_year))) {
                // if "Any Year" is not currently selected, then query by whatever is selected
                builder.appendQueryParameter("primary_release_year", selectedYear);
            }

            if(!selectedGenre.equals(mContext.getString(R.string.default_movie_filter_genre_id))) {
                // if "Any Genre" is not currently selected, query by the genre id that is selected
                builder.appendQueryParameter("with_genres", selectedGenre);
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
//            numMoviesFetched = parseMoviesAndInsertToDb(jsonBody);
            parseMoviesAndInsertToDb(jsonBody, movieIdList);

            // if this code is reached, there must not have been any exceptions thrown,
            // so set key_fetch_new_movies to false.. we don't really care if zero movies was return,
            // this could only happen if the user has filter criteria that is too restrictive, in
            // which case they will need to adjust their filter, and will be shown a msg
            // however if an exception is thrown, that implies a network or json error, so
            // do not set the fetch_new_movies bool to false because we want to try again in the hopes
            // that the user has network access in the future..  this is checked every time
            // FragmentMovieGrid.onResume is called, which is where this task is fired from
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(mContext.getString(R.string.key_fetch_new_movies), false);
            editor.commit();
            Log.i(LOGTAG, "      fetch had no exceptions, don't care if zero movies fetched, so just set sharedPrefs key_fetch_new_movies to ****FALSE****");


        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
            Log.i(LOGTAG, "  so sharedPrefs key_fetch_new_movies should still be true, here's what it is: "
                    + sharedPrefs.getBoolean(mContext.getString(R.string.key_fetch_new_movies), false));
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
            Log.i(LOGTAG, "  so sharedPrefs key_fetch_new_movies should still be true, here's what it is: "
                    + sharedPrefs.getBoolean(mContext.getString(R.string.key_fetch_new_movies), false));
        }

//        return numMoviesFetched;
        return movieIdList;

    }


    private void parseMoviesAndInsertToDb(JSONObject jsonBody, ArrayList<Integer> movieIdList) throws JSONException {
        Log.i(LOGTAG, "entered parseMoviesAndInsertToDb");
        
        JSONArray moviesJsonArray = jsonBody.getJSONArray("results");
        int numMovies = moviesJsonArray.length();
        int numInserted = 0;
        Vector<ContentValues> valuesVector = new Vector<>(numMovies);

//        List<Long> movieIdList = new ArrayList<>();

        for (int i = 0; i < numMovies; i++) {
            // get a single JSON object from jsonBody
            JSONObject jsonObject = moviesJsonArray.getJSONObject(i);
            ContentValues values = new ContentValues();

            // extract the data from the json object and put it in a single ContentValues object
            values.put(MoviesEntry.COLUMN_MOVIE_ID, jsonObject.getLong("id")); // NOT NULL COLUMN
            values.put(MoviesEntry.COLUMN_VOTE_COUNT, jsonObject.getLong("vote_count"));

            values.put(MoviesEntry.COLUMN_OVERVIEW, jsonObject.getString("overview")); // NOT NULL COLUMN
            values.put(MoviesEntry.COLUMN_RELEASE_DATE, jsonObject.getString("release_date")); // NOT NULL COLUMN
//            values.put(MoviesEntry.COLUMN_ORIGINAL_TITLE, jsonObject.getString("original_title"));
            values.put(MoviesEntry.COLUMN_TITLE, jsonObject.getString("title")); // NOT NULL COLUMN

//            values.put(MoviesEntry.COLUMN_ORIGINAL_LANGUAGE, jsonObject.getString("original_language"));

//            values.put(MoviesEntry.COLUMN_HAS_VIDEO, jsonObject.getString("video"));
//            values.put(MoviesEntry.COLUMN_ADULT, jsonObject.getString("adult"));

            values.put(MoviesEntry.COLUMN_POPULARITY, jsonObject.getDouble("popularity")); // NOT NULL COLUMN
            values.put(MoviesEntry.COLUMN_VOTE_AVG, jsonObject.getDouble("vote_average")); // NOT NULL COLUMN

//            values.put(MoviesEntry.COLUMN_BACKDROP_PATH, jsonObject.getString("backdrop_path")); // NOT NULL COLUMN
//            values.put(MoviesEntry.COLUMN_POSTER_PATH, jsonObject.getString("poster_path")); // NOT NULL COLUMN


            // put the fully formed image URL's in the db, this URL will point to an image size that
            // is appropriate for the device this app is running on
            Uri.Builder backdropImageUrlBuilder = new Uri.Builder();
            backdropImageUrlBuilder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_image_authority))
                    .appendPath("t").appendPath("p")
                    .appendPath(mContext.getString(R.string.themoviedb_backdrop_size)); // https://image.tmdb.org/t/p/[image_size] to this point

            String backdropImageUrl = backdropImageUrlBuilder.build().toString()
                    + jsonObject.getString("backdrop_path");
            values.put(MoviesEntry.COLUMN_BACKDROP_PATH, backdropImageUrl); // NOT NULL COLUMN

            Uri.Builder posterImageUrlBuilder = new Uri.Builder();
            posterImageUrlBuilder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_image_authority))
                    .appendPath("t").appendPath("p")
                    .appendPath(mContext.getString(R.string.themoviedb_poster_size)); // https://image.tmdb.org/t/p/[image_size] to this point
            String posterImageUrl = posterImageUrlBuilder.build().toString()
                    + jsonObject.getString("poster_path");
            values.put(MoviesEntry.COLUMN_POSTER_PATH, posterImageUrl); // NOT NULL COLUMN


            // and get the genre ids out of the nested json array, ok to be NULL
            JSONArray genresJsonArray = jsonObject.getJSONArray("genre_ids");
            try {
                values.put(MoviesEntry.COLUMN_GENRE_ID1, genresJsonArray.getInt(0));
                values.put(MoviesEntry.COLUMN_GENRE_ID2, genresJsonArray.getInt(1));
                values.put(MoviesEntry.COLUMN_GENRE_ID3, genresJsonArray.getInt(2));
                values.put(MoviesEntry.COLUMN_GENRE_ID4, genresJsonArray.getInt(3));
            } catch (JSONException e) {
//                Log.i(LOGTAG, "  there were less than 4 genres associated with this movie, this is not an error");
            }
            

            // print the data for all the NON NULL columns
            Log.d(LOGTAG, "  added movie id: " + jsonObject.getLong("id") + "  title: " + jsonObject.getString("title"));
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


            // set the is_favorite column to FALSE
            //
            values.put(MoviesEntry.COLUMN_IS_FAVORITE, "false"); // assume all are not favorites here,
            // but below the bulkinsert will reject any movies that are already in the db due to UNIQUE
            // constraint on movie_id column, so no danger of overriding a users favorites list

            values.put(MoviesEntry.COLUMN_FETCH_ORDER, i); // keep track of the order of the movies




            // need a list of movieIds so that MovieGridFragment will know which ones to select
            // from the db when it creates the loader that populates the movie grid
            movieIdList.add(jsonObject.getInt("id"));


            // check if the movie just parsed from the json is already in the db and is a favorite,
            // if it is, update it's fetch_order so that the movie grid will display it in the right
            // order along with all the other non favorites movies that may have been parsed out
            ContentValues fetchOrderCV = new ContentValues();
            fetchOrderCV.put(MoviesEntry.COLUMN_FETCH_ORDER, i);

            int favoriteFetchOrderUpdated = mContext.getContentResolver().update(
                    MoviesEntry.CONTENT_URI,
                    fetchOrderCV,
                    MoviesEntry.COLUMN_MOVIE_ID + " = ? AND " + MoviesEntry.COLUMN_IS_FAVORITE + " = ?",
                    new String[]{ jsonObject.getString("id"), "true" });
            Log.e(LOGTAG, "    IN MOVIES FETCHER, favoriteFetchOrderUpdated for movieId: " + jsonObject.getString("id") +
                " is (0 means no update performed because " +
                    "that movieId was not in db already and was not a favorite): " + favoriteFetchOrderUpdated);


            // add the single object to the ContentValues Vector
            valuesVector.add(values);
        }

        if(valuesVector.size() > 0) { // no point in doing anything if no data could be obtained
            // TODO: can get rid of numDeleted after testing

            Log.i(LOGTAG, "  about to wipe out old movies table data but only NON favorites, calling delete with uri: " + MoviesEntry.CONTENT_URI);
            // wipe out the old data
            int numDeleted = mContext.getContentResolver()
                    .delete(MoviesEntry.CONTENT_URI,
                            MoviesEntry.COLUMN_IS_FAVORITE + " = ?",
                            new String[]{ "false" });

            Log.i(LOGTAG, "    number of NON favorites records deleted: " + numDeleted);







            Log.i(LOGTAG, "      about to call bulkInsert with the same URI");
            // insert the new data
            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
            valuesVector.toArray(valuesArray);

            numInserted = mContext.getContentResolver()
                    .bulkInsert(MoviesEntry.CONTENT_URI, valuesArray);





        }

        Log.d(LOGTAG, "        before return from parseMoviesAndInsertToDb, numInserted is: " + numInserted);
//        return numInserted;
    }

}
