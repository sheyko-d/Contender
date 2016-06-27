package com.moyersoftware.contender.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

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

    /**
     * Generates numbers for the game board.
     */
    public static ArrayList<Integer> generateBoardNumbers() {
        Random rng = new Random(); // Ideally just create one instance globally
        // Note: use LinkedHashSet to maintain insertion order
        Set<Integer> generated = new LinkedHashSet<>();
        while (generated.size() < 10) {
            Integer next = rng.nextInt(10);
            // As we're adding to a set, this will automatically do a containment check
            generated.add(next);
        }
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int number : generated) {
            numbers.add(number);
        }
        return numbers;
    }
}
