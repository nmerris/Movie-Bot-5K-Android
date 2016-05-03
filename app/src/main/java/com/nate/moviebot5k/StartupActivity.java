package com.nate.moviebot5k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Initializes this app the first time it is installed on device, and detects if internet is
 * available.  If internet is available, launches Intent to HomeActivity, if not it will check to
 * see if user has at least one favorite, and then asks user via an AlertDialog if they would like
 * to view their favorites.  If so, launches and Intent to FavoritesActivity.
 */
public class StartupActivity extends AppCompatActivity {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "StartupActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "just in onCreate");


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);






//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        finish();

    }


    // initialize all sharedPrefs, need this to happen the first time app is installed
    // or if user clears the app data, they will either ALL exist, or NONE will exist
    private void initializeSharedPrefs() {

    }


}
