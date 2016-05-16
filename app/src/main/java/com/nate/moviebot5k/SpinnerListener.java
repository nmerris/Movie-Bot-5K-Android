package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * Created by Nathan Merris on 5/13/2016.
 */
public class SpinnerListener implements AdapterView.OnItemSelectedListener {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "SpinnerListener";

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    public SpinnerListener(Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(LOGTAG, "entered onItemSelected");
        SharedPreferences.Editor editor = mSharedPrefs.edit();

//        switch (parent.getId()) {
//            case R.id.spinner_year:
//                String currentYearFilter = mSharedPrefs.getString(mContext.getString(R.string.key_movie_filter_year), "");
//                String selectedYearFilter = parent.getItemAtPosition(position).toString();
//
//                // check to make sure user did not just select the same item that was already selected
//                if(!currentYearFilter.equals(selectedYearFilter)) {
//                    // update the year filter
//                    editor.putString(mContext.getString(R.string.key_movie_filter_year), selectedYearFilter);
//                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
//                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
//                    editor.putBoolean(mContext.getString(R.string.key_fetch_new_movies), true);
//                    // store the position in the spinner so that it's the same next time user comes back here
//                    editor.putInt(mContext.getString(R.string.key_movie_filter_year_spinner_position), position);
//
//                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_year: " + selectedYearFilter);
//                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
//                }
//                break;
//
//            case R.id.spinner_sortby:
//                int savedSortbySpinnerPosition = mSharedPrefs
//                        .getInt(mContext.getString(R.string.key_movie_filter_sortby_spinner_position), 0);
//
//                // check to see if the user actually changed the sortby selection
//                // this is why the order of the resource array matters
//                if(savedSortbySpinnerPosition != position) {
//                    String[] sortbyValues = mContext.getResources().getStringArray(R.array.movie_filter_sortby_values);
//
//                    // update the sortby filter value, which is not the same as the label
//                    // the value is what is used in MoviesFetcher for the API call
//                    // the label is what the user sees in the spinner
//                    editor.putString(mContext.getString(R.string.key_movie_filter_sortby_value), sortbyValues[position]);
//
//                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
//                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
//                    editor.putBoolean(mContext.getString(R.string.key_fetch_new_movies), true);
//                    // store the position in the spinner so that it's the same next time user comes back here
//                    editor.putInt(mContext.getString(R.string.key_movie_filter_sortby_spinner_position), position);
//
//                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_sortby_value: " + sortbyValues[position]);
//                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
//
//                }
//                break;
//
//            case R.id.spinner_genre:
//                String savedGenreId = mSharedPrefs
//                        .getString(mContext.getString(R.string.key_movie_filter_genre_id), "");
//
//                // get the selected genre filter **ID** from the tag of the view
//                String selectedGenreId = (String) view.getTag();
//
//                // check if user actually changed the genre filter
//                if(!savedGenreId.equals(selectedGenreId)) {
//                    // update the genreId saved in sharedPrefs
//                    editor.putString(mContext.getString(R.string.key_movie_filter_genre_id), selectedGenreId);
//                    // update fetch_new_movies so a new fetch task starts when appropriate
//                    editor.putBoolean(mContext.getString(R.string.key_fetch_new_movies), true);
//                    // save the new position of the spinner
//                    editor.putInt(mContext.getString(R.string.key_movie_filter_genre_spinner_position), position);
//
//                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_genre_id: " + selectedGenreId);
//                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
//
//                }
//                break;
//
//            case R.id.spinner_cert:
//                String savedCert = mSharedPrefs
//                        .getString(mContext.getString(R.string.key_movie_filter_cert), "");
//
//                // get the selected cert (only need name here, no ID needed for certs api queries)
//                TextView tv = (TextView) view;
//                String selectedCert = tv.getText().toString();
//
//                // check if user actually changed the cert filter
//                if(!savedCert.equals(selectedCert)) {
//                    // update the certId saved in sharedPrefs
//                    editor.putString(mContext.getString(R.string.key_movie_filter_cert), selectedCert);
//                    // update fetch_new_movies so a new fetch task starts when appropriate
//                    editor.putBoolean(mContext.getString(R.string.key_fetch_new_movies), true);
//                    // save the new position of the spinner
//                    editor.putInt(mContext.getString(R.string.key_movie_filter_cert_spinner_position), position);
//
//                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_cert_id: " + selectedCert);
//                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
//
//                }
//
//        }
//        editor.commit();
    }




    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally blank
    }

}
