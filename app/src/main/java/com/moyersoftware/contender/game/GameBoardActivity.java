package com.moyersoftware.contender.game;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.print.PrintHelper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.GameBoardAdapter;
import com.moyersoftware.contender.game.adapter.GameFriendsAdapter;
import com.moyersoftware.contender.game.adapter.GameFriendsSquaresAdapter;
import com.moyersoftware.contender.game.adapter.GamePaidPlayersAdapter;
import com.moyersoftware.contender.game.adapter.GamePlayersAdapter;
import com.moyersoftware.contender.game.adapter.GameRowAdapter;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.game.data.SelectedSquare;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.CustomLinearLayout;
import com.moyersoftware.contender.util.StandardGestures;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameBoardActivity extends AppCompatActivity {

    // Constants
    private final static int COLUMNS_COUNT = 10;
    public static final String EXTRA_GAME_ID = "GameId";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 0;

    // Views
    @Bind(R.id.board_game_img)
    ImageView mGameImg;
    @Bind(R.id.board_recycler)
    RecyclerView mBoardRecycler;
    @Bind(R.id.board_row_recycler)
    RecyclerView mRowRecycler;
    @Bind(R.id.board_column_recycler)
    RecyclerView mColumnRecycler;
    @Bind(R.id.board_horizontal_scroll_view)
    HorizontalScrollView mHorizontalScrollView;
    @Bind(R.id.board_bottom_sheet)
    View mBottomSheet;
    @Bind(R.id.board_info_home_img)
    ImageView mTeam1Img;
    @Bind(R.id.board_info_away_img)
    ImageView mTeam2Img;
    @Bind(R.id.board_title_txt)
    TextView mTitleTxt;
    @Bind(R.id.board_layout)
    CustomLinearLayout mLayout;
    @Bind(R.id.board_away_name_txt)
    TextView mAwayNameTxt;
    @Bind(R.id.board_home_name_txt)
    TextView mHomeNameTxt;
    @Bind(R.id.board_info_away_name_txt)
    TextView mInfoAwayNameTxt;
    @Bind(R.id.board_info_home_name_txt)
    TextView mInfoHomeNameTxt;
    @Bind(R.id.board_info_home_total_score_txt)
    TextView mInfoHomeTotalScoreTxt;
    @Bind(R.id.board_info_away_total_score_txt)
    TextView mInfoAwayTotalScoreTxt;
    @Bind(R.id.board_info_q1_score_txt)
    TextView mQ1ScoreTxt;
    @Bind(R.id.board_info_q2_score_txt)
    TextView mQ2ScoreTxt;
    @Bind(R.id.board_info_q3_score_txt)
    TextView mQ3ScoreTxt;
    @Bind(R.id.board_info_final_score_txt)
    TextView mFinalScoreTxt;
    @Bind(R.id.board_info_time_txt)
    TextView mTimeTxt;
    @Bind(R.id.board_pdf_img)
    ImageView mPdfImg;
    @Bind(R.id.board_progress_txt)
    View mProgressBar;
    @Bind(R.id.board_invite_friends_img)
    ImageView mInviteFriendsImg;
    @Bind(R.id.board_paid_players_img)
    ImageView mPaidPlayersImg;
    @Bind(R.id.board_manual_add_img)
    ImageView mManualAddImg;
    @Bind(R.id.board_info_q1_winner_img)
    ImageView mWinner1Img;
    @Bind(R.id.board_info_q2_winner_img)
    ImageView mWinner2Img;
    @Bind(R.id.board_info_q3_winner_img)
    ImageView mWinner3Img;
    @Bind(R.id.board_info_final_winner_img)
    ImageView mWinnerFinalImg;
    @Bind(R.id.board_info_squares_txt)
    TextView mSquaresTxt;
    @Bind(R.id.page_indicator)
    CirclePageIndicator mPageIndicator;
    @Bind(R.id.board_details_txt)
    TextView mDetailsTxt;
    @Bind(R.id.contender_logo)
    View mContenderLogo;
    @Bind(R.id.board)
    View mBoard;
    @Bind(R.id.board_pdf_q1_txt)
    TextView mPdfQ1Txt;
    @Bind(R.id.board_pdf_q2_txt)
    TextView mPdfQ2Txt;
    @Bind(R.id.board_pdf_q3_txt)
    TextView mPdfQ3Txt;
    @Bind(R.id.board_pdf_final_txt)
    TextView mPdfFinalTxt;

    // Usual variables
    private int mTotalScrollY;
    private LinearLayoutManager mColumnLayoutManager;
    private LinearLayoutManager mRowLayoutManager;
    private DatabaseReference mDatabase;
    private String mGameId;
    private ArrayList<Integer> mRowNumbers = new ArrayList<>();
    private ArrayList<Integer> mColumnNumbers = new ArrayList<>();
    private ArrayList<SelectedSquare> mSelectedSquares = new ArrayList<>();
    private GameRowAdapter mColumnAdapter;
    private GameRowAdapter mRowAdapter;
    private String mGameName;
    private String mMyId;
    private String mMyName;
    private String mMyPhoto;
    private GameBoardAdapter mBoardAdapter;
    private Boolean mGameLive = false;
    private boolean mPendingUpload = false;
    private boolean mIgnoreUpdate = false;
    private ArrayList<String> mPlayerEmails = new ArrayList<>();
    private String mAuthorId;
    private AlertDialog mPlayersDialog;
    private ArrayList<Player> mPlayers = new ArrayList<>();
    private ArrayList<String> mPaidPlayers = new ArrayList<>();
    private GamePlayersAdapter mPlayersAdapter;
    private String mMyEmail;
    private String mDeviceOwnerId;
    private AlertDialog mInviteFriendsDialog;
    private ArrayList<Friend> mFriends = new ArrayList<>();
    private GameFriendsAdapter mFriendsAdapter;
    private ArrayList<String> mInvitedFriendIds = new ArrayList<>();
    private int mRemoveSquarePos;
    private ArrayList<String> mFriendIds = new ArrayList<>();
    private ArrayList<Integer> mSelectedSquaresCount = new ArrayList<>();
    private GameInvite.Game mGame;
    private GameFriendsSquaresAdapter mFriendsSquaresAdapter;
    private Event mEvent;
    private RecyclerView recycler;
    private boolean mIsHost;
    private GamePaidPlayersAdapter mPaidPlayersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);
        ButterKnife.bind(this);

        mGameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        if (TextUtils.isEmpty(mGameId)) return;

        Util.setCurrentPlayerId(null);

        initUser();
        initRowRecycler();
        initColumnRecycler();
        initBoardRecycler();
        initHorizontalScrollView();
        initBottomSheet();
        initDatabase();
        initScaleLayout();
        loadPlayers();
        loadPaidPlayers();
        loadFriends();
        initBoardLayout();
    }

    private void initBoardLayout() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mBoard.getLayoutParams().width = size.x;
        mBoard.requestFocus();
    }

    private void loadPaidPlayers() {
        mDatabase.child("games").child(mGameId).child("paid_players").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPaidPlayers.clear();
                        for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                            if (playerSnapshot.getValue(Boolean.class)) {
                                mPaidPlayers.add(playerSnapshot.getKey());
                            }
                        }
                        try {
                            mPaidPlayersAdapter.setPaidPlayers(mPaidPlayers);
                            mPaidPlayersAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            // Dialog is not shown yet
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void initScaleLayout() {
        mLayout.setOnTouchListener(new StandardGestures(this, mBoardAdapter, mRowAdapter,
                mColumnAdapter, mRowRecycler, mColumnRecycler, mBoardRecycler));

        int height = Util.getCellSize();

        mColumnRecycler.getLayoutParams().width = height;
        mColumnRecycler.getLayoutParams().height = height * 10;

        mRowRecycler.getLayoutParams().width = height * 10;
        mRowRecycler.getLayoutParams().height = height;

        ((FrameLayout.LayoutParams) mBoardRecycler.getLayoutParams())
                .topMargin = height;
        mBoardRecycler.requestLayout();

        ((FrameLayout.LayoutParams) mColumnRecycler.getLayoutParams())
                .topMargin = height;
        mColumnRecycler.requestLayout();

        ((FrameLayout.LayoutParams) mRowRecycler.getLayoutParams())
                .leftMargin = height;
        mRowRecycler.requestLayout();

        mBoardRecycler.setPadding(height, 0, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancelAll();
    }

    private void loadPlayers() {
        mDatabase.child("games").child(mGameId).child("players").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPlayers.clear();
                        mSelectedSquaresCount.clear();
                        if (mAuthorId.equals(mMyId)) {
                            mPlayers.add(new Player(mMyId, null, mMyEmail, mMyName, mMyPhoto));
                            int playerSquares = 0;
                            for (SelectedSquare selectedSquare : mSelectedSquares) {
                                String playerId = mMyId;
                                if (selectedSquare.getAuthorId().equals(playerId)) {
                                    playerSquares++;
                                }
                            }
                            mSelectedSquaresCount.add(playerSquares);
                        }
                        for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                            Player player = playerSnapshot.getValue(Player.class);
                            if (!TextUtils.isEmpty(player.getCreatedByUserId())
                                    && player.getCreatedByUserId().equals(mDeviceOwnerId)) {
                                mPlayers.add(player);

                                int playerSquares = 0;
                                for (SelectedSquare selectedSquare : mSelectedSquares) {
                                    String playerId = player.getUserId();
                                    if (selectedSquare.getAuthorId().equals(playerId)) {
                                        playerSquares++;
                                    }
                                }
                                mSelectedSquaresCount.add(playerSquares);
                            }
                        }
                        if (mPlayersAdapter != null) {
                            mPlayersAdapter.notifyDataSetChanged();
                        }

                        final int playersSize = mPlayers.size() - 1;
                        Util.Log("scroll to bottom");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            recycler.scrollToPosition(playersSize);
                                        } catch (Exception e) {
                                        }
                                    }
                                });
                            }
                        }, 1000);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void initUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mDeviceOwnerId = firebaseUser.getUid();
            mMyId = firebaseUser.getUid();
            Util.setCurrentPlayerId(mMyId);
            mMyName = Util.getDisplayName();
            if (TextUtils.isEmpty(mMyName)) {
                mMyName = firebaseUser.getEmail();
            }
            mMyEmail = firebaseUser.getEmail();
            mMyPhoto = Util.getPhoto();
        }
    }

    public String getCurrentId() {
        return mMyId;
    }

    private void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("games").child(mGameId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Util.Log("data change: " + dataSnapshot.toString());

                if (!dataSnapshot.exists()) {
                    Toast.makeText(GameBoardActivity.this, "Game not found", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    GameInvite.Game game = dataSnapshot.getValue(GameInvite.Game.class);

                    if (mGame != game) {
                        initGameDetails(game);
                        mGame = game;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mDatabase.child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mGame != null) {
                    initGameDetails(mGame);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initGameDetails(GameInvite.Game game) {
        Util.Log("game limit: " + game.getSquaresLimit());

        Util.Log("initGameDetails");
        if (mIgnoreUpdate) {
            mIgnoreUpdate = false;
            return;
        }

        // Set game image
        Picasso.with(this).load(game.getImage()).centerCrop().fit()
                .placeholder(R.drawable.placeholder).into(mGameImg);

        // Set game name
        mGameName = game.getName();
        mTitleTxt.setText(mGameName);

        // Update row numbers
        mRowNumbers.clear();
        mRowNumbers.addAll(game.getRowNumbers());
        mRowAdapter.notifyDataSetChanged();

        // Update column numbers
        mColumnNumbers.clear();
        mColumnNumbers.addAll(game.getColumnNumbers());
        mColumnAdapter.notifyDataSetChanged();

        // Update prices
        mQ1ScoreTxt.setText("Q1: " + game.getQuarter1Price() + " points");
        mQ2ScoreTxt.setText("Q2: " + game.getQuarter2Price() + " points");
        mQ3ScoreTxt.setText("Q3: " + game.getQuarter3Price() + " points");
        mFinalScoreTxt.setText("FINAL: " + game.getFinalPrice() + " points");

        mPdfQ1Txt.setText(String.valueOf(game.getQuarter1Price()));
        mPdfQ2Txt.setText(String.valueOf(game.getQuarter2Price()));
        mPdfQ3Txt.setText(String.valueOf(game.getQuarter3Price()));
        mPdfFinalTxt.setText(String.valueOf(game.getFinalPrice()));

        // Update winners
        mWinner1Img.setVisibility(game.getQuarter1Winner() == null ? View.GONE : View.VISIBLE);
        if (game.getQuarter1Winner() != null) {
            Picasso.with(this).load(game.getQuarter1Winner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinner1Img);

        }
        mWinner2Img.setVisibility(game.getQuarter2Winner() == null ? View.GONE : View.VISIBLE);
        if (game.getQuarter2Winner() != null) {
            Picasso.with(this).load(game.getQuarter2Winner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinner2Img);
        }
        mWinner3Img.setVisibility(game.getQuarter3Winner() == null ? View.GONE : View.VISIBLE);
        if (game.getQuarter3Winner() != null) {
            Picasso.with(this).load(game.getQuarter3Winner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinner3Img);

        }
        mWinnerFinalImg.setVisibility(game.getFinalWinner() == null ? View.GONE
                : View.VISIBLE);
        if (game.getFinalWinner() != null) {
            Picasso.with(this).load(game.getFinalWinner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinnerFinalImg);
        }

        // Get players
        mPlayerEmails.clear();
        mPlayerEmails.add(game.getAuthor().getEmail());
        mAuthorId = game.getAuthor().getUserId();

        mIsHost = mAuthorId.equals(mMyId);

        // Show rules dialog
        if (!Util.rulesShown(game.getId()) && !mIsHost) showRulesDialog(game);

        if (game.getPlayers() != null) {
            for (Player player : game.getPlayers()) {
                mPlayerEmails.add(player.getEmail());
            }
        }

        // Update selected squares
        mSelectedSquares.clear();
        ArrayList<SelectedSquare> selectedSquares = game.getSelectedSquares();
        if (selectedSquares == null) selectedSquares = new ArrayList<>();

        int mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : selectedSquares) {
            mSelectedSquares.add(selectedSquare);
            String playerId = Util.getCurrentPlayerId();
            if (TextUtils.isEmpty(playerId)) {
                playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            if (selectedSquare.getAuthorId().equals(playerId)) {
                mySelectedSquares++;
            }
        }
        mSquaresTxt.setText("◻ " + mySelectedSquares + " selected");
        mBoardAdapter.refresh(mSelectedSquares);

        // TODO: Remove
        mBoardAdapter.setRowNumbers(mRowNumbers);
        mBoardAdapter.setColumnNumbers(mColumnNumbers);

        updateLiveState();

        Util.Log("game = " + game.getId());
        mDatabase.child("events").child(game.getEventId()).addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Event event = dataSnapshot.getValue(Event.class);
                        if (event != null) {
                            mEvent = event;

                            mGameLive = (event.getTime() != -1 && System.currentTimeMillis()
                                    > event.getTime()) || mSelectedSquares.size() == 100;
                            mColumnAdapter.setLive(mGameLive);
                            mRowAdapter.setLive(mGameLive);
                            mBoardAdapter.setLive(mGameLive);
                            try {
                                mBoardAdapter.setScore(Integer.parseInt(event.getTeamHome()
                                        .getScore().getTotal()), Integer.parseInt(event
                                        .getTeamAway().getScore().getTotal()));
                            } catch (Exception e) {
                            }
                            mColumnAdapter.notifyDataSetChanged();
                            mRowAdapter.notifyDataSetChanged();
                            mBoardAdapter.notifyDataSetChanged();

                            mTimeTxt.setText(event.getTimeText());

                            mAwayNameTxt.setText(event.getTeamAway().getName());
                            mHomeNameTxt.setText(event.getTeamHome().getName());

                            // Init bottom section info
                            mInfoAwayNameTxt.setText(event.getTeamAway().getName());
                            mInfoHomeNameTxt.setText(event.getTeamHome().getName());

                            Picasso.with(GameBoardActivity.this).load(event.getTeamHome()
                                    .getImage()).into(mTeam1Img);
                            Picasso.with(GameBoardActivity.this).load(event.getTeamAway()
                                    .getImage()).into(mTeam2Img);

                            if (event.getTeamAway().getScore() != null && !TextUtils.isEmpty
                                    (event.getTeamAway().getScore().getTotal()) && !TextUtils
                                    .isEmpty(event.getTeamHome().getScore().getTotal())) {
                                mInfoAwayTotalScoreTxt.setText(event.getTeamAway().getScore()
                                        .getTotal());
                                mInfoHomeTotalScoreTxt.setText(event.getTeamHome().getScore()
                                        .getTotal());
                            } else {
                                mInfoAwayTotalScoreTxt.setText("00");
                                mInfoHomeTotalScoreTxt.setText("00");
                            }
                        } else {
                            Toast.makeText(GameBoardActivity.this, "Event not found",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        mPaidPlayersImg.setVisibility(mIsHost ? View.VISIBLE : View.GONE);
        mManualAddImg.setVisibility(mIsHost ? View.VISIBLE : View.GONE);
        mInviteFriendsImg.setVisibility(mIsHost ? View.VISIBLE : View.GONE);

        mDetailsTxt.setText(Html.fromHtml("Name: " + mGameName + "<br>ID: "
                + mGameId + "<br>Rules: " + (TextUtils.isEmpty(game.getRules())
                ? "No additional rules" : game.getRules())));
    }

    @SuppressWarnings("deprecation")
    private void showRulesDialog(GameInvite.Game game) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(game.getRules())) {
            dialogBuilder.setTitle("Rules");
            dialogBuilder.setMessage(Html.fromHtml(
                    game.getRules() +
                            "<br><br>"
                            + "<b>Points breakdown</b>"
                            + "<br>"
                            + "Q1: " + game.getQuarter1Price() + " points"
                            + "<br>"
                            + "Q2: " + game.getQuarter2Price() + " points"
                            + "<br>"
                            + "Q3: " + game.getQuarter3Price() + " points"
                            + "<br>"
                            + "Q4: " + game.getFinalPrice() + " points"));
        } else {
            dialogBuilder.setTitle("Points breakdown");

            dialogBuilder.setMessage(Html.fromHtml(
                    "Q1: " + game.getQuarter1Price() + " points"
                            + "<br>"
                            + "Q2: " + game.getQuarter2Price() + " points"
                            + "<br>"
                            + "Q3: " + game.getQuarter3Price() + " points"
                            + "<br>"
                            + "Q4: " + game.getFinalPrice() + " points"));
        }
        dialogBuilder.setPositiveButton("Got it", null);
        dialogBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialogBuilder.create().show();

        Util.setRulesShown(game.getId());
    }

    private void initBoardRecycler() {
        mBoardRecycler.setLayoutManager(new GridLayoutManager(this, COLUMNS_COUNT));
        mBoardRecycler.setHasFixedSize(true);
        mBoardAdapter = new GameBoardAdapter(this);
        mBoardRecycler.setAdapter(mBoardAdapter);

        mBoardRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mTotalScrollY += dy;

                mColumnLayoutManager.scrollToPositionWithOffset(0, -mTotalScrollY);
            }
        });

        registerForContextMenu(mBoardRecycler);
    }

    private void initRowRecycler() {
        mRowLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mRowRecycler.setLayoutManager(mRowLayoutManager);
        mRowRecycler.setHasFixedSize(true);
        mRowAdapter = new GameRowAdapter(this, mRowNumbers);
        mRowRecycler.setAdapter(mRowAdapter);
        mRowRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void initColumnRecycler() {
        mColumnLayoutManager = new LinearLayoutManager(this);
        mColumnRecycler.setLayoutManager(mColumnLayoutManager);
        mColumnRecycler.setHasFixedSize(true);
        mColumnAdapter = new GameRowAdapter(this, mColumnNumbers);
        mColumnRecycler.setAdapter(mColumnAdapter);
        mColumnRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void initHorizontalScrollView() {
        mHorizontalScrollView.getViewTreeObserver().addOnScrollChangedListener
                (new ViewTreeObserver.OnScrollChangedListener() {

                    @Override
                    public void onScrollChanged() {
                        int scrollX = mHorizontalScrollView.getScrollX();

                        mRowLayoutManager.scrollToPositionWithOffset(0, -scrollX);
                    }
                });
    }

    private void initBottomSheet() {
        WizardPagerAdapter adapter = new WizardPagerAdapter();
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        BottomSheetBehavior behavior = BottomSheetBehavior.from(mBottomSheet);
        behavior.setPeekHeight(Util.convertDpToPixel(48));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    pager.setCurrentItem(0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mPageIndicator.setViewPager(pager);
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onBackButtonClicked(View view) {
        finish();
    }

    public void onPrintButtonClicked(View view) {
        mLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mBoardAdapter.setPrintMode(true);
        mBoardRecycler.post(new Runnable() {
            @Override
            public void run() {
                takeScreenshot();
            }
        });
    }

    private void takeScreenshot() {
        int boardSize = (int) (getResources().getDimension(R.dimen.board_cell_size) * 11
                + Util.convertDpToPixel(32));
        mLayout.getLayoutParams().width = boardSize;
        mLayout.getLayoutParams().height = boardSize;
        mLayout.requestLayout();
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                PrintHelper photoPrinter = new PrintHelper(GameBoardActivity.this);
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                Bitmap bitmap = loadBitmapFromView(mLayout);
                photoPrinter.printBitmap(mGameName + ", Contender", bitmap);

                mLayout.getLayoutParams().width = ViewPager.LayoutParams.MATCH_PARENT;
                mLayout.getLayoutParams().height = ViewPager.LayoutParams.MATCH_PARENT;
                mLayout.requestLayout();
                mBoardAdapter.setPrintMode(false);

                mProgressBar.setVisibility(View.GONE);
                mLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private static String FILE = Environment.getExternalStorageDirectory() + "/Contender.pdf";

    public void onPdfButtonClicked(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            } else {
                onPdfButtonClicked();
            }
        } else {
            onPdfButtonClicked();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPdfButtonClicked();
                }
            }
        }
    }

    private void onPdfButtonClicked() {
        mLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mBoardAdapter.setPrintMode(true);
        mContenderLogo.setVisibility(View.VISIBLE);
        mBoardRecycler.post(new Runnable() {
            @Override
            public void run() {
                makePdf();
            }
        });
    }

    private void makePdf() {
        int height = (int) getResources().getDimension(R.dimen.board_cell_size);

        mBoardAdapter.scale(height);
        mColumnAdapter.scale(height);
        mRowAdapter.scale(height);
        mBoardAdapter.notifyDataSetChanged();
        mColumnAdapter.notifyDataSetChanged();
        mRowAdapter.notifyDataSetChanged();

        mColumnRecycler.getLayoutParams().width = height;
        mColumnRecycler.getLayoutParams().height = height * 10;

        mRowRecycler.getLayoutParams().width = height * 10;
        mRowRecycler.getLayoutParams().height = height;

        ((FrameLayout.LayoutParams) mBoardRecycler.getLayoutParams())
                .topMargin = height;
        mBoardRecycler.requestLayout();

        ((FrameLayout.LayoutParams) mColumnRecycler.getLayoutParams())
                .topMargin = height;
        mColumnRecycler.requestLayout();

        ((FrameLayout.LayoutParams) mRowRecycler.getLayoutParams())
                .leftMargin = height;
        mRowRecycler.requestLayout();

        mBoard.getLayoutParams().width = height * 11 + Util.convertDpToPixel(32);
        mBoard.requestLayout();

        mBoardRecycler.setPadding(height, 0, 0, 0);

        int boardSize = height * 11 + Util.convertDpToPixel(32);
        mLayout.getLayoutParams().width = boardSize + Util.convertDpToPixel(12)
                + Util.convertDpToPixel(220);
        mLayout.getLayoutParams().height = boardSize + Util.convertDpToPixel(12);
        mLayout.requestLayout();
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmapFromView(mLayout);

                Document document = new Document(new Rectangle(bitmap.getWidth(),
                        bitmap.getHeight()), 0, 0, 0, 0);

                try {
                    PdfWriter.getInstance(document, new FileOutputStream(FILE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                document.open();

                addImage(document, bitmap);
                document.close();

                Util.Log("FILE = " + FILE);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                String[] playerEmails = new String[mPlayerEmails.size()];
                playerEmails = mPlayerEmails.toArray(playerEmails);
                intent.putExtra(Intent.EXTRA_EMAIL, playerEmails);
                intent.putExtra(Intent.EXTRA_SUBJECT, mGameName + " Contender board");
                File file = new File(FILE);
                if (!file.exists() || !file.canRead()) {
                    Toast.makeText(GameBoardActivity.this, "Attachment Error", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                Uri uri = FileProvider.getUriForFile(GameBoardActivity.this, getPackageName(),
                        file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Send PDF..."));

                mContenderLogo.setVisibility(View.GONE);

                mLayout.getLayoutParams().width = ViewPager.LayoutParams.MATCH_PARENT;
                mLayout.getLayoutParams().height = ViewPager.LayoutParams.MATCH_PARENT;
                mLayout.requestLayout();
                mBoardAdapter.setPrintMode(false);

                mProgressBar.setVisibility(View.GONE);
                mLayout.setVisibility(View.VISIBLE);

                mBoardAdapter.scale(Util.getCellSize());
                mColumnAdapter.scale(Util.getCellSize());
                mRowAdapter.scale(Util.getCellSize());
                mBoardAdapter.notifyDataSetChanged();
                mColumnAdapter.notifyDataSetChanged();
                mRowAdapter.notifyDataSetChanged();
                initScaleLayout();
                initBoardLayout();
            }
        });
    }

    private void addImage(Document document, Bitmap bitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            document.add(Image.getInstance(byteArray));

            Util.Log("saved = " + document.getPageSize().getWidth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    private Handler mHandler = new Handler();

    public void selectSquare(int position) {
        int mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            String playerId = Util.getCurrentPlayerId();
            if (TextUtils.isEmpty(playerId)) {
                playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            if (selectedSquare.getAuthorId().equals(playerId)) {
                mySelectedSquares++;
            }
        }

        int squaresLimit = mGame.getSquaresLimit();
        if (squaresLimit == 0) squaresLimit = 100;
        if (mySelectedSquares >= squaresLimit) {
            Toast.makeText(this, "You can't select more than " + mGame.getSquaresLimit()
                            + " squares",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean squareExists = false;
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            if (selectedSquare.getPosition() == position) squareExists = true;
        }
        if (!squareExists) {
            int column = 0;
            int row = position;
            while (row / 10 > 0) {
                row -= 10;
                column++;
            }
            mSelectedSquares.add(new SelectedSquare(mMyId, mMyName, mMyPhoto, column, row,
                    position));
            mBoardAdapter.refresh(mSelectedSquares, position);

            uploadSquares();
        }

        updateLiveState();

        mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            String playerId = Util.getCurrentPlayerId();
            if (TextUtils.isEmpty(playerId)) {
                playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            if (selectedSquare.getAuthorId().equals(playerId)) {
                mySelectedSquares++;
            }
        }
        mSquaresTxt.setText("◻ " + mySelectedSquares + " selected");
    }

    public void onManualAddButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle("Select or add player");
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_player, null);
        recycler = (RecyclerView) dialogView.findViewById
                (R.id.friends_select_recycler);
        final EditText editTxt = (EditText) dialogView.findViewById(R.id.friends_select_edit_txt);
        View addBtn = dialogView.findViewById(R.id.friends_select_add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editTxt.getText().toString())) {
                    Toast.makeText(GameBoardActivity.this, "Name is empty", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mDatabase.child("games").child(mGameId).child("players")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GenericTypeIndicator<ArrayList<Player>> t
                                            = new GenericTypeIndicator<ArrayList<Player>>() {
                                    };
                                    ArrayList<Player> players = dataSnapshot.getValue(t);
                                    if (players == null) players = new ArrayList<>();

                                    players.add(new Player(Util.generatePlayerId(), mDeviceOwnerId,
                                            null, editTxt.getText().toString(), null));

                                    mDatabase.child("games").child(mGameId).child("players")
                                            .setValue(players);

                                    int mySelectedSquares = 0;
                                    for (SelectedSquare selectedSquare : mSelectedSquares) {
                                        String playerId = Util.getCurrentPlayerId();
                                        if (TextUtils.isEmpty(playerId)) {
                                            playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        }
                                        if (selectedSquare.getAuthorId().equals(playerId)) {
                                            mySelectedSquares++;
                                        }
                                    }
                                    mSquaresTxt.setText("◻ " + mySelectedSquares + " selected");

                                    editTxt.setText("");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                }
            }
        });
        mPlayersAdapter = new GamePlayersAdapter(this, mPlayers, mMyId);
        recycler.setAdapter(mPlayersAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        dialogBuilder.setView(dialogView);
        dialogBuilder.setNegativeButton("Close", null);
        mPlayersDialog = dialogBuilder.create();
        mPlayersDialog.show();
    }

    public void selectPlayer(Player player) {
        mMyId = player.getUserId();
        Util.setCurrentPlayerId(mMyId);
        mMyEmail = player.getEmail();
        mMyName = player.getName();
        if (TextUtils.isEmpty(mMyName)) {
            mMyName = player.getEmail();
        }
        mMyPhoto = player.getPhoto();
        mPlayersAdapter.setCurrentPlayerId(mMyId);

        int mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            String playerId = Util.getCurrentPlayerId();
            if (TextUtils.isEmpty(playerId)) {
                playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            if (selectedSquare.getAuthorId().equals(playerId)) {
                mySelectedSquares++;
            }
        }
        mSquaresTxt.setText("◻ " + mySelectedSquares + " selected");
    }

    public void onInviteFriendsButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);

        if (!mGameLive) {
            dialogBuilder.setTitle("Invite friends");
            @SuppressLint("InflateParams")
            RecyclerView recycler = (RecyclerView) LayoutInflater.from(this).inflate
                    (R.layout.dialog_invite_friends, null);
            mFriendsAdapter = new GameFriendsAdapter(this, mFriends, mInvitedFriendIds);
            recycler.setAdapter(mFriendsAdapter);
            recycler.setLayoutManager(new LinearLayoutManager(this));
            dialogBuilder.setView(recycler);
        } else {
            dialogBuilder.setTitle("Players");
            @SuppressLint("InflateParams")
            RecyclerView recycler = (RecyclerView) LayoutInflater.from(this).inflate
                    (R.layout.dialog_invite_friends, null);
            mFriendsSquaresAdapter = new GameFriendsSquaresAdapter(mPlayers, mSelectedSquaresCount);
            recycler.setAdapter(mFriendsSquaresAdapter);
            recycler.setLayoutManager(new LinearLayoutManager(this));
            dialogBuilder.setView(recycler);
        }
        dialogBuilder.setNegativeButton("Close", null);
        mInviteFriendsDialog = dialogBuilder.create();
        mInviteFriendsDialog.show();
    }

    public void onPaidPlayersButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);

        dialogBuilder.setTitle("Players who paid");
        @SuppressLint("InflateParams")
        RecyclerView recycler = (RecyclerView) LayoutInflater.from(this).inflate
                (R.layout.dialog_paid_players, null);
        mPaidPlayersAdapter = new GamePaidPlayersAdapter(this, mPlayers);
        mPaidPlayersAdapter.setPaidPlayers(mPaidPlayers);
        recycler.setAdapter(mPaidPlayersAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        dialogBuilder.setView(recycler);
        dialogBuilder.setNegativeButton("Close", null);
        dialogBuilder.create().show();
    }

    private void loadFriends() {
        mDatabase.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFriends.clear();
                mFriendIds.clear();
                for (DataSnapshot friendshipSnapshot : dataSnapshot.getChildren()) {
                    final Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                    if (friendship.getUser1Id().equals(mMyId)
                            || friendship.getUser2Id().equals(mMyId)) {
                        final String friendId = friendship.getUser1Id().equals(mMyId)
                                ? friendship.getUser2Id() : friendship.getUser1Id();

                        mDatabase.child("users").child(friendId).addListenerForSingleValueEvent
                                (new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        Util.Log("data change: " + user.getName());

                                        if (!friendship.isPending()) {
                                            Friend friend = new Friend(dataSnapshot.getKey(),
                                                    user.getName(), user.getUsername(),
                                                    user.getImage(), user.getEmail(), false);
                                            if (!mFriendIds.contains(friend.getId())) {
                                                mFriends.add(friend);
                                                mFriendIds.add(friend.getId());
                                            }
                                            if (mFriendsAdapter != null) {
                                                mFriendsAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        mInvitedFriendIds.add(mAuthorId);
                                        if (mFriendsAdapter != null) {
                                            mFriendsAdapter.notifyDataSetChanged();
                                        }
                                        for (final Friend friend : mFriends) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("game_invites").child(friend.getId())
                                                    .addListenerForSingleValueEvent
                                                            (new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    for (DataSnapshot gameInviteSnapshot : dataSnapshot.getChildren()) {
                                                                        GameInvite gameInvite = gameInviteSnapshot.getValue(GameInvite.class);
                                                                        if (gameInvite.getGame().getId().equals(mGameId)) {
                                                                            mInvitedFriendIds.add(friend.getId());
                                                                            if (mFriendsAdapter != null) {
                                                                                mFriendsAdapter.notifyDataSetChanged();
                                                                            }
                                                                        }
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                }
                                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void inviteFriend(final Friend friend) {
        mInvitedFriendIds.add(friend.getId());
        mFriendsAdapter.notifyDataSetChanged();

        mDatabase.child("game_invites").child(friend.getId()).push()
                .setValue(new GameInvite(Util.getDisplayName(), mGame));
    }

    public void openRemoveMenu(SelectedSquare selectedSquare) {
        openContextMenu(mBoardRecycler);
        mRemoveSquarePos = selectedSquare.getPosition();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add("Remove this square");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            if (selectedSquare.getPosition() == mRemoveSquarePos) {
                mSelectedSquares.remove(selectedSquare);
                break;
            }
        }

        mBoardAdapter.refresh(mSelectedSquares, mRemoveSquarePos);

        int mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            String playerId = Util.getCurrentPlayerId();
            if (TextUtils.isEmpty(playerId)) {
                playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            if (selectedSquare.getAuthorId().equals(playerId)) {
                mySelectedSquares++;
            }
        }
        mSquaresTxt.setText("◻ " + mySelectedSquares + " selected");

        uploadSquares();
        return super.onContextItemSelected(item);
    }

    public void onCopyIdButtonClicked(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Game ID", mGameId);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(GameBoardActivity.this, "Game ID is copied to clipboard",
                Toast.LENGTH_SHORT).show();
    }

    private void uploadSquares() {
        mIgnoreUpdate = true;
        mDatabase.child("games").child(mGameId).child("selectedSquares")
                .setValue(mSelectedSquares);
        mPendingUpload = false;
    }

    private void updateLiveState() {
        if (mSelectedSquares.size() == 100 != mGameLive) {
            mGameLive = mSelectedSquares.size() == 100;
            mBoardAdapter.setLive(mGameLive);
            mRowAdapter.setLive(mGameLive);
            mColumnAdapter.setLive(mGameLive);
            mBoardAdapter.notifyDataSetChanged();
            mRowAdapter.notifyDataSetChanged();
            mColumnAdapter.notifyDataSetChanged();

            mInviteFriendsImg.setImageResource(!mGameLive ? R.drawable.friend_invite
                    : R.drawable.friend_invite_live);
        }
    }

    public void showWinner1(View view) {
        Toast.makeText(this, mGame.getQuarter1Winner().getPlayer().getName()
                + " won the first quarter!", Toast.LENGTH_SHORT)
                .show();
    }

    public void showWinner2(View view) {
        Toast.makeText(this, mGame.getQuarter2Winner().getPlayer().getName()
                + " won the second quarter!", Toast.LENGTH_SHORT)
                .show();
    }

    public void showWinner3(View view) {
        Toast.makeText(this, mGame.getQuarter3Winner().getPlayer().getName()
                + " won the third quarter!", Toast.LENGTH_SHORT)
                .show();
    }

    public void showWinnerFinal(View view) {
        Toast.makeText(this, mGame.getFinalWinner().getPlayer().getName()
                + " won the final quarter!", Toast.LENGTH_SHORT)
                .show();
    }

    public void setPlayerPaid(final String playerId, final boolean checked) {
        mDatabase.child("games").child(mGameId).child("paid_players").child(playerId)
                .setValue(checked);
    }


    class WizardPagerAdapter extends PagerAdapter {

        public Object instantiateItem(ViewGroup collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.page_one;
                    break;
                case 1:
                    resId = R.id.page_two;
                    break;
            }
            return findViewById(resId);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }
    }
}
