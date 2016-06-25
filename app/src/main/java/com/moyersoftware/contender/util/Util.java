package com.moyersoftware.contender.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

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

    /**
     * Generates a new game ID with 8 random digits.
     */
    public static String generateGameId() {
        return String.valueOf(10000000 + new Random().nextInt(90000000));
    }

    /**
     * Formats a timestamp into a human-readable text.
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDate(long time) {
        SimpleDateFormat fmtOut = new SimpleDateFormat("dd MMM, h:mm a zzz");
        return fmtOut.format(time);
    }

    /**
     * Parses the user email and full name and formats it to username.
     */
    public static String parseUsername(FirebaseUser firebaseUser) {
        if (firebaseUser.getEmail() != null) {
            String email = firebaseUser.getEmail();
            return email.substring(0, email.indexOf("@"));
        } else if (firebaseUser.getDisplayName() != null) {
            return firebaseUser.getDisplayName().toLowerCase(Locale.getDefault());
        } else {
            return firebaseUser.getUid();
        }
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String formatNumber(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatNumber(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatNumber(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
