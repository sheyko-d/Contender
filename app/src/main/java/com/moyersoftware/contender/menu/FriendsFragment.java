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
import android.widget.TextView;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.menu.adapter.FriendsAdapter;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {

    // Constants
    private static final int REQUEST_INVITE = 0;

    // Views
    @Bind(R.id.friends_recycler)
    RecyclerView mFriendsRecycler;
    @Bind(R.id.friends_invite_btn)
    Button mInviteBtn;
    @Bind(R.id.friends_social_txt)
    TextView mSocialTxt;
    @Bind(R.id.friends_social_btn)
    TextView mSocialBtn;

    // Usual variables
    private ArrayList<Friend> mFriends = new ArrayList<>();
    private String mMyId;
    private DatabaseReference mDatabase;

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

        initUser();
        initDatabase();
        initRecycler();
        initInviteBtn();
        initFacebookInviteBtn();

        return view;
    }

    private void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void initUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mMyId = firebaseUser.getUid();
        }
    }

    private void initFacebookInviteBtn() {
        boolean canInviteFacebook = AppInviteDialog.canShow();
        mSocialBtn.setVisibility(canInviteFacebook ? View.VISIBLE : View.GONE);
        mSocialTxt.setVisibility(canInviteFacebook ? View.VISIBLE : View.GONE);
        if (canInviteFacebook) {
            mSocialBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retrieve the invite code for this user if it exists
                    mDatabase.child("invites").child(mMyId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String code = dataSnapshot.getValue(String.class);
                                    if (code == null) {
                                        code = createGameCode();
                                    }

                                    AppInviteContent content = new AppInviteContent.Builder()
                                            .setApplinkUrl(Util.INVITE_LINK)
                                            .setPreviewImageUrl(Util.INVITE_IMAGE)
                                            .setPromotionDetails("Code", code)
                                            .build();
                                    AppInviteDialog.show(getActivity(), content);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                }
            });
        }
    }

    private void initRecycler() {
        // TODO: Remove
        mFriends.clear();
        mFriends.add(new Friend("Jeff Spadaccini", "@jspadacc", "http://womensenews.org/files/NFL-football.jpg"));

        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setAdapter(new FriendsAdapter(getContext(), mFriends));
    }

    private void initInviteBtn() {
        mInviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the invite code for this user if it exists
                mDatabase.child("invites").child(mMyId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String code = dataSnapshot.getValue(String.class);
                                if (code == null) {
                                    code = createGameCode();
                                }
                                Util.Log("code = " + code);
                                sendInvite(code);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
            }
        });
    }

    private String createGameCode() {
        String gameCode = Util.generateGameCode();
        mDatabase.child("invites").child(mMyId).setValue(gameCode);
        return gameCode;
    }

    private void sendInvite(String code) {
        Intent intent = new AppInviteInvitation.IntentBuilder("Be a Sports Contender")
                .setMessage("Hey check this game out! (use this code: " + code + ")")
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }
}
