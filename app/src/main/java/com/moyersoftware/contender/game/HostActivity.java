package com.moyersoftware.contender.game;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.moyersoftware.contender.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onBackButtonClicked(View view) {
        finish();
    }
}
