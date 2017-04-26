package com.moyersoftware.contender.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.HostActivity;
import com.moyersoftware.contender.game.HowToPlayActivity;
import com.moyersoftware.contender.game.HowToUseActivity;
import com.moyersoftware.contender.game.JoinActivity;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.menu.adapter.GamesAdapter;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GamesFragment extends Fragment {

    // Views
    @Bind(R.id.games_recycler)
    RecyclerView mGamesRecycler;
    @Bind(R.id.games_host_btn)
    Button mHostBtn;
    @Bind(R.id.games_join_btn)
    Button mJoinBtn;
    @Bind(R.id.games_title_txt)
    TextView mTitleTxt;
    @Bind(R.id.games_how_to_use_btn)
    Button mHowToUseBtn;
    @Bind(R.id.games_how_to_btn)
    Button mHowToBtn;
    @Bind(R.id.welcome_close_img)
    View mCloseImg;
    @Bind(R.id.welcome_layout)
    View mWelcomeLayout;

    // Usual variables
    private ArrayList<GameInvite.Game> mGames = new ArrayList<>();
    private GamesAdapter mAdapter;
    private HashMap<String, Long> mEventTimes = new HashMap<>();
    private String mGameToRemoveId;
    private FirebaseUser mFirebaseUser;
    private GameInvite.Game mRemoveGame;

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
        initDatabase();
        initButtons();
        initWelcomeLayout();

        return view;
    }

    private void initWelcomeLayout() {
        if (Util.showWelcomeBanner()) mWelcomeLayout.setVisibility(View.VISIBLE);
        mCloseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWelcomeLayout.setVisibility(View.GONE);
                Util.hideWelcomeBanner();
            }
        });
    }

    public void initDatabase() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            Query query = database.child("events");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mEventTimes.clear();
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        Util.Log("event: " + eventSnapshot);
                        try {
                            final Event event = eventSnapshot.getValue(Event.class);

                            if (event.getTime() > 0) {
                                mEventTimes.put(event.getId(), event.getTime());
                            } else {
                                mEventTimes.put(event.getId(), event.getTime());
                            }
                        } catch (Exception e) {
                        }
                    }

                    getGames(database, mFirebaseUser);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


        }
    }

    private void getGames(final DatabaseReference database, final FirebaseUser firebaseUser) {
        Query query = database.child("games").orderByChild("time");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update the games list
                mGames.clear();
                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    try {
                        final GameInvite.Game game = gameSnapshot.getValue(GameInvite.Game.class);
                        Util.Log("game = " + game.getId() + ", " + game.getName()+", "+game.getEventId());

                        if (game != null && (game.getAuthor().getUserId().equals(firebaseUser
                                .getUid()) || (game.getPlayers() != null && game.getPlayers()
                                .contains(new Player(firebaseUser.getUid(), null,
                                        firebaseUser.getEmail(),
                                        Util.getDisplayName(), Util.getPhoto()))))) {
                            game.setEventTime(mEventTimes.get(game.getEventId()));
                            if (!mGames.contains(game)) mGames.add(game);
                            if ((game.getSelectedSquares() == null || (game.getSelectedSquares() != null
                                    && game.getSelectedSquares().size() < 100))
                                    && (mEventTimes.get(game.getEventId()) == -2
                                    || mEventTimes.get(game.getEventId()) < System.currentTimeMillis())) {
                                String code = game.getCode();
                                if (!TextUtils.isEmpty(code)) {
                                    OkHttpClient client = new OkHttpClient();

                                    RequestBody formBody = new FormBody.Builder()
                                            .add("text", code)
                                            .build();
                                    Request request = new Request.Builder()
                                            .url(Util.SET_CODE_VALID_URL)
                                            .post(formBody)
                                            .build();

                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Util.Log("Can't set code as expired: " + e);
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response)
                                                throws IOException {
                                            if (!response.isSuccessful()) return;

                                            Util.Log("Make code for game " + game.getName() + " valid");
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Can't parse game
                    }
                }

                database.child("game_invites").child(mFirebaseUser.getUid()).addValueEventListener
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot gameInviteSnapshot : dataSnapshot.getChildren()) {
                                    try {
                                        GameInvite gameInvite = gameInviteSnapshot.getValue(GameInvite.class);
                                        Util.Log("add game = " + gameInvite.getGame().getEventTime());
                                        Util.Log(gameInvite.getName() + " invited you");
                                        if (gameInvite.getGame() != null) {
                                            GameInvite.Game game = gameInvite.getGame();
                                            if (mEventTimes.get(game.getEventId()) > 0) {
                                                game.setEventTime(mEventTimes.get(game.getEventId()));
                                                game.setInviteName(gameInvite.getName());
                                                game.setInviteId(gameInviteSnapshot.getKey());

                                                if (!mGames.contains(game)) mGames.add(game);
                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                }

                                try {
                                    Collections.sort(mGames, new GameComparator());
                                    mAdapter.notifyDataSetChanged();

                                    // Update the title text
                                    mTitleTxt.setText(mGames.size() > 0 ? R.string.games_title
                                            : R.string.games_title_empty);
                                } catch (Exception e) {
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Collections.sort(mGames, new GameComparator());
                                mAdapter.notifyDataSetChanged();

                                // Update the title text
                                mTitleTxt.setText(mGames.size() > 0 ? R.string.games_title
                                        : R.string.games_title_empty);
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public class GameComparator implements Comparator<GameInvite.Game> {
        public int compare(GameInvite.Game game1, GameInvite.Game game2) {
            return game1.getEventTime().compareTo(game2.getEventTime());
        }
    }

    private void initRecycler() {
        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        String myId = null;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        }
        mAdapter = new GamesAdapter((MainActivity) getActivity(), this, mGames, myId);
        mGamesRecycler.setAdapter(mAdapter);
        registerForContextMenu(mGamesRecycler);
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
                startActivity(new Intent(getActivity(), JoinActivity.class));
            }
        });
        mHowToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HowToPlayActivity.class));
            }
        });
        mHowToUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HowToUseActivity.class));
            }
        });
    }

    public void joinGame(String gameId) {
        startActivity(new Intent(getActivity(), GameBoardActivity.class)
                .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId));
    }

    public void deleteGame(final GameInvite.Game game) {
        mGameToRemoveId = game.getId();

        FirebaseDatabase.getInstance().getReference().child("events").child(game.getEventId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if (event != null) {

                    if (!game.getAuthor().getUserId().equals(mFirebaseUser.getUid())) {
                        mRemoveGame = game;
                        getActivity().openContextMenu(mGamesRecycler);
                    } else {
                        if (event.getTimeText().toLowerCase().contains("final") || (game.getPlayers()
                                == null && game.getSelectedSquares() == null)) {
                            mRemoveGame = game;
                            getActivity().openContextMenu(mGamesRecycler);
                        } else {
                            Toast.makeText(getActivity(),
                                    "You can only delete this game if it's finished or empty",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "You can't remove this game", Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (mRemoveGame.getAuthor().getUserId().equals(mFirebaseUser.getUid())) {
            menu.add("Delete game");
        } else {
            menu.add("Leave game");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!mRemoveGame.getAuthor().getUserId().equals(mFirebaseUser.getUid())) {
            FirebaseDatabase.getInstance().getReference().child("games").child(mRemoveGame.getId())
                    .child("players")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GenericTypeIndicator<ArrayList<Player>> t
                                    = new GenericTypeIndicator<ArrayList<Player>>() {
                            };
                            ArrayList<Player> players = dataSnapshot.getValue(t);
                            if (players == null) players = new ArrayList<>();

                            ArrayList<Player> playersCopy = new ArrayList<>(players);

                            try {
                                for (Player player : players) {
                                    if (player.getUserId().equals(FirebaseAuth.getInstance()
                                            .getCurrentUser().getUid())) {
                                        playersCopy.remove(player);
                                    }
                                }
                            } catch (Exception e) {
                                // Can't delete game
                            }

                            players = playersCopy;

                            FirebaseDatabase.getInstance().getReference().child("games")
                                    .child(mRemoveGame.getId()).child("players")
                                    .setValue(players);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        } else {
            FirebaseDatabase.getInstance().getReference().child("games").child(mGameToRemoveId)
                    .removeValue();
        }
        return true;
    }
}
