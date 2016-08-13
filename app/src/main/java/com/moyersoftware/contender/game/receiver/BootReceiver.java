package com.moyersoftware.contender.game.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moyersoftware.contender.game.service.WinnerService;
import com.moyersoftware.contender.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        JSONArray emptyCellReminderTimes = Util.getEmptyCellReminderTimes();
        for (int i = 0; i < emptyCellReminderTimes.length(); i++) {
            try {
                setAlarm(context, new JSONObject(emptyCellReminderTimes.getString(i))
                        .getLong("time"));
            } catch (JSONException e) {
                Util.Log("Can't reset alarm: " + e);
            }
        }

        context.startService(new Intent(context, WinnerService.class));
    }

    private void setAlarm(Context context, long time) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EmptyCellCheckReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, time - Util.HALF_HOUR_DURATION, alarmIntent);
    }
}
