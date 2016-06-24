package com.moyersoftware.contender.menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.menu.adapter.FriendsAdapter;
import com.moyersoftware.contender.menu.data.Friend;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {

    // Views
    @Bind(R.id.friends_recycler)
    RecyclerView mFriendsRecycler;

    // Usual variables
    private ArrayList<Friend> mFriends = new ArrayList<>();

    public FriendsFragment() {
        // Required empty public constructor
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);

        initRecycler();

        return view;
    }

    private void initRecycler() {
        // TODO: Remove
        mFriends.clear();
        mFriends.add(new Friend("Jeff Spadaccini", "@jspadacc",
                "http://womensenews.org/files/NFL-football.jpg"));

        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setAdapter(new FriendsAdapter(getContext(), mFriends));
    }
}
