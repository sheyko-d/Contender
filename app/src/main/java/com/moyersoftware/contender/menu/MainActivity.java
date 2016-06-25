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
import com.moyersoftware.contender.login.LoadingActivity;
import com.moyersoftware.contender.menu.adapter.MainPagerAdapter;
import com.moyersoftware.contender.util.Util;

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
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoadingActivity.class));
            finish();
            return;
        } else {
            Util.Log("username = "+mFirebaseUser.getEmail()+", "+mFirebaseUser.getEmail().replace("/@.*/", ""));
        }

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
