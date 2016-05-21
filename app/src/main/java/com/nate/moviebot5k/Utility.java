package com.nate.moviebot5k;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.nate.moviebot5k.data.MovieTheaterContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Nathan Merris on 5/6/2016.
 */
public class Utility {


    // boilerplate networking code taken from Big Nerd Ranch Android Programming, 2nd ed
    // use getUrlBytes when downloading pics or other non-string data
    public static byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }


    // returns the URL fetch as a string, use when parsing json with async tasks
    public static String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }



    // returns an array of Strings to fill the year filter spinner, starting at the min year
    // as defined in values/ints resource file and ending at the current year
    // the zeroth element is filled with the default year, which is 'Any Year'
    // NOTE: the spinner adapter lists the zeroth element first
    public static String[] getMovieFilterYears(Context context) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int currentYearInt = Integer.valueOf(currentYear);
        int minYear =  context.getResources().getInteger(R.integer.min_year_movie_filter) - 1;
        int numYears = Integer.valueOf(currentYear) - minYear;

        String[] years = new String[numYears + 1];
        for(int i = 0; i < numYears; i++) {
            years[i + 1] = String.valueOf(currentYearInt - i);
        }
        years[0] = context.getString(R.string.default_movie_filter_year);

        return years;
    }
    
    
    public static ArrayList<Integer> getMovieIdList(Context context) {

        ArrayList<Integer> movieIds = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                MovieTheaterContract.MoviesEntry.CONTENT_URI,
                new String[]{MovieTheaterContract.MoviesEntry._ID, MovieTheaterContract.MoviesEntry.COLUMN_MOVIE_ID},
                null, null,
                "_id ASC");

        if(cursor != null && cursor.moveToFirst()) {

            do {
//                Log.i("UTILITY", "******** cursor _id is: " + cursor.getInt(0));
//                Log.i("UTILITY", "********    and added movieId to list: " + cursor.getInt(1));

                movieIds.add(cursor.getInt(1));
                cursor.moveToNext();

            } while(!cursor.isAfterLast());

//            Log.i("UTILITY", "    and numMovies is: " + movieIds.size());

            cursor.close();
        }
        return movieIds;
    }


    public static Uri buildYouTubeUri(String youTubeKey) {
        Uri.Builder youTubeUrl = new Uri.Builder();
        youTubeUrl.scheme("https")
                .authority("www.youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", youTubeKey);

        return youTubeUrl.build();
    }

    
}
