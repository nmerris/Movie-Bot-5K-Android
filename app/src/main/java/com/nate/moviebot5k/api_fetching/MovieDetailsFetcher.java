package com.nate.moviebot5k.api_fetching;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
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
 * Fetches movie details from themoviedb servers.  The api calls made here are different than those
 * made by MoviesFetcher.  Reviews, Credits, Videos, budget, and revenue data are grabbed here for
 * whatever movieId is passed to the constructor.
 *
 * Created by Nathan Merris on 5/17/2016.
 */
public class MovieDetailsFetcher {
    private final String LOGTAG = ActivitySingleFragment.N8LOG + "MovDetlsFetchr";

    private Context mContext; // used to retrieve String resources for API queries
    private int mMovieId; // the movieId this fetcher be fetchin'
    private boolean updateVidsReviewsCredits;


    /**
     * Before creating a new MovieDetailsFetcher, make sure there are not already records in the db
     * with the same movieId, or you may end up with duplicates for videos, reviews, and credits.
     * One movieId may have, for example, 50 credits table records associated with it, so can't rely on
     * the UNIQUE constraint to prevent duplicates.  On the other hand, movies table does have a UNIQUE constraint.
     *
     * @param movieId themoviedb movieId
     * @param updateVidsReviewsCredits only pass in true if there are currently NO db records
     *                                 with movieId, or else you may get duplicate records
     */
    public MovieDetailsFetcher(Context context, int movieId, boolean updateVidsReviewsCredits) {
        mContext = context;
        mMovieId = movieId;
        this.updateVidsReviewsCredits = updateVidsReviewsCredits;
    }


    /**
     * Initiates a network api call to fetch all the details for the movie with id passed in to
     * MovieDetailsFetcher constructor.
     *
     * @return true if there were no network faults and the db was successfully updated
     */
    public boolean fetchMovieDetails() {
        Log.i(LOGTAG, "  just inside fetchMovieDetails and the movieId to be used to fetch movie details is: " + mMovieId);

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

            String jsonString = Utility.getUrlString(url);
//            Log.i(LOGTAG, "    Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString);

            // parse and insert the movie details to the appropriate db tables
            parseJsonAndInsertToDb(jsonBody);

        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to fetch items", ioe);
            return false;
        } catch (JSONException je) {
            Log.e(LOGTAG, "Failed to parse JSON", je);
            return false;
        }

        return true;
    }
    
    
    /**
     * Parses the movies data from json and updates the db.  If there is already a record in the
     * movies table with the same movieId, no records are updated or inserted.  Launches methods to
     * parse and update the videos, reviews, and credits tables depending on how this class was
     * constructed.
     * 
     * @see #MovieDetailsFetcher(Context, int, boolean) 
     */
    private void parseJsonAndInsertToDb(JSONObject jsonBody) throws JSONException {
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

        // update the movies table record
        mContext.getContentResolver()
                .update(MovieTheaterContract.MoviesEntry.buildMovieUriFromMovieId(mMovieId),
                       values, null, null);


        // only attempt to update these table if instructed to do so by caller
        // need to avoid duplicate records
        if(updateVidsReviewsCredits) {
            try {
                processVideosJson(jsonBody);
                processReviewsJson(jsonBody);
                processCreditsJson(jsonBody);
            } catch (JSONException je) {
                Log.e(LOGTAG, "Failed to parse JSON", je);
            }
        }

//        if(updateVidsReviewsCredits) {
//            try {
//                processVideosJson(jsonBody);
//            } catch (JSONException je) {
//                Log.e(LOGTAG, "Failed to parse JSON in processVideosJson", je);
//            }
//
//            try {
//                processReviewsJson(jsonBody);
//            } catch (JSONException je) {
//                Log.e(LOGTAG, "Failed to parse JSON in processReviewsJson", je);
//            }
//
//            try {
//                processCreditsJson(jsonBody);
//            } catch (JSONException je) {
//                Log.e(LOGTAG, "Failed to parse JSON in processCreditsJson", je);
//            }
//        }

    }
    
    
    /**
     * Parses the videos data from json, builds video thumbnail URLs, and updates the videos
     * table with all the new data.
     */
    private void processVideosJson (JSONObject jsonBody) throws JSONException {
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

            // build a video thumbnail URL, youtube only per design specs
            String youtubeVideoThumbnailUrl = "http://img.youtube.com/vi/" +
                    jsonObject.getString("key") + "/hqdefault.jpg";
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_THUMBNAIL_URL, youtubeVideoThumbnailUrl);

            // set the is_favorite column to FALSE
            valuesVideos.put(MovieTheaterContract.VideosEntry.COLUMN_IS_FAVORITE, "false");

            valuesVidsVector.add(valuesVideos);
        }

        if(valuesVidsVector.size() > 0) { // no point in doing anything if no data could be obtained
            // insert the new data
            ContentValues[] valuesVidsArray = new ContentValues[valuesVidsVector.size()];
            valuesVidsVector.toArray(valuesVidsArray);

            numVideosInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.VideosEntry.CONTENT_URI, valuesVidsArray);
        }
        Log.i(LOGTAG, "  num videos inserted to videos table was: " + numVideosInserted);
    }
    
    
    /**
     * Parses the reviews data from json, and updates the reviews
     * table with all the new data.
     */
    private void processReviewsJson (JSONObject jsonBody) throws JSONException {
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

//            Log.i(LOGTAG, " review " + i + " content: " + jsonObject.getString("content"));

            // set the is_favorite column to FALSE
            valuesReviews.put(MovieTheaterContract.MoviesEntry.COLUMN_IS_FAVORITE, "false");

            // add the single object to the ContentValues Vector
            valuesReviewsVector.add(valuesReviews);
        }
        
        if(valuesReviewsVector.size() > 0) { // no point in doing anything if no data could be obtained
            // insert the new data
            ContentValues[] valuesReviewsArray = new ContentValues[valuesReviewsVector.size()];
            valuesReviewsVector.toArray(valuesReviewsArray);

            numReviewsInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.ReviewsEntry.CONTENT_URI, valuesReviewsArray);
        }
        Log.i(LOGTAG, "  num reviews inserted to reviews table was: " + numReviewsInserted);
    }
    
    
    /**
     * Parses the credits data from json, builds credit thumbnail URLs, and updates the credits
     * table with all the new data.
     */
    private void processCreditsJson(JSONObject jsonBody) throws JSONException {
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

            // set the is_favorite column to FALSE
            valuesCredits.put(MovieTheaterContract.CreditsEntry.COLUMN_IS_FAVORITE, "false");

            // add the single object to the ContentValues Vector
            valuesCreditsVector.add(valuesCredits);
        }
        
        if(valuesCreditsVector.size() > 0) { // no point in doing anything if no data could be obtained
            // insert the new data
            ContentValues[] valuesCreditsArray = new ContentValues[valuesCreditsVector.size()];
            valuesCreditsVector.toArray(valuesCreditsArray);

            numCreditsInserted = mContext.getContentResolver()
                    .bulkInsert(MovieTheaterContract.CreditsEntry.CONTENT_URI, valuesCreditsArray);
        }
        Log.i(LOGTAG, "  num credits inserted to credits table was: " + numCreditsInserted);
    }
    
}
