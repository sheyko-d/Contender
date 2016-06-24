package com.moyersoftware.contender.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Helper class.
 */
public class Util {

    private static final String LOG_TAG = "ContenderDebug";

    /**
     * Adds a message to LogCat.
     */
    public static void Log(Object text) {
        Log.d(LOG_TAG, text + "");
    }

    /**
     * Converts from DP (density-independent pixels) to regular pixels.
     */
    public static int convertDpToPixel(float dp) {
        Resources resources = MyApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }
}
