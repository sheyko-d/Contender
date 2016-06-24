package com.moyersoftware.contender.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.JoinGamesAdapter;
import com.moyersoftware.contender.game.data.Game;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JoinIdFragment extends Fragment {

    // Views
    @Bind(R.id.join_id_recycler)
    RecyclerView mGamesRecycler;

    // Usual variables
    private ArrayList<Game> mGames = new ArrayList<>();

    public JoinIdFragment() {
        // Required empty public constructor
    }

    public static JoinIdFragment newInstance() {
        return new JoinIdFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_id, container, false);
        ButterKnife.bind(this, view);

        initRecycler();

        return view;
    }

    private void initRecycler() {
        // TODO: Remove
        mGames.clear();
        mGames.add(new Game("Big Day", System.currentTimeMillis(), "http://womensenews.org/files/NFL-football.jpg", "89/100", "sd1234"));
        mGames.add(new Game("NFL", System.currentTimeMillis(), "http://cache3.asset-cache.net/gc/461080164-detailed-view-of-an-nfl-game-ball-during-the-gettyimages.jpg?v=1&c=IWSAsset&k=2&d=GkZZ8bf5zL1ZiijUmxa7QSy23N5sserBo9ZkzRzjt5OUXhIP8J6xwGtqTHhFXuOJK6GssOSoFoAZB2jFBmovrQ%3D%3D", "100/100", "sd1234"));

        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        mGamesRecycler.setAdapter(new JoinGamesAdapter(getContext(), mGames));
    }
}
