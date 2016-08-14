package com.moyersoftware.contender.game.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.data.Game;
import com.moyersoftware.contender.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.moyersoftware.contender.game.GameBoardActivity.EXTRA_GAME_ID;

public class EmptyCellCheckReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final JSONArray emptyCellReminderTimes = Util.getEmptyCellReminderTimes();
        for (int i = 0; i < emptyCellReminderTimes.length(); i++) {
            try {
                JSONObject game = new JSONObject(emptyCellReminderTimes.getString(i));
                final String id = game.getString("id");
                final String name = game.getString("name");
                final long time = game.getLong("time");

                FirebaseDatabase.getInstance().getReference().child("games").child(id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Game game = dataSnapshot.getValue(Game.class);
                                int emptySquaresCount = -1;
                                if (game != null) {
                                    if (game.getSelectedSquares() != null) {
                                        emptySquaresCount = 100 - game.getSelectedSquares().size();
                                    } else {
                                        emptySquaresCount = 100;
                                    }
                                }

                                if (emptySquaresCount == 0
                                        && game.getTime() > System.currentTimeMillis()) {
                                    showReminderNotification(context, id, name, time,
                                            emptySquaresCount);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            } catch (Exception e) {
                Util.Log("Can't parse game: " + e);
            }
        }
    }

    private void showReminderNotification(Context context, String id, String name, long time,
                                          int emptySquaresCount) {
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.warning)
                        .setContentTitle(name + " starts @ " + Util.formatTime(time))
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        if (emptySquaresCount == -1) {
            mBuilder.setContentText("Don't forget to fill all squares!");
        } else if (emptySquaresCount == 100) {
            mBuilder.setContentText("You didn't fill a single square yet!");
        } else {
            mBuilder.setContentText("Don't forget to fill " + emptySquaresCount + " more squares!");
        }
        Intent resultIntent = new Intent(context, GameBoardActivity.class)
                .putExtra(EXTRA_GAME_ID, id);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // Builds the notification and issues it.
        mNotifyMgr.notify(1, notification);
    }
}
