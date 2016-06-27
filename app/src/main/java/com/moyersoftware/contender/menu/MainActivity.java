package com.moyersoftware.contender.menu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
import com.moyersoftware.contender.login.LoadingActivity;
import com.moyersoftware.contender.menu.adapter.MainPagerAdapter;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    // Views
    @Bind(R.id.main_tab_layout)
    TabLayout mTabLayout;
    @Bind(R.id.main_pager)
    ViewPager mPager;

    // Usual variables
    private int[] mTabIcons = new int[]{
            R.drawable.tab_home,
            R.drawable.tab_friends,
            R.drawable.tab_settings
    };
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoadingActivity.class));
            finish();
            return;
        }

        if (getIntent().getExtras() != null && getIntent().getData() != null) {
            String data = getIntent().getDataString();
            if (data.contains("moyersoftware.com/contender#")) {
                String gameId = data.substring(data.indexOf("#") + 1, data.length());
                playGame(gameId);
            }
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initPager();
        initTabs();
    }

    public void playGame(final String gameId) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Retrieve current list of players for the game user wants to join
        database.child("games").child(gameId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get game value
                        Game game = dataSnapshot.getValue(Game.class);

                        if (firebaseUser != null) {
                            final String id = firebaseUser.getUid();
                            ArrayList<String> players = game.getPlayers();
                            if (players == null) players = new ArrayList<>();

                            if (!players.contains(id)) players.add(id);

                            database.child("games").child(gameId).child("players")
                                    .setValue(players);

                            startActivity(new Intent(MainActivity.this, GameBoardActivity.class)
                                    .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void initPager() {
        mPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
    }

    private void initTabs() {
        mTabLayout.setupWithViewPager(mPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            //noinspection ConstantConditions
            mTabLayout.getTabAt(i).setIcon(mTabIcons[i]);
        }
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onLogOutButtonClicked(View view) {
        mFirebaseAuth.signOut();
        startActivity(new Intent(this, LoadingActivity.class));
        finish();
    }

    public void onSupportButtonClicked(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Util.SUPPORT_URL)));
    }

    public void onRateButtonClicked(View view) {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                    + appPackageName)));
        } catch (android.content.ActivityNotFoundException exception) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                    ("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void onAboutButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0)
                    .versionName;
            dialogBuilder.setTitle(getString(R.string.app_name) + " v" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            dialogBuilder.setTitle(getString(R.string.app_name));
        }
        dialogBuilder.setView(R.layout.dialog_about);
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.create().show();
    }
}
