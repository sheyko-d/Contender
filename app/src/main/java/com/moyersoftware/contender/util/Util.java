package com.moyersoftware.contender.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.moyersoftware.contender.login.data.User;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

/**
 * Helper class.
 */
public class Util {

    private static final String LOG_TAG = "ContenderDebug";
    public static final String GET_CODES_URL = "http://www.moyersoftware.com/contender/" +
            "get_codes.php";
    public static final String SET_CODE_EXPIRED_URL = "http://www.moyersoftware.com/contender/" +
            "set_code_expired.php";
    public static final String SET_CODE_VALID_URL = "http://www.moyersoftware.com/contender/" +
            "set_code_valid.php";
    public static final String SUPPORT_URL = "http://www.moyersoftware.com";
    public static final String INVITE_LINK = "https://fb.me/1791931414374088";
    public static final String INVITE_IMAGE = "http://moyersoftware.com/contender/images" +
            "/invite.png";
    private static final String PREF_REFERRAL_CODE = "ReferralCode";
    private static final String PREF_REFERRAL_ASKED = "ReferralAsked";
    private static final String PREF_DISPLAY_NAME = "DisplayName";
    private static final String PREF_PHOTO = "Photo";
    private static final String PREF_EMPTY_CELL_REMINDER_TIMES = "EmptyCellReminderTimes";
    public static final int HALF_HOUR_DURATION = 1000 * 60 * 30;
    private static final String PREF_CURRENT_PLAYER_ID = "CurrentPlayerId";

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
     * Generates a new game code with 4 random digits.
     */
    public static String generateGameCode() {
        return String.valueOf(1000 + new Random().nextInt(9000));
    }

    /**
     * Formats a timestamp into a human-readable text.
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(long time) {
        SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d @ h:mma");
        return fmtOut.format(time).replace("AM", "am").replace("PM", "pm");
    }

    /**
     * Formats a timestamp into a human-readable text.
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDate(long time) {
        SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d");
        fmtOut.setTimeZone(TimeZone.getTimeZone("EST"));
        return fmtOut.format(time);
    }

    /**
     * Parses the user email and full name and formats it to username.
     */
    public static String parseUsername(FirebaseUser firebaseUser) {
        if (firebaseUser.getEmail() != null) {
            String email = firebaseUser.getEmail();
            return email.substring(0, email.indexOf("@"));
        } else if (Util.getDisplayName() != null) {
            return Util.getDisplayName().toLowerCase(Locale.getDefault());
        } else {
            return firebaseUser.getUid();
        }
    }

    /**
     * Parses the user email and full name and formats it to username.
     */
    public static String parseUsername(User friend) {
        if (friend.getEmail() != null) {
            String email = friend.getEmail();
            return email.substring(0, email.indexOf("@"));
        } else if (friend.getName() != null) {
            return friend.getName().toLowerCase(Locale.getDefault());
        } else {
            return friend.getId();
        }
    }

    /**
     * Parses the user email and formats it to username.
     */
    public static String parseUsername(String email) {
        try {
            return email.substring(0, email.indexOf("@"));
        } catch (Exception e) {
            return "";
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

    public static void setReferralCode(String code) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                .putString(PREF_REFERRAL_CODE, code).apply();
    }

    public static String getReferralCode() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .getString(PREF_REFERRAL_CODE, null);
    }

    public static void setReferralAsked() {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                .putBoolean(PREF_REFERRAL_ASKED, true).apply();
    }

    public static Boolean isReferralAsked() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .getBoolean(PREF_REFERRAL_ASKED, false);
    }

    public static String formatTime(Long time) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("EST"));
        return sdf.format(new Date(time)).replace(":00", "");
    }

    public static void setDisplayName(String name) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                .putString(PREF_DISPLAY_NAME, name).apply();
    }

    public static String getDisplayName() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .getString(PREF_DISPLAY_NAME, null);
    }

    public static void setPhoto(String photo) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                .putString(PREF_PHOTO, photo).apply();
    }

    public static String getPhoto() {
        String photo = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .getString(PREF_PHOTO, null);
        if (TextUtils.isEmpty(photo)) {
            photo = "null";
        }
        return photo;
    }

    public static void setEmptyCellReminderTimes(JSONArray emptyCellReminderTimes) {
        try {
            PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                    .putString(PREF_EMPTY_CELL_REMINDER_TIMES, emptyCellReminderTimes.toString())
                    .apply();
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                    .remove(PREF_EMPTY_CELL_REMINDER_TIMES).apply();
        }
    }

    public static JSONArray getEmptyCellReminderTimes() {
        try {
            return new JSONArray(PreferenceManager.getDefaultSharedPreferences
                    (MyApplication.getContext())
                    .getString(PREF_EMPTY_CELL_REMINDER_TIMES, null));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static String generatePlayerId() {
        return String.valueOf(1000000000 + new Random().nextInt(900000000));
    }

    public static void setCurrentPlayerId(String id) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit()
                .putString(PREF_CURRENT_PLAYER_ID, id)
                .apply();
    }

    public static String getCurrentPlayerId() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .getString(PREF_CURRENT_PLAYER_ID, null);
    }
}
