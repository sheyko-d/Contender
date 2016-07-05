package com.moyersoftware.contender.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.JoinGamesAdapter;
import com.moyersoftware.contender.game.data.Game;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JoinIdFragment extends Fragment {

    // Views
    @Bind(R.id.join_id_recycler)
    RecyclerView mGamesRecycler;
    @Bind(R.id.join_id_edit_txt)
    EditText mIdEditTxt;
    @Bind(R.id.join_id_title_txt)
    TextView mTitleTxt;

    // Usual variables
    private ArrayList<Game> mGames = new ArrayList<>();
    private JoinGamesAdapter mAdapter;
    private String mQuery;
    private DataSnapshot mDataSnapshot;
    private String mMyId;
    private String mMyEmail;
    private String mMyName;
    private String mMyPhoto;

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

        initUser();
        initSearchField();
        initRecycler();
        initDatabase();

        return view;
    }

    private void initUser() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mMyId = firebaseUser.getUid();
            mMyEmail = firebaseUser.getEmail();
            mMyName = Util.getDisplayName();
            mMyPhoto = firebaseUser.getPhotoUrl()+"";
        }
    }

    private void initSearchField() {
        mIdEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mQuery = mIdEditTxt.getText().toString();
                updateGames(mDataSnapshot);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initRecycler() {
        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        mAdapter = new JoinGamesAdapter((JoinActivity) getActivity(), mGames, mMyId, mMyEmail,
                mMyName, mMyPhoto);
        mGamesRecycler.setAdapter(mAdapter);
    }

    private void initDatabase() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Query query = database.child("games").orderByChild("time");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDataSnapshot = dataSnapshot;
                updateGames(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Updates the games list.
     */
    private void updateGames(DataSnapshot dataSnapshot) {
        if (dataSnapshot == null) return;

        mGames.clear();
        if (mMyId != null) {
            for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                Game game = gameSnapshot.getValue(Game.class);

                if (!TextUtils.isEmpty(mQuery) && game.getId().contains(mQuery)
                        && !game.getAuthor().getUserId().equals(mMyId)) {
                    mGames.add(game);
                }
            }
        }
        mAdapter.notifyDataSetChanged();

        mTitleTxt.setVisibility(mGames.size() > 0 ? View.VISIBLE : View.GONE);
    }

}
