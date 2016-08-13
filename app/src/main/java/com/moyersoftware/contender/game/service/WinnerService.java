package com.moyersoftware.contender.game.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.data.Game;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.MyApplication;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

public class WinnerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        addGamesListener();
        Util.Log("add games listener");
    }

    private void addGamesListener() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //try {
                Util.Log("games data changed");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String deviceOwnerId = user.getUid();
                    String myId = Util.getCurrentPlayerId();
                    if (myId == null) myId = deviceOwnerId;

                    for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                        Game game = gameSnapshot.getValue(Game.class);
                        if (game == null) continue;

                        // Check if I'm playing in this game
                        if (!game.getAuthor().getUserId().equals(myId) && !isPlayer(myId,
                                game.getPlayers())) {
                            continue;
                        }

                        String gameId = game.getId();

                        if (game.getQuarter1Winner() != null) {
                            if (!game.getQuarter1Winner().isConsumed()
                                    && game.getQuarter1Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter1Price(), "1st", null, gameId);
                            } else if (!game.getQuarter1Winner().isConsumed() && game.getQuarter1Winner()
                                    .getPlayer().getCreatedByUserId().equals(deviceOwnerId)) {
                                showWinDialog(game.getQuarter1Price(), "1st", game.getQuarter1Winner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("quarter1Winner").child("consumed")
                                    .setValue(true);
                        }

                        if (game.getQuarter2Winner() != null) {
                            if (!game.getQuarter2Winner().isConsumed()
                                    && game.getQuarter2Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter2Price(), "2nd", null, gameId);
                            } else if (!game.getQuarter2Winner().isConsumed() && game.getQuarter2Winner()
                                    .getPlayer().getCreatedByUserId().equals(deviceOwnerId)) {
                                showWinDialog(game.getQuarter2Price(), "2nd", game.getQuarter2Winner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("quarter2Winner").child("consumed")
                                    .setValue(true);
                        }

                        if (game.getQuarter3Winner() != null) {
                            if (!game.getQuarter3Winner().isConsumed()
                                    && game.getQuarter3Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter3Price(), "3rd", null, gameId);
                            } else if (!game.getQuarter3Winner().isConsumed() && game.getQuarter3Winner()
                                    .getPlayer().getCreatedByUserId().equals(deviceOwnerId)) {
                                showWinDialog(game.getQuarter3Price(), "3rd", game.getQuarter3Winner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("quarter3Winner").child("consumed")
                                    .setValue(true);
                        }

                        if (game.getFinalWinner() != null) {
                            if (!game.getFinalWinner().isConsumed()
                                    && game.getFinalWinner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getFinalPrice(), "final", null, gameId);
                            } else if (!game.getFinalWinner().isConsumed() && game.getFinalWinner()
                                    .getPlayer().getCreatedByUserId().equals(deviceOwnerId)) {
                                showWinDialog(game.getFinalPrice(), "final", game.getFinalWinner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("finalWinner").child("consumed")
                                    .setValue(true);
                        }
                    }
                }
                try {
                } catch (Exception e) {
                    Util.Log("Can't update games: " + e);
                }
            }

            private boolean isPlayer(String myId, ArrayList<Player> players) {
                for (Player player : players) {
                    if (player.getUserId().equals(myId)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void showWinDialog(final int price, final String quarter, final String name, final String gameId) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameBoardActivity.this,
                            R.style.MaterialDialog);
                    dialogBuilder.setTitle("\uD83C\uDFC6  Congratulations!");
                    if (TextUtils.isEmpty(name)) {
                        dialogBuilder.setMessage("You won " + price + " points in the " + quarter
                                + " quarter");
                    } else {
                        dialogBuilder.setMessage(name + " won " + price + " points in the " + quarter
                                + " quarter");
                    }
                    dialogBuilder.setNegativeButton("OK", null);
                    dialogBuilder.create().show();
                } catch (Exception e) {*/
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(MyApplication.getContext())
                        .setSmallIcon(R.drawable.notif)
                        .setContentTitle("Congratulations!")
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(MyApplication.getContext(), R.color.colorPrimary));
        if (TextUtils.isEmpty(name)) {
            mBuilder.setContentText("You won " + price + " points in the " + quarter
                    + " quarter");
        } else {
            mBuilder.setContentText(name + " won " + price + " points in the " + quarter
                    + " quarter");
        }
        Intent resultIntent = new Intent(MyApplication.getContext(), GameBoardActivity.class)
                .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        MyApplication.getContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);// Sets an ID for the notification
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // Builds the notification and issues it.
        mNotifyMgr.notify(gameId.hashCode() + quarter.hashCode(), notification);
               /* }
            }
        });*/
    }
}
