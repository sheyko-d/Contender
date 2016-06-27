package com.moyersoftware.contender.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.login.LoadingActivity;
import com.moyersoftware.contender.menu.adapter.MainPagerAdapter;

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
            if (data.contains("http://moyersoftware.com/contender#")) {
                String gameId = data.substring(data.indexOf("#") + 1, data.length());
                // TODO: Add to players list
                startActivity(new Intent(this, GameBoardActivity.class)
                        .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId));
            }
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initPager();
        initTabs();
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
}
