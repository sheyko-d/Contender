package com.moyersoftware.contender.game.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.JoinIdFragment;
import com.moyersoftware.contender.game.JoinLocationFragment;
import com.moyersoftware.contender.util.MyApplication;

public class JoinPagerAdapter extends FragmentPagerAdapter {

    private String[] mTitles = MyApplication.getContext().getResources()
            .getStringArray(R.array.join_tabs);

    public JoinPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Returns total number of pages.
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * Returns the fragment to display for that page.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return JoinIdFragment.newInstance();
            case 1:
                return JoinLocationFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}