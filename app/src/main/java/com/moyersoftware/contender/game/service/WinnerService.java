package com.moyersoftware.contender.game.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.MyApplication;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.moyersoftware.contender.game.GameBoardActivity.EXTRA_GAME_ID;

public class WinnerService extends Service {

    private static final int PAID_NOTIFICATION_CODE = 123;
    private HashMap<String, Long> mEventTimes = new HashMap<>();
    private ArrayList<Integer> mShownPaidNotifications = new ArrayList<>();

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
        Query query = database.child("events");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventTimes.clear();
                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    try {
                        final Event event = gameSnapshot.getValue(Event.class);
                        mEventTimes.put(event.getId(), event.getTime());
                    } catch (Exception e) {
                        // Can't retrieve game time
                    }
                }

                getGames(FirebaseAuth.getInstance().getCurrentUser(), database);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getGames(FirebaseAuth.getInstance().getCurrentUser(), database);
                        new Handler().postDelayed(this, 60 * 1000);
                    }
                }, 60 * 1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        database.child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parseGames(database, FirebaseAuth.getInstance().getCurrentUser(), dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getGames(final FirebaseUser firebaseUser, final DatabaseReference database) {
        database.child("games").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parseGames(database, firebaseUser, dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void parseGames(DatabaseReference database, FirebaseUser firebaseUser, DataSnapshot dataSnapshot) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String deviceOwnerId = user.getUid();
            String myId = Util.getCurrentPlayerId();
            if (myId == null) myId = deviceOwnerId;

            for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                try {
                    GameInvite.Game game = gameSnapshot.getValue(GameInvite.Game.class);
                    if (game != null && (game.getAuthor().getUserId().equals(firebaseUser
                            .getUid()) || (game.getPlayers() != null && game.getPlayers()
                            .contains(new Player(firebaseUser.getUid(), null,
                                    firebaseUser.getEmail(),
                                    Util.getDisplayName(), Util.getPhoto()))))) {
                        if (!TextUtils.isEmpty(game.getInviteName())) {
                            continue;
                        }
                        int emptySquaresCount;
                        if (game.getSelectedSquares() != null) {
                            emptySquaresCount = 100 - game.getSelectedSquares().size();
                        } else {
                            emptySquaresCount = 100;
                        }

                        if (emptySquaresCount == 100 && (mEventTimes.get(game.getEventId())
                                == -2 || mEventTimes.get(game.getEventId())
                                < System.currentTimeMillis())) {
                            continue;
                        }

                        if (emptySquaresCount > 0 && mEventTimes.get(game.getEventId()) != -2
                                && mEventTimes.get(game.getEventId()) -
                                System.currentTimeMillis() < 1000 * 60 * 30) {
                            if (!PreferenceManager.getDefaultSharedPreferences
                                    (MyApplication.getContext()).getBoolean
                                    (game.getId() + "reminder", false)) {
                                PreferenceManager.getDefaultSharedPreferences
                                        (MyApplication.getContext()).edit().putBoolean
                                        (game.getId() + "reminder", true).apply();
                                showReminderNotification(MyApplication.getContext(),
                                        game.getId(), game.getName(),
                                        mEventTimes.get(game.getEventId()) + 60 * 60 * 1000,
                                        emptySquaresCount);
                            }
                        }

                        String gameId = game.getId();

                        if (game.getQuarter1Winner() != null) {
                            if (!game.getQuarter1Winner().isConsumed() && game.getQuarter1Winner().getPlayer() != null
                                    && game.getQuarter1Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter1Price(), "1st", null, gameId);
                            } else if (!game.getQuarter1Winner().isConsumed() && deviceOwnerId.equals(game.getQuarter1Winner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getQuarter1Price(), "1st", game.getQuarter1Winner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("quarter1Winner").child("consumed")
                                    .setValue(true);
                        }
                        if (game.getQuarter2Winner() != null) {
                            Util.Log("show winner 1 " + game.getQuarter2Winner().getPlayer().getName() + ", " + game.getQuarter2Winner().getPlayer().getUserId() + " == " + myId);
                            if (!game.getQuarter2Winner().isConsumed() && game.getQuarter2Winner().getPlayer() != null
                                    && game.getQuarter2Winner().getPlayer().getUserId().equals(myId)) {
                                Util.Log("show winner 2");
                                showWinDialog(game.getQuarter2Price(), "2nd", null, gameId);
                            } else if (!game.getQuarter2Winner().isConsumed() && deviceOwnerId.equals(game.getQuarter2Winner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getQuarter2Price(), "2nd", game.getQuarter2Winner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("quarter2Winner").child("consumed")
                                    .setValue(true);
                        }

                        if (game.getQuarter3Winner() != null) {
                            if (!game.getQuarter3Winner().isConsumed() && game.getQuarter3Winner().getPlayer() != null
                                    && game.getQuarter3Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter3Price(), "3rd", null, gameId);
                            } else if (!game.getQuarter3Winner().isConsumed() && deviceOwnerId.equals(game.getQuarter3Winner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getQuarter3Price(), "3rd", game.getQuarter3Winner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("quarter3Winner").child("consumed")
                                    .setValue(true);
                        }

                        if (game.getFinalWinner() != null) {
                            if (!game.getFinalWinner().isConsumed() && game.getFinalWinner().getPlayer() != null
                                    && game.getFinalWinner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getFinalPrice(), "final", null, gameId);
                            } else if (!game.getFinalWinner().isConsumed() && deviceOwnerId.equals(game.getFinalWinner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getFinalPrice(), "final", game.getFinalWinner().getPlayer()
                                        .getName(), gameId);
                            }
                            database.child("games").child(gameId).child("finalWinner").child("consumed")
                                    .setValue(true);
                        }


                        // Warn if not all players paid
                        if (game.getAuthor().getUserId().equals(myId) && mEventTimes.get
                                (game.getEventId()) != -1 && mEventTimes.get(game.getEventId())
                                != -2 && System.currentTimeMillis() < mEventTimes.get
                                (game.getEventId()) + 60 * 60 * 1000) {
                            loadPlayers(database, gameId, game.getName(),
                                    mEventTimes.get(game.getEventId()) + 60 * 60 * 1000, myId);
                        }
                    }
                } catch (Exception e) {
                    Util.Log("Can't show winners: " + e);
                }
            }
        }
    }

    private void loadPlayers(final DatabaseReference database, final String gameId,
                             final String name, final long time, final String myId) {
        database.child("games").child(gameId).child("players").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> players = new ArrayList<>();
                        players.clear();
                        players.add(myId);
                        for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Player player = playerSnapshot.getValue(Player.class);
                                players.add(player.getUserId());
                                Util.Log("check existing player: " + player.getUserId());
                            } catch (Exception e) {
                                Util.Log("Can't parse player");
                            }
                        }

                        loadPaidPlayers(database, gameId, players, name, time);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void loadPaidPlayers(DatabaseReference database, final String gameId,
                                 final ArrayList<String> players, final String name,
                                 final long time) {
        database.child("games").child(gameId).child("paid_players").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> paidPlayers = new ArrayList<>();
                        paidPlayers.clear();
                        for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                            if (playerSnapshot.getValue(Boolean.class)) {
                                paidPlayers.add(playerSnapshot.getKey());
                                Util.Log("paid player: " + paidPlayers);
                            }
                        }

                        if (paidPlayers.size() != players.size()) {
                            showPaidNotification(MyApplication.getContext(), gameId, name, time);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void showPaidNotification(Context context, String id, String name, long time) {
        int minutesBefore;

        if (time - System.currentTimeMillis() < 5 * 60 * 1000) {
            if (!mShownPaidNotifications.contains(PAID_NOTIFICATION_CODE + Integer.parseInt(id)
                    + 5)) {
                minutesBefore = 5;
            } else {
                return;
            }
        } else if (time - System.currentTimeMillis() < 15 * 60 * 1000) {
            if (!mShownPaidNotifications.contains(PAID_NOTIFICATION_CODE + Integer.parseInt(id)
                    + 15)) {
                minutesBefore = 15;
            } else {
                return;
            }
        } else if (time - System.currentTimeMillis() < 30 * 60 * 1000) {
            if (!mShownPaidNotifications.contains(PAID_NOTIFICATION_CODE + Integer.parseInt(id)
                    + 30)) {
                minutesBefore = 30;
            } else {
                return;
            }
        } else {
            return;
        }

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.warning)
                        .setContentTitle(name + " game starts in less than " + minutesBefore + " minutes")
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mBuilder.setContentText("Not everybody paid for their squares yet!");
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
        mNotifyMgr.notify(PAID_NOTIFICATION_CODE + Integer.parseInt(id), notification);

        mShownPaidNotifications.add(PAID_NOTIFICATION_CODE + Integer.parseInt(id) + minutesBefore);
    }

    private void showReminderNotification(Context context, String id, String name, long time,
                                          int emptySquaresCount) {
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.warning)
                        .setContentTitle(name + " game starts @ " + Util.formatTime(time))
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        if (emptySquaresCount == 100) {
            mBuilder.setContentText("You didn't fill a single square yet!");
        } else {
            mBuilder.setContentText("Don't forget to fill " + emptySquaresCount + " more square(s)!");
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
        mNotifyMgr.notify(new Random().nextInt(), notification);
    }


    private void showWinDialog(final int price, final String quarter, final String name, final String gameId) {
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
        mNotifyMgr.notify((gameId + quarter).hashCode(), notification);
    }
}
