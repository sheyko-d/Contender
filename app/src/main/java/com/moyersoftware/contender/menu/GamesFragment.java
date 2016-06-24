package com.moyersoftware.contender.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.HostActivity;
import com.moyersoftware.contender.menu.adapter.GamesAdapter;
import com.moyersoftware.contender.game.data.Game;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GamesFragment extends Fragment {

    // Views
    @Bind(R.id.games_recycler)
    RecyclerView mGamesRecycler;
    @Bind(R.id.games_host_btn)
    Button mHostBtn;
    @Bind(R.id.games_join_btn)
    Button mJoinBtn;

    // Usual variables
    private ArrayList<Game> mGames = new ArrayList<>();

    public GamesFragment() {
        // Required empty public constructor
    }

    public static GamesFragment newInstance() {
        return new GamesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        ButterKnife.bind(this, view);

        initRecycler();
        initButtons();

        return view;
    }

    private void initRecycler() {
        // TODO: Remove
        mGames.clear();
        mGames.add(new Game("Big Day", System.currentTimeMillis(), "http://womensenews.org/files/NFL-football.jpg", "89/100"));
        mGames.add(new Game("NFL", System.currentTimeMillis(), "http://cache3.asset-cache.net/gc/461080164-detailed-view-of-an-nfl-game-ball-during-the-gettyimages.jpg?v=1&c=IWSAsset&k=2&d=GkZZ8bf5zL1ZiijUmxa7QSy23N5sserBo9ZkzRzjt5OUXhIP8J6xwGtqTHhFXuOJK6GssOSoFoAZB2jFBmovrQ%3D%3D", "100/100"));

        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        mGamesRecycler.setAdapter(new GamesAdapter(getContext(), mGames));
    }

    private void initButtons() {
        mHostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HostActivity.class));
            }
        });
        mJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Change HostActivity to JoinActivity
                startActivity(new Intent(getActivity(), HostActivity.class));
            }
        });
    }
}
