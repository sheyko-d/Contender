package com.moyersoftware.contender.util;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.moyersoftware.contender.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyApplication extends Application{
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        // Init Facebook SDK
        FacebookSdk.sdkInitialize(this);

        // Init calligraphy library
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/segoe-ui.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    public static Context getContext() {
        return sContext;
    }
}
