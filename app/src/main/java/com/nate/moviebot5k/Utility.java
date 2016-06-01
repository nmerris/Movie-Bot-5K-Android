package com.nate.moviebot5k;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nate.moviebot5k.data.MovieTheaterContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    
    /**
     * Updates the title and subtitle in the textviews contained in the details toolbar.
     * Only call this method if the app is running in tablet mode, because only table mode has
     * a details toolbar.
     *
     * @param activity The calling activity which should have a details toolbar
     * @param title The toolbar title to display, will center it if tagline is null
     * @param tagline The toolbar subtitle to display
     */
    public static void updateToolbarTitleAndTagline(AppCompatActivity activity, String title, String tagline) {
        // set the title and tagline in the action bar, depending on if the movie
        // in question actually has tagline data stored in the db.. seems about 80% have taglines
        TextView movieTitleTextView = (TextView) activity.findViewById(R.id.toolbar_movie_title);
        TextView movieTaglineTextView = (TextView) activity.findViewById(R.id.toolbar_movie_tagline);

        // it's nice to have the title centered when there is no tagline, so remove the tagline
        // view from the layout temporarily so the title can center itself
        // I am getting both null and empty strings in the db, so check for both
        if(movieTaglineTextView != null && movieTitleTextView != null) {
            // I am getting both null and empty strings in the db for taglines, so check for both,
            // but start with null check so short circuit OR will avoid possible null pointer exception
            // when .equals executes
            if (tagline == null || tagline.equals("")) {
                movieTaglineTextView.setVisibility(View.GONE);
                movieTitleTextView.setText(title);
            } else {
                movieTaglineTextView.setVisibility(View.VISIBLE);
                movieTitleTextView.setText(title);
                movieTaglineTextView.setText(tagline);
            }
        }
    }


    public static Uri buildYouTubeUri(String youTubeKey) {
        Uri.Builder youTubeUrl = new Uri.Builder();
        youTubeUrl.scheme("https")
                .authority("www.youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", youTubeKey);

        return youTubeUrl.build();
    }


    public static void displayScreenDP(Context context, String logtag) {
        // TESTING: just want to see what screen dp of device is..
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.i(logtag, "==== screen dpWidth is: " + dpWidth + ", and dpHeight is: " + dpHeight + " ====");
    }


    public static Calendar parseDate(String dateString) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(dateString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar;

//            String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
//            String year = String.valueOf(calendar.get(Calendar.YEAR));
//            String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
//
//            return month + " " + day + ", " + year;

        } catch (ParseException e) {
            Log.e("EXCEPTION", "  dateFormat.parse error: " + e);
            e.printStackTrace();
            return null;
        }





    }
    
}
