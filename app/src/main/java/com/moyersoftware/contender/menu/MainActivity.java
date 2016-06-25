package com.moyersoftware.contender.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.moyersoftware.contender.R;
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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initAuth();
        initPager();
        initTabs();
    }

    /**
     * Initializes Firebase Auth.
     */
    private void initAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
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
