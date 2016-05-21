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

        Log.e(LOGTAG, "in MovieDetailsFetcher contructor, mMovieId is: " + movieId);
    }


    // I can't believe that Void and void are different.......
    // needs to be Void because the async task that launches it needs it to return Void
    public Void fetchMovieDetails() {
        Log.i(LOGTAG, "entered fetchMovieDetails");

        // get the currently selected movieId to use for API queries
//        int movieId = PreferenceManager.getDefaultSharedPreferences(mContext)
//                .getInt(mContext.getString(R.string.key_currently_selected_movie_id), 0);
        Log.i(LOGTAG, "  and the movieId to be used to fetch movie details is: " + mMovieId);

        try { // build the URL for themoviedb GET
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_authority))
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(mMovieId)) // https://api.themoviedb.org/3/movie/{id}
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


        // get the relevant pieces of extra data and update the MOVIES table
        ContentValues values = new ContentValues();
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_BUDGET, jsonBody.getLong("budget")); // in dollars
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_REVENUE, jsonBody.getLong("revenue")); // in dollars
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_RUNTIME, jsonBody.getInt("runtime")); // in min
        values.put(MovieTheaterContract.MoviesEntry.COLUMN_TAGLINE, jsonBody.getString("tagline"));


        // populate up to 4 genre columns, I'm just ignoring more than that
        JSONArray genresJsonArray = jsonBody.getJSONArray("genres");
        try {
            for(int i = 0; i < 3; i++) {
                JSONObject jsonObject = genresJsonArray.getJSONObject(i);
                values.put(MovieTheaterContract.MoviesEntry.GENRE_NAMEx + (i + 1),
                        jsonObject.getString("name"));
            }
        } catch (JSONException e) {
                Log.i(LOGTAG, "  there were less than 4 genre names associated with this movie, this is not an error");
        }
        
        
        
        

        int numUpdated = mContext.getContentResolver()
                .update(MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                       values, null, null);

        Log.i(LOGTAG, "  tagline: " + jsonBody.getString("tagline"));
        Log.i(LOGTAG, "  revenue: " + jsonBody.getLong("revenue"));
        Log.i(LOGTAG, "  runtime: " + jsonBody.getLong("runtime"));
        Log.i(LOGTAG, "  budget: " + jsonBody.getLong("budget"));
        Log.i(LOGTAG, "  numRecords updated in movies table: " + numUpdated);



        // insert new records to the VIDEOS table
        JSONObject videosJsonObject = jsonBody.getJSONObject("videos");
        JSONArray videosJsonArray = videosJsonObject.getJSONArray("results");

        int numVideos = videosJsonArray.length();
        int numVideosInserted = 0;
        Vector<ContentValues> valuesVidsVector = new Vector<>(numVideos);

        for (int i = 0; i < numVideos; i++) {
            // get a single JSON object from jsonBody
            JSONObject jsonObject = videosJsonArray.getJSONObject(i);
            ContentValues valuesVideos = new ContentValues();

            // put the movieId in to the ContentValues object
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_MOVIE_ID, mMovieId);

            // extract the data from the json object and put it in a single ContentValues object
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_KEY, jsonObject.getString("key"));
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_NAME, jsonObject.getString("name"));
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_SITE, jsonObject.getString("site"));
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_SIZE, jsonObject.getInt("size"));
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_TYPE, jsonObject.getString("type"));

            String youtubeVideoThumbnailUrl = "http://img.youtube.com/vi/" +
                    jsonObject.getString("key") + "/hqdefault.jpg";
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_THUMBNAIL_URL, youtubeVideoThumbnailUrl);

            // testing
            Log.i(LOGTAG, "  added video table record with movie_id: " + mMovieId);
            Log.i(LOGTAG, "    and with key: " + jsonObject.getString("key"));
            Log.i(LOGTAG, "    and with size: " + jsonObject.getInt("size"));
            Log.i(LOGTAG, "    and with type: " + jsonObject.getString("type"));
            Log.i(LOGTAG, "    and with name: " + jsonObject.getString("name"));
            Log.e(LOGTAG, "    and with thumbnail URL: " + youtubeVideoThumbnailUrl);

            valuesVidsVector.add(valuesVideos);
        }

        Log.i(LOGTAG, "      num video records extracted from json array was: " + numVideos);

        if(valuesVidsVector.size() > 0) { // no point in doing anything if no data could be obtained
            // TODO: can get rid of numDeleted after testing

            Log.i(LOGTAG, "      about to call bulkInsert");
            // insert the new data
            ContentValues[] valuesVidsArray = new ContentValues[valuesVidsVector.size()];
            valuesVidsVector.toArray(valuesVidsArray);

            numVideosInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.VideosEntry.CONTENT_URI, valuesVidsArray);
        }
        Log.d(LOGTAG, "        num videos inserted to videos table was: " + numVideosInserted);



        // insert new records to the REVIEWS table
        JSONObject reviewsJsonObject = jsonBody.getJSONObject("reviews");
        JSONArray reviewsJsonArray = reviewsJsonObject.getJSONArray("results");

        int numReviews = reviewsJsonArray.length();
        int numReviewsInserted = 0;
        Vector<ContentValues> valuesReviewsVector = new Vector<>(numReviews);

        for (int i = 0; i < numReviews; i++) {
            // get a single JSON object from jsonBody
            JSONObject jsonObject = reviewsJsonArray.getJSONObject(i);
            ContentValues valuesReviews = new ContentValues();

            // put the movieId in to the ContentValues object
            valuesReviews.put(MovieTheaterContract.ReviewsEntry.COLUMN_MOVIE_ID, mMovieId);

            // extract the data from the json object and put it in a single ContentValues object
            valuesReviews.put(MovieTheaterContract.ReviewsEntry.COLUMN_AUTHOR, jsonObject.getString("author"));
            valuesReviews.put(MovieTheaterContract.ReviewsEntry.COLUMN_CONTENT, jsonObject.getString("content"));

            // testing
            Log.i(LOGTAG, "  added review table record with movie_id: " + mMovieId);
            Log.i(LOGTAG, "    and with author: " + jsonObject.getString("author"));
            Log.i(LOGTAG, "    and with content: " + jsonObject.getString("content"));

            // add the single object to the ContentValues Vector
            valuesReviewsVector.add(valuesReviews);
        }

        Log.i(LOGTAG, "      num review records extracted from json array was: " + numReviews);

        if(valuesReviewsVector.size() > 0) { // no point in doing anything if no data could be obtained
            // TODO: can get rid of numDeleted after testing

            Log.i(LOGTAG, "      about to call bulkInsert");
            // insert the new data
            ContentValues[] valuesReviewsArray = new ContentValues[valuesReviewsVector.size()];
            valuesReviewsVector.toArray(valuesReviewsArray);

            numReviewsInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.ReviewsEntry.CONTENT_URI, valuesReviewsArray);
        }
        Log.d(LOGTAG, "        num reviews inserted to reviews table was: " + numReviewsInserted);



        // insert new records to the CREDITS table
        JSONObject creditsJsonObject = jsonBody.getJSONObject("credits");
        JSONArray creditsJsonArray = creditsJsonObject.getJSONArray("cast");

        int numCredits = creditsJsonArray.length();
        int numCreditsInserted = 0;
        Vector<ContentValues> valuesCreditsVector = new Vector<>(numCredits);

        for (int i = 0; i < numCredits; i++) {
            // get a single JSON object from jsonBody
            JSONObject jsonObject = creditsJsonArray.getJSONObject(i);
            ContentValues valuesCredits = new ContentValues();

            // put the movieId in to the ContentValues object
            valuesCredits.put(MovieTheaterContract.CreditsEntry.COLUMN_MOVIE_ID, mMovieId);

            // extract the data from the json object and put it in a single ContentValues object
            valuesCredits.put(MovieTheaterContract.CreditsEntry.COLUMN_CHARACTER, jsonObject.getString("character"));
            valuesCredits.put(MovieTheaterContract.CreditsEntry.COLUMN_NAME, jsonObject.getString("name"));
            valuesCredits.put(MovieTheaterContract.CreditsEntry.COLUMN_ORDER, jsonObject.getInt("order"));

            // create a fully formed URL for the actor/actress profile image path
            Uri.Builder profileImageUrlBuilder = new Uri.Builder();
            profileImageUrlBuilder.scheme(mContext.getString(R.string.themoviedb_scheme))
                    .authority(mContext.getString(R.string.themoviedb_image_authority))
                    .appendPath("t").appendPath("p")
                    .appendPath(mContext.getString(R.string.themoviedb_profile_size))
                    .build(); // https://image.tmdb.org/t/p/[image_size] to this point

            String profileImageUrl = profileImageUrlBuilder.build().toString()
                    + jsonObject.getString("profile_path");
            valuesCredits.put(MovieTheaterContract.CreditsEntry.COLUMN_PROFILE_PATH, profileImageUrl);

            // testing
//            Log.i(LOGTAG, "  added credit table record with movie_id: " + mMovieId);
//            Log.i(LOGTAG, "    and with character: " + jsonObject.getString("character"));
//            Log.i(LOGTAG, "    and with name: " + jsonObject.getString("name"));
//            Log.i(LOGTAG, "    and with profile path: " + profileImageUrl);
//            Log.i(LOGTAG, "    and with profile order: " + jsonObject.getInt("order"));

            // add the single object to the ContentValues Vector
            valuesCreditsVector.add(valuesCredits);
        }

        Log.i(LOGTAG, "      num credit records extracted from json array was: " + numCredits);

        if(valuesCreditsVector.size() > 0) { // no point in doing anything if no data could be obtained
            // TODO: can get rid of numDeleted after testing

            Log.i(LOGTAG, "      about to call bulkInsert");
            // insert the new data
            ContentValues[] valuesCreditsArray = new ContentValues[valuesCreditsVector.size()];
            valuesCreditsVector.toArray(valuesCreditsArray);

            numCreditsInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.CreditsEntry.CONTENT_URI, valuesCreditsArray);
        }
        Log.d(LOGTAG, "        num credits inserted to credits table was: " + numCreditsInserted);

    }
    
}
