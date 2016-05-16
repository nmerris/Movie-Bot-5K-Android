package com.nate.moviebot5k;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nate.moviebot5k.adapters.CertSpinnerAdapter;
import com.nate.moviebot5k.adapters.GenreSpinnerAdapter;
import com.nate.moviebot5k.adapters.SortbySpinnerAdapter;
import com.nate.moviebot5k.adapters.YearSpinnerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nathan Merris on 5/11/2016.
 */
public class MovieFiltersSpinnerFragment extends Fragment
        implements AdapterView.OnItemSelectedListener {
    private final String LOGTAG = SingleFragmentActivity.N8LOG + "MovFiltsSpinFrg";

    @Bind(R.id.spinner_year) Spinner mYearSpinner;
    @Bind(R.id.spinner_sortby) Spinner mSortbySpinner;
    @Bind(R.id.spinner_genre) Spinner mGenreSpinner;
    @Bind(R.id.spinner_cert) Spinner mCertSpinner;

    private SimpleCursorAdapter mGenreSpinnerAdapter, mCertSpinnerAdapter;
    private SharedPreferences mSharedPrefs;
    private Callbacks mCallbacks;

    /**
     * Required interface for any activity that hosts this fragment
     */
    public interface Callbacks {

        /**
         *  Hosting Activity should determine what to do when a movie filter is changed.
         *  This will only be called if a filter actually does change values, as in, if user
         *  selects the same value as was already selected, this callback will not be made.
         */
        void onFilterChanged();
    }


    @Override
    public void onAttach(Context context) {
        // associate the fragment's mCallbacks object with the activity it was just attached to
        mCallbacks = (Callbacks) getActivity();
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mCallbacks = null; // need to make sure this member variable is up to date with the correct activity
        // so nullify it every time this fragment gets detached from it's hosting activity
        super.onDetach();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // this is where Loaders are recommended to be initialized
        new GenreAndCertSpinnerLoader(getActivity(), mGenreSpinnerAdapter, mCertSpinnerAdapter,
                mGenreSpinner, mCertSpinner, getLoaderManager());

        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "entered onCreateView");

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        View rootView = inflater.inflate(R.layout.fragment_filter_spinners_ref, container, false);
        ButterKnife.bind(this, rootView);

        ArrayAdapter<String> yrSpinnerAdapter = new YearSpinnerAdapter(getActivity()); // get all the selectable years
        yrSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yrSpinnerAdapter);
        // make sure the spinner starts at the same position as last time
        mYearSpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_year_spinner_position), 0));
        mYearSpinner.setOnItemSelectedListener(new SpinnerListener(getActivity()));


        ArrayAdapter<String> sortbySpinnerAdapter = new SortbySpinnerAdapter(getActivity());
        sortbySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortbySpinner.setAdapter(sortbySpinnerAdapter);
        mSortbySpinner.setSelection(mSharedPrefs.
                getInt(getString(R.string.key_movie_filter_sortby_spinner_position), 0));
        mSortbySpinner.setOnItemSelectedListener(new SpinnerListener(getActivity()));


        // set an adapter on the genre spinner
        mGenreSpinnerAdapter = new GenreSpinnerAdapter(getActivity());
        mGenreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenreSpinner.setAdapter(mGenreSpinnerAdapter);


        // set an adapter on the cert spinner
        mCertSpinnerAdapter = new CertSpinnerAdapter(getActivity());
        mCertSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCertSpinner.setAdapter(mCertSpinnerAdapter);

        return rootView;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(LOGTAG, "entered onItemSelected");
        SharedPreferences.Editor editor = mSharedPrefs.edit();

        switch (parent.getId()) {
            case R.id.spinner_year:
                String currentYearFilter = mSharedPrefs.getString(getActivity().getString(R.string.key_movie_filter_year), "");
                String selectedYearFilter = parent.getItemAtPosition(position).toString();

                // check to make sure user did not just select the same item that was already selected
                if(!currentYearFilter.equals(selectedYearFilter)) {
                    // update the year filter
                    editor.putString(getActivity().getString(R.string.key_movie_filter_year), selectedYearFilter);
                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
                    editor.putBoolean(getActivity().getString(R.string.key_fetch_new_movies), true);
                    // store the position in the spinner so that it's the same next time user comes back here
                    editor.putInt(getActivity().getString(R.string.key_movie_filter_year_spinner_position), position);

                    // notify hosting fragment that a movie filter has changed so the grid can reload
                    mCallbacks.onFilterChanged();

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_year: " + selectedYearFilter);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");
                }
                break;

            case R.id.spinner_sortby:
                int savedSortbySpinnerPosition = mSharedPrefs
                        .getInt(getActivity().getString(R.string.key_movie_filter_sortby_spinner_position), 0);

                // check to see if the user actually changed the sortby selection
                // this is why the order of the resource array matters
                if(savedSortbySpinnerPosition != position) {
                    String[] sortbyValues = getActivity().getResources().getStringArray(R.array.movie_filter_sortby_values);

                    // update the sortby filter value, which is not the same as the label
                    // the value is what is used in MoviesFetcher for the API call
                    // the label is what the user sees in the spinner
                    editor.putString(getActivity().getString(R.string.key_movie_filter_sortby_value), sortbyValues[position]);

                    // set fetch movies key to true so that MoviesFetcher is called when user goes back to
                    // HomeActivity which hosts MovieGridFragment, which launches the fetch task
                    editor.putBoolean(getActivity().getString(R.string.key_fetch_new_movies), true);
                    // store the position in the spinner so that it's the same next time user comes back here
                    editor.putInt(getActivity().getString(R.string.key_movie_filter_sortby_spinner_position), position);

                    // notify hosting fragment that a movie filter has changed so the grid can reload
                    mCallbacks.onFilterChanged();

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_sortby_value: " + sortbyValues[position]);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");

                }
                break;

            case R.id.spinner_genre:
                String savedGenreId = mSharedPrefs
                        .getString(getActivity().getString(R.string.key_movie_filter_genre_id), "");

                // get the selected genre filter **ID** from the tag of the view
                String selectedGenreId = (String) view.getTag();

                // check if user actually changed the genre filter
                if(!savedGenreId.equals(selectedGenreId)) {
                    // update the genreId saved in sharedPrefs
                    editor.putString(getActivity().getString(R.string.key_movie_filter_genre_id), selectedGenreId);
                    // update fetch_new_movies so a new fetch task starts when appropriate
                    editor.putBoolean(getActivity().getString(R.string.key_fetch_new_movies), true);
                    // save the new position of the spinner
                    editor.putInt(getActivity().getString(R.string.key_movie_filter_genre_spinner_position), position);

                    // notify hosting fragment that a movie filter has changed so the grid can reload
                    mCallbacks.onFilterChanged();

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_genre_id: " + selectedGenreId);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");

                }
                break;

            case R.id.spinner_cert:
                String savedCert = mSharedPrefs
                        .getString(getActivity().getString(R.string.key_movie_filter_cert), "");

                // get the selected cert (only need name here, no ID needed for certs api queries)
                TextView tv = (TextView) view;
                String selectedCert = tv.getText().toString();

                // check if user actually changed the cert filter
                if(!savedCert.equals(selectedCert)) {
                    // update the certId saved in sharedPrefs
                    editor.putString(getActivity().getString(R.string.key_movie_filter_cert), selectedCert);
                    // update fetch_new_movies so a new fetch task starts when appropriate
                    editor.putBoolean(getActivity().getString(R.string.key_fetch_new_movies), true);
                    // save the new position of the spinner
                    editor.putInt(getActivity().getString(R.string.key_movie_filter_cert_spinner_position), position);

                    // notify hosting fragment that a movie filter has changed so the grid can reload
                    mCallbacks.onFilterChanged();

                    Log.i(LOGTAG, "  just wrote to sharedPrefs key_movie_filter_cert_id: " + selectedCert);
                    Log.i(LOGTAG, "    and changed sharedPrefs key_fetch_new_movies bool to ****TRUE****");

                }

        }
        editor.commit();
    }
    

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally blank
    }

}
