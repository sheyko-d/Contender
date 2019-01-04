package com.moyersoftware.contender.util;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.facebook.FacebookSdk;
import com.google.firebase.database.FirebaseDatabase;
import com.moyersoftware.contender.R;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyApplication extends Application{
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Answers());
        sContext = this;

        // Init Facebook SDK
        FacebookSdk.sdkInitialize(this);

        // Init Firebase SDK
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        // Init calligraphy library
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/segoe_ui.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    public static Context getContext() {
        return sContext;
    }
}
