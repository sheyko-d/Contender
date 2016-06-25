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

public class JoinLocationFragment extends Fragment {

    // Views
    @Bind(R.id.join_id_recycler)
    RecyclerView mGamesRecycler;

    // Usual variables
    private ArrayList<Game> mGames = new ArrayList<>();

    public JoinLocationFragment() {
        // Required empty public constructor
    }

    public static JoinLocationFragment newInstance() {
        return new JoinLocationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_location, container, false);
        ButterKnife.bind(this, view);

        initRecycler();

        return view;
    }

    private void initRecycler() {
        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        mGamesRecycler.setAdapter(new JoinGamesAdapter(getContext(), mGames));
    }
}
