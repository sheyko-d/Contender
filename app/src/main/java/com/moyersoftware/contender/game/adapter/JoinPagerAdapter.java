package com.moyersoftware.contender.game.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.moyersoftware.contender.game.JoinFragment;

public class JoinPagerAdapter extends FragmentPagerAdapter {

    public JoinPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Returns total number of pages.
     */
    @Override
    public int getCount() {
        return 1;
    }

    /**
     * Returns the fragment to display for that page.
     */
    @Override
    public Fragment getItem(int position) {
        return JoinFragment.newInstance();
    }
}