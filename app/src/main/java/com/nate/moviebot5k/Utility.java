package com.nate.moviebot5k;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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




    public static String[] getMovieFilterYears(Context context) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int minYear =  context.getResources().getInteger(R.integer.min_year_movie_filter);
        int numYears = Integer.valueOf(currentYear) - minYear;

        String[] years = new String[numYears];
        for(int i = 0; i < numYears; i++) {
            years[i] = String.valueOf(minYear + i);
        }

        return years;
    }

}
