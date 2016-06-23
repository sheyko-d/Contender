package com.moyersoftware.contender.games.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.moyersoftware.contender.games.MainFragment;
import com.moyersoftware.contender.games.SettingsFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Returns total number of pages.
     */
    @Override
    public int getCount() {
        return 3;
    }

    /**
     * Returns the fragment to display for that page.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MainFragment.newInstance();
            case 1:
                return MainFragment.newInstance();
            case 2:
                return SettingsFragment.newInstance();
            default:
                return null;
        }
    }
}