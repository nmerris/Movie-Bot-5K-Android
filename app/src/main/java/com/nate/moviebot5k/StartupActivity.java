package com.nate.moviebot5k;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StartupActivity extends AppCompatActivity {
    private static final String LOGTAG = SingleFragmentActivity.N8LOG + "StartupActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(SingleFragmentActivity.N8LOG, "just in onCreate");



        // TODO: perform startup tasks here




//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        finish();

    }
}
