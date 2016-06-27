package com.moyersoftware.contender.game;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JoinLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    // Constants
    private static final int LOCATION_PERMISSION_CODE = 0;
    private static final float GAME_SEARCH_RADIUS_MILES = 100;
    private static final float GAME_SEARCH_RADIUS_METERS = (float) (GAME_SEARCH_RADIUS_MILES
            * 1609.34);

    // Views
    @Bind(R.id.join_location_recycler)
    RecyclerView mGamesRecycler;
    @Bind(R.id.join_location_title_txt)
    TextView mTitleTxt;
    @Bind(R.id.join_location_search_txt)
    TextView mSearchTxt;

    // Usual variables
    private ArrayList<Game> mGames = new ArrayList<>();
    private DataSnapshot mDataSnapshot;
    private JoinGamesAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mMyLocation;
    private String mMyId;

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

        initUser();
        initRecycler();
        initDatabase();
        initGoogleClient();

        return view;
    }

    private void initUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mMyId = firebaseUser.getUid();
        }
    }

    private void initGoogleClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void initRecycler() {
        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        mAdapter = new JoinGamesAdapter((JoinActivity) getActivity(), mGames, mMyId);
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

                if (game.getLatitude() != 0 && game.getLongitude() != 0) {
                    Location gameLocation = new Location("");
                    gameLocation.setLatitude(game.getLatitude());
                    gameLocation.setLongitude(game.getLongitude());
                    if (mMyLocation != null && mMyLocation.distanceTo(gameLocation)
                            < GAME_SEARCH_RADIUS_METERS && !game.getAuthorId().equals(mMyId)) {
                        mGames.add(game);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();

        mTitleTxt.setVisibility(mGames.size() > 0 ? View.VISIBLE : View.GONE);
        if (mGames.size() > 0) {
            mSearchTxt.setVisibility(View.GONE);
        } else {
            mSearchTxt.setText(R.string.join_location_empty);
            mSearchTxt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            } else {
                mMyLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        } else {
            mMyLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        updateGames(mDataSnapshot);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMyLocation = LocationServices.FusedLocationApi.getLastLocation
                            (mGoogleApiClient);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
