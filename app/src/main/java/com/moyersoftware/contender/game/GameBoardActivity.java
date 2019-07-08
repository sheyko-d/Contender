package com.moyersoftware.contender.game;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
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
import com.moyersoftware.contender.game.service.firebase.MyFirebaseMessagingService;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.menu.data.PaidPlayer;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.network.ApiFactory;
import com.moyersoftware.contender.util.CustomLinearLayout;
import com.moyersoftware.contender.util.StandardGestures;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.moyersoftware.contender.util.MyApplication.getContext;

public class GameBoardActivity extends AppCompatActivity {

    // Constants
    private final static int COLUMNS_COUNT = 10;
    public static final String EXTRA_GAME_ID = "GameId";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 0;

    // Views
    @BindView(R.id.board_game_img)
    ImageView mGameImg;
    @BindView(R.id.board_recycler)
    RecyclerView mBoardRecycler;
    @BindView(R.id.board_row_recycler)
    RecyclerView mRowRecycler;
    @BindView(R.id.board_column_recycler)
    RecyclerView mColumnRecycler;
    @BindView(R.id.board_horizontal_scroll_view)
    HorizontalScrollView mHorizontalScrollView;
    @BindView(R.id.board_title_txt)
    TextView mTitleTxt;
    @BindView(R.id.board_layout)
    CustomLinearLayout mLayout;
    @BindView(R.id.board_away_name_txt)
    TextView mAwayNameTxt;
    @BindView(R.id.board_home_name_txt)
    TextView mHomeNameTxt;
    @BindView(R.id.board_info_away_name_txt)
    TextView mInfoAwayNameTxt;
    @BindView(R.id.board_info_home_name_txt)
    TextView mInfoHomeNameTxt;
    @BindView(R.id.board_info_home_total_score_txt)
    TextView mInfoHomeTotalScoreTxt;
    @BindView(R.id.board_info_away_total_score_txt)
    TextView mInfoAwayTotalScoreTxt;
    @BindView(R.id.board_info_time_txt)
    TextView mTimeTxt;
    @BindView(R.id.board_progress_txt)
    View mProgressBar;
    @BindView(R.id.contender_logo)
    View mContenderLogo;
    @BindView(R.id.board)
    View mBoard;
    @BindView(R.id.board_pdf_q1_txt)
    TextView mPdfQ1Txt;
    @BindView(R.id.board_pdf_q2_txt)
    TextView mPdfQ2Txt;
    @BindView(R.id.board_pdf_q3_txt)
    TextView mPdfQ3Txt;
    @BindView(R.id.board_pdf_final_txt)
    TextView mPdfFinalTxt;
    @BindView(R.id.menuBtn)
    ImageView mMenuBtn;
    @BindView(R.id.board_quarter_home_txt)
    TextView mQuarterHomeTxt;
    @BindView(R.id.board_quarter_away_txt)
    TextView mQuarterAwayTxt;
    @BindView(R.id.board_home_q1_score_txt)
    TextView mHomeQ1ScoreTxt;
    @BindView(R.id.board_away_q1_score_txt)
    TextView mAwayQ1ScoreTxt;
    @BindView(R.id.board_home_q2_score_txt)
    TextView mHomeQ2ScoreTxt;
    @BindView(R.id.board_away_q2_score_txt)
    TextView mAwayQ2ScoreTxt;
    @BindView(R.id.board_home_q3_score_txt)
    TextView mHomeQ3ScoreTxt;
    @BindView(R.id.board_away_q3_score_txt)
    TextView mAwayQ3ScoreTxt;
    @BindView(R.id.board_home_q4_score_txt)
    TextView mHomeQ4ScoreTxt;
    @BindView(R.id.board_away_q4_score_txt)
    TextView mAwayQ4ScoreTxt;
    @BindView(R.id.board_home_final_score_txt)
    TextView mHomeFinalScoreTxt;
    @BindView(R.id.board_away_final_score_txt)
    TextView mAwayFinalScoreTxt;
    @BindView(R.id.team_home_bg)
    View mHomeBg;
    @BindView(R.id.team_away_bg)
    View mAwayBg;
    @BindView(R.id.board_score_txt)
    TextView mBoardTotalScoreTxt;
    @BindView(R.id.game_info_layout)
    View mGameInfoLayout;
    @BindView(R.id.board_info_title_txt)
    TextView mInfoTitleTxt;
    @BindView(R.id.board_info_score_txt)
    TextView mInfoScoreTxt;
    @BindView(R.id.board_rules_txt)
    TextView mRulesTxt;
    @BindView(R.id.players_layout)
    ViewGroup mPlayersLayout;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.scoresPager)
    ViewPager mScoresPager;
    @BindView(R.id.score_q1_title)
    TextView mScoreQ1Title;
    @BindView(R.id.score_q1_desc)
    TextView mScoreQ1Desc;
    @BindView(R.id.score_q2_title)
    TextView mScoreQ2Title;
    @BindView(R.id.score_q2_desc)
    TextView mScoreQ2Desc;
    @BindView(R.id.score_q3_title)
    TextView mScoreQ3Title;
    @BindView(R.id.score_q3_desc)
    TextView mScoreQ3Desc;
    @BindView(R.id.score_final_title)
    TextView mScoreFinalTitle;
    @BindView(R.id.score_final_desc)
    TextView mScoreFinalDesc;
    @BindView(R.id.scores_layout)
    View mScoresLayout;
    @BindView(R.id.board_home_q1_title_txt)
    TextView mHomeQ1TitleTxt;
    @BindView(R.id.board_home_q2_title_txt)
    TextView mHomeQ2TitleTxt;
    @BindView(R.id.board_home_q3_title_txt)
    TextView mHomeQ3TitleTxt;
    @BindView(R.id.board_home_q4_title_txt)
    TextView mHomeQ4TitleTxt;
    @BindView(R.id.board_home_final_title_txt)
    TextView mHomeFinalTitleTxt;
    @BindView(R.id.board_info_game_id_txt)
    TextView mGameIdTxt;

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
    private boolean mIgnoreUpdate = false;
    private ArrayList<String> mPlayerEmails = new ArrayList<>();
    private String mAuthorId;
    private AlertDialog mPlayersDialog;
    private ArrayList<Player> mPlayers = new ArrayList<>();
    private ArrayList<Player> mAllPlayers = new ArrayList<>();
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
    private Call<Void> mPaidPlayersCall = null;
    private ViewPager pager;
    private PopupWindow popupWindow;

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
        loadGame();
        initScaleLayout();
        loadFriends();
        initBoardLayout();
        registerRealTimeListener();
        initScoresPager();
    }

    private void initScoresPager() {
        ScoresPagerAdapter myPagerAdapter = new ScoresPagerAdapter();
        mScoresPager.setOffscreenPageLimit(4);
        mScoresPager.setAdapter(myPagerAdapter);
        mTabLayout.setupWithViewPager(mScoresPager);
    }

    private void registerRealTimeListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyFirebaseMessagingService.TYPE_GAMES_UPDATED);
        filter.addAction(MyFirebaseMessagingService.TYPE_EVENTS_UPDATED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadGame();
            }
        }, filter);
    }

    private void loadGame() {
        retrofit2.Call<GameInvite.Game> call = ApiFactory.getApiService().getGame(mGameId);
        call.enqueue(new retrofit2.Callback<GameInvite.Game>() {
            @Override
            public void onResponse(retrofit2.Call<GameInvite.Game> call,
                                   retrofit2.Response<GameInvite.Game> response) {
                if (!response.isSuccessful()) finish();

                GameInvite.Game game = response.body();

                if (mGame != game) {
                    mGame = game;

                    Util.Log("author = " + new Gson().toJson(mGame));
                    initGameDetails(game);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GameInvite.Game> call, Throwable t) {
            }
        });
    }

    private void initBoardLayout() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mBoard.getLayoutParams().width = size.x;
        mBoard.requestFocus();
    }

    private void initPaidPlayers() {
        Util.Log("get paid players");
        mPaidPlayers.clear();

        if (mGame.getPaidPlayers() != null) {
            for (PaidPlayer player : mGame.getPaidPlayers()) {
                if (player.paid()) {
                    mPaidPlayers.add(player.getUserId());
                    Util.Log("paid player");
                }
            }
        }
        try {
            mPaidPlayersAdapter.setPaidPlayers(mPaidPlayers);
            mPaidPlayersAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            // Dialog is not shown yet
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void initScaleLayout() {
        mLayout.setOnTouchListener(new StandardGestures(this, mBoardAdapter, mRowAdapter,
                mColumnAdapter, mRowRecycler, mColumnRecycler, mBoardRecycler));

        int height = Util.getCellSize();

        mColumnRecycler.getLayoutParams().width = height;

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
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void initGameDetails(GameInvite.Game game) {
        if (game == null) return;

        if (mIgnoreUpdate) {
            mIgnoreUpdate = false;
            return;
        }

        mGameIdTxt.setText(game.getId());

        // Set game image
        Picasso.with(this).load(game.getImage()).centerCrop().fit()
                .placeholder(R.drawable.placeholder).into(mGameImg);

        // Set game name
        mGameName = game.getName();
        mTitleTxt.setText(mGameName);
        mInfoTitleTxt.setText(mGameName);

        // Update row numbers
        mRowNumbers.clear();
        mRowNumbers.addAll(game.getRowNumbers());
        mRowAdapter.notifyDataSetChanged();

        // Update column numbers
        mColumnNumbers.clear();
        mColumnNumbers.addAll(game.getColumnNumbers());
        mColumnAdapter.notifyDataSetChanged();

        mPlayersLayout.removeAllViews();

        if (game.getQuarter1Winner() != null
                || game.getQuarter2Winner() != null
                || game.getQuarter3Winner() != null
                || game.getFinalWinner() != null) {
            mScoresLayout.setVisibility(View.VISIBLE);
            mScoreQ1Title.setText(parseNameAbbr(game.getQuarter1Winner().getPlayer().getName()));
            mScoreQ1Desc.setText(game.getQuarter1Price() + " pts");
            if (game.getQuarter2Winner() != null) {
                mScoreQ2Title.setText(parseNameAbbr(game.getQuarter2Winner().getPlayer().getName()));
                mScoreQ2Desc.setText(game.getQuarter2Price() + " pts");
            }
            if (game.getQuarter3Winner() != null) {
                mScoreQ3Title.setText(parseNameAbbr(game.getQuarter3Winner().getPlayer().getName()));
                mScoreQ3Desc.setText(game.getQuarter3Price() + " pts");
            }
            if (game.getFinalWinner() != null) {
                mScoreFinalTitle.setText(parseNameAbbr(game.getFinalWinner().getPlayer().getName()));
                mScoreFinalDesc.setText(game.getFinalPrice() + " pts");
            }
        } else {
            mScoresLayout.setVisibility(View.GONE);
        }

        View playerLayout = LayoutInflater.from(this)
                .inflate(R.layout.item_player_avatar, null);
        TextView playerName = playerLayout.findViewById(R.id.player_name);
        ImageView playerImage = playerLayout.findViewById(R.id.player_photo);
        if (TextUtils.isEmpty(game.getAuthor().getPhoto())) {
            playerImage.setVisibility(View.GONE);
        } else {
            playerImage.setVisibility(View.VISIBLE);
            Picasso.with(this).load(game.getAuthor().getPhoto()).into(playerImage);
        }
        playerName.setText(parseNameAbbr(game.getAuthor().getName()));
        mPlayersLayout.addView(playerLayout);
        for (Player player : game.getPlayers()) {
            playerLayout = LayoutInflater.from(this)
                    .inflate(R.layout.item_player_avatar, null);
            playerName = playerLayout.findViewById(R.id.player_name);
            playerImage = playerLayout.findViewById(R.id.player_photo);
            if (TextUtils.isEmpty(player.getPhoto())) {
                playerImage.setVisibility(View.GONE);
            } else {
                playerImage.setVisibility(View.VISIBLE);
                Picasso.with(this).load(player.getPhoto()).into(playerImage);
            }
            playerName.setText(parseNameAbbr(player.getName()));
            mPlayersLayout.addView(playerLayout);
        }

        mPdfQ1Txt.setText(String.valueOf(game.getQuarter1Price()));
        mPdfQ2Txt.setText(String.valueOf(game.getQuarter2Price()));
        mPdfQ3Txt.setText(String.valueOf(game.getQuarter3Price()));
        mPdfFinalTxt.setText(String.valueOf(game.getFinalPrice()));

        mBoardTotalScoreTxt.setText(NumberFormat.getNumberInstance(Locale.US)
                .format(game.getTotalPrice()) + " pts");
        mInfoScoreTxt.setText(NumberFormat.getNumberInstance(Locale.US)
                .format(game.getTotalPrice()) + " pts total");

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

        Util.Log("Init game details");
        int mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : selectedSquares) {
            if (selectedSquare.getPosition() != -1) {
                mSelectedSquares.add(selectedSquare);

                String playerId = Util.getCurrentPlayerId();
                if (TextUtils.isEmpty(playerId)) {
                    playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                if (selectedSquare.getAuthorId().equals(playerId)) {
                    mySelectedSquares++;
                }
            }
        }
        mBoardAdapter.refresh(mSelectedSquares);

        mBoardAdapter.setRowNumbers(mRowNumbers);
        mBoardAdapter.setColumnNumbers(mColumnNumbers);

        Util.Log("test");
        retrofit2.Call<Event> call = ApiFactory.getApiService().getEvent(game.getEventId());
        call.enqueue(new retrofit2.Callback<Event>() {
            @Override
            public void onResponse(retrofit2.Call<Event> call,
                                   retrofit2.Response<Event> response) {
                if (!response.isSuccessful()) return;

                Event event = response.body();
                if (event != null) {
                    mEvent = event;

                    updateLiveState();

                    try {
                        mHomeBg.setBackgroundColor(Color.parseColor(event.getTeamHome().getColor()));
                        mInfoHomeNameTxt.setTextColor(Color.parseColor(event.getTeamHome().getFont()));
                        mInfoHomeTotalScoreTxt.setTextColor(Color.parseColor(event.getTeamHome()
                                .getFont()));
                        mHomeNameTxt.setTextColor(Color.parseColor(event.getTeamHome()
                                .getColor()));
                    } catch (Exception e) {
                        mHomeBg.setBackgroundColor(Color.parseColor("#777777"));
                        mInfoHomeNameTxt.setTextColor(Color.parseColor("#FFFFFF"));
                        mInfoHomeTotalScoreTxt.setTextColor(Color.parseColor("#FFFFFF"));
                        mHomeNameTxt.setTextColor(Color.parseColor("#777777"));
                    }
                    try {
                        mAwayBg.setBackgroundColor(Color.parseColor(event.getTeamAway().getColor()));
                        mInfoAwayNameTxt.setTextColor(Color.parseColor(event.getTeamAway().getFont()));
                        mInfoAwayTotalScoreTxt.setTextColor(Color.parseColor(event.getTeamAway()
                                .getFont()));
                        mAwayNameTxt.setTextColor(Color.parseColor(event.getTeamAway()
                                .getColor()));
                    } catch (Exception e) {
                        mAwayBg.setBackgroundColor(Color.parseColor("#77777"));
                        mInfoAwayNameTxt.setTextColor(Color.parseColor("#FFFFFF"));
                        mInfoAwayTotalScoreTxt.setTextColor(Color.parseColor("#FFFFFF"));
                        mAwayNameTxt.setTextColor(Color.parseColor("#777777"));
                    }

                    mHomeQ1ScoreTxt.setText(event.getTeamHome().getScore().getQ1());
                    mHomeQ2ScoreTxt.setText(event.getTeamHome().getScore().getQ2());
                    mHomeQ3ScoreTxt.setText(event.getTeamHome().getScore().getQ3());
                    mHomeQ4ScoreTxt.setText(event.getTeamHome().getScore().getQ4());
                    mHomeFinalScoreTxt.setText(event.getTeamHome().getScore().getTotal());
                    mAwayQ1ScoreTxt.setText(event.getTeamAway().getScore().getQ1());
                    mAwayQ2ScoreTxt.setText(event.getTeamAway().getScore().getQ2());
                    mAwayQ3ScoreTxt.setText(event.getTeamAway().getScore().getQ3());
                    mAwayQ4ScoreTxt.setText(event.getTeamAway().getScore().getQ4());
                    mAwayFinalScoreTxt.setText(event.getTeamAway().getScore().getTotal());

                    mHomeQ1TitleTxt.setTextColor(ContextCompat.getColor
                            (GameBoardActivity.this, event.getTimeText().contains("Q1")
                                    ? R.color.color_green : android.R.color.white));
                    mHomeQ2TitleTxt.setTextColor(ContextCompat.getColor
                            (GameBoardActivity.this, event.getTimeText().contains("Q2")
                                    ? R.color.color_green : android.R.color.white));
                    mHomeQ3TitleTxt.setTextColor(ContextCompat.getColor
                            (GameBoardActivity.this, event.getTimeText().contains("Q3")
                                    ? R.color.color_green : android.R.color.white));
                    mHomeQ4TitleTxt.setTextColor(ContextCompat.getColor
                            (GameBoardActivity.this, event.getTimeText().contains("Q4")
                                    ? R.color.color_green : android.R.color.white));
                    mHomeFinalTitleTxt.setTextColor(ContextCompat.getColor
                            (GameBoardActivity.this, event.getTimeText().contains("Final")
                                    ? R.color.color_green : android.R.color.white));

                    try {
                        if (!TextUtils.isEmpty(event.getTeamAway().getScore().getTotal())) {
                            mBoardAdapter.setScore(Integer.parseInt(event.getTeamHome()
                                    .getScore().getTotal()), Integer.parseInt(event
                                    .getTeamAway().getScore().getTotal()));
                        } else if (!TextUtils.isEmpty(event.getTeamAway().getScore().getQ4())) {
                            mBoardAdapter.setScore(Integer.parseInt(event.getTeamHome()
                                    .getScore().getQ4()), Integer.parseInt(event
                                    .getTeamAway().getScore().getQ4()));
                        } else if (!TextUtils.isEmpty(event.getTeamAway().getScore().getQ3())) {
                            mBoardAdapter.setScore(Integer.parseInt(event.getTeamHome()
                                    .getScore().getQ3()), Integer.parseInt(event
                                    .getTeamAway().getScore().getQ3()));
                        } else if (!TextUtils.isEmpty(event.getTeamAway().getScore().getQ2())) {
                            mBoardAdapter.setScore(Integer.parseInt(event.getTeamHome()
                                    .getScore().getQ2()), Integer.parseInt(event
                                    .getTeamAway().getScore().getQ2()));
                        } else if (!TextUtils.isEmpty(event.getTeamAway().getScore().getQ1())) {
                            mBoardAdapter.setScore(Integer.parseInt(event.getTeamHome()
                                    .getScore().getQ1()), Integer.parseInt(event
                                    .getTeamAway().getScore().getQ1()));
                        }
                    } catch (Exception e) {
                        Util.Log("Can't set scores: " + e);
                    }
                    mColumnAdapter.notifyDataSetChanged();
                    mRowAdapter.notifyDataSetChanged();
                    mBoardAdapter.notifyDataSetChanged();

                    mTimeTxt.setText(event.getTimeText());

                    mAwayNameTxt.setText(event.getTeamAway().getName());
                    mHomeNameTxt.setText(event.getTeamHome().getName());

                    // Init bottom section info
                    mInfoAwayNameTxt.setText(event.getTeamAway().getName() + " "
                            + event.getTeamAway().getAbbrev());
                    mInfoHomeNameTxt.setText(event.getTeamHome().getName() + " "
                            + event.getTeamHome().getAbbrev());
                    mQuarterHomeTxt.setText(event.getTeamAway().getAbbrev());
                    mQuarterAwayTxt.setText(event.getTeamHome().getAbbrev());

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
            public void onFailure(retrofit2.Call<Event> call, Throwable t) {
            }
        });

        mRulesTxt.setText(TextUtils.isEmpty(game.getRules()) ? getString(R.string.rules_default)
                : getString(R.string.rules_default) + "\n\n" + game.getRules());

        mBoardAdapter.setCustom(mGame.isCustom());
        mBoardAdapter.notifyDataSetChanged();

        initPlayers();
        initPaidPlayers();
    }

    public PopupWindow popupDisplay() {
        popupWindow = new PopupWindow(this);

        // inflate your layout or dynamically add view
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view;
        if (!mGameLive) {
            view = inflater.inflate(R.layout.popup_window, null);

            view.findViewById(R.id.player_details).setOnClickListener(menuClickListener);
            view.findViewById(R.id.list_players).setOnClickListener(menuClickListener);
            view.findViewById(R.id.paid_players).setOnClickListener(menuClickListener);
            view.findViewById(R.id.create_pdf).setOnClickListener(menuClickListener);
            view.findViewById(R.id.game_information).setOnClickListener(menuClickListener);
        } else {
            view = inflater.inflate(R.layout.popup_window_closed, null);

            view.findViewById(R.id.list_players).setOnClickListener(menuClickListener);
            view.findViewById(R.id.create_pdf).setOnClickListener(menuClickListener);
            view.findViewById(R.id.game_information).setOnClickListener(menuClickListener);
            view.findViewById(R.id.paid_players).setOnClickListener(menuClickListener);
        }
        if (view.findViewById(R.id.paid_players) != null) {
            view.findViewById(R.id.paid_players).setVisibility(mIsHost ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.paid_players_divider).setVisibility(mIsHost ? View.VISIBLE : View.GONE);
        }

        if (view.findViewById(R.id.player_details) != null) {
            view.findViewById(R.id.player_details).setVisibility(mIsHost ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.player_details_divider).setVisibility(mIsHost ? View.VISIBLE : View.GONE);
        }

        if (view.findViewById(R.id.list_players_title) != null) {
            ((TextView) view.findViewById(R.id.list_players_title)).setText(mGameLive ? R.string.list_of_players : R.string.invite_friends);
        }

        ((TextView) view.findViewById(R.id.game_information_txt)).setText
                (mGameInfoLayout.getVisibility() == View.GONE
                        ? "Game Information" : "Close Game Information");

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);

        return popupWindow;
    }

    private String parseNameAbbr(String name) {
        try {
            String[] parts = name.split(" ");
            StringBuilder abbr = new StringBuilder();
            for (String part : parts) {
                if (abbr.length() == 2) break;
                abbr.append(part.substring(0, 1));
            }
            return abbr.toString();
        } catch (Exception e) {
            try {
                return name.substring(0, 2).toLowerCase(Locale.US);
            } catch (Exception e2) {
                return name;
            }
        }
    }

    private void initPlayers() {
        mPlayers.clear();
        mAllPlayers.clear();
        mSelectedSquaresCount.clear();
        if (mAuthorId.equals(mMyId)) {
            mPlayers.add(new Player(mMyId, null, mMyEmail, mMyName, mMyPhoto));
            mAllPlayers.add(new Player(mMyId, null, mMyEmail, mMyName, mMyPhoto));
            int playerSquares = 0;
            for (SelectedSquare selectedSquare : mSelectedSquares) {
                String playerId = mMyId;
                if (selectedSquare.getAuthorId().equals(playerId)) {
                    playerSquares++;
                }
            }
            mSelectedSquaresCount.add(playerSquares);
        }
        for (Player player : mGame.getPlayers()) {
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

            mAllPlayers.add(player);
        }
        if (mPlayersAdapter != null) {
            mPlayersAdapter.notifyDataSetChanged();
        }

        final int playersSize = mPlayers.size() - 1;
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

    @SuppressWarnings("deprecation")
    private void showRulesDialog(GameInvite.Game game) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameBoardActivity.this);
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
        mMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow popupwindow_obj = popupDisplay();
                popupwindow_obj.showAsDropDown(mMenuBtn, 0, -10);
            }
        });
    }

    private void initRowRecycler() {
        mRowLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mRowRecycler.setLayoutManager(mRowLayoutManager);
        mRowRecycler.setHasFixedSize(true);
        mRowAdapter = new GameRowAdapter(this, mRowNumbers, true);
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
        mColumnAdapter = new GameRowAdapter(this, mColumnNumbers, false);
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
        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);
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

    @Override
    public void onBackPressed() {
        if (mGameInfoLayout.getVisibility() == View.VISIBLE) {
            onGameInformationButtonClicked();
        } else {
            super.onBackPressed();
        }
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

                mColumnRecycler.getLayoutParams().width = ViewPager.LayoutParams.MATCH_PARENT;
                mRowRecycler.getLayoutParams().height = ViewPager.LayoutParams.MATCH_PARENT;

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
        if (position == -1) return;

        int mySelectedSquares = 0;
        for (SelectedSquare selectedSquare : mSelectedSquares) {
            if (selectedSquare.getPosition() != -1) {
                String playerId = Util.getCurrentPlayerId();
                if (TextUtils.isEmpty(playerId)) {
                    playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                if (selectedSquare.getAuthorId().equals(playerId)) {
                    mySelectedSquares++;
                }
            }
        }

        int squaresLimit;
        try {
            squaresLimit = mGame.getSquaresLimit();
        } catch (Exception e) {
            squaresLimit = 100;
        }
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
    }

    public void onManualAddButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameBoardActivity.this,
                R.style.MaterialDialog);
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
                    if (mGame == null) return;
                    ArrayList<Player> players = mGame.getPlayers();
                    if (players == null) players = new ArrayList<>();

                    players.add(new Player(Util.generatePlayerId(), mDeviceOwnerId,
                            null, editTxt.getText().toString(), null));

                    mGame.setPlayers(players);

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
                    editTxt.setText("");

                    initGameDetails(mGame);
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
        mPaidPlayersAdapter = new GamePaidPlayersAdapter(this, mAllPlayers);
        mPaidPlayersAdapter.setPaidPlayers(mPaidPlayers);
        recycler.setAdapter(mPaidPlayersAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        dialogBuilder.setView(recycler);
        dialogBuilder.setNegativeButton("Close", null);
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                updateGameOnServer();
            }
        });
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
                    if (TextUtils.equals(friendship.getUser1Id(), mMyId)
                            || TextUtils.equals(friendship.getUser2Id(), mMyId)) {
                        final String friendId = friendship.getUser1Id().equals(mMyId)
                                ? friendship.getUser2Id() : friendship.getUser1Id();

                        try {
                            if (!TextUtils.isEmpty(friendId)) {
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
                                                                                try {
                                                                                    if (gameInvite.getGame().getId().equals(mGameId)) {
                                                                                        mInvitedFriendIds.add(friend.getId());
                                                                                        if (mFriendsAdapter != null) {
                                                                                            mFriendsAdapter.notifyDataSetChanged();
                                                                                        }
                                                                                    }
                                                                                } catch (Exception e) {
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
                        } catch (Exception e) {
                        }
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

    View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popupWindow.dismiss();

            if (v.getId() == R.id.create_pdf) {
                onPdfButtonClicked(null);
            } else if (v.getId() == R.id.player_details) {
                onManualAddButtonClicked(null);
            } else if (v.getId() == R.id.list_players) {
                onInviteFriendsButtonClicked(null);
            } else if (v.getId() == R.id.paid_players) {
                onPaidPlayersButtonClicked(null);
            } else if (v.getId() == R.id.game_information) {
                onGameInformationButtonClicked();
            } else {
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

                uploadSquares();
            }
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_pdf) {
            onPdfButtonClicked(null);
        } else if (item.getItemId() == R.id.player_details) {
            onManualAddButtonClicked(null);
        } else if (item.getItemId() == R.id.list_players) {
            onInviteFriendsButtonClicked(null);
        } else if (item.getItemId() == R.id.paid_players) {
            onPaidPlayersButtonClicked(null);
        } else if (item.getItemId() == R.id.game_information) {
            onGameInformationButtonClicked();
        } else {
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

            uploadSquares();
        }
        return super.onContextItemSelected(item);
    }

    private void onGameInformationButtonClicked() {
        if (mGameInfoLayout.getVisibility() == View.GONE) {
            mGameInfoLayout.setVisibility(View.VISIBLE);
            mLayout.setVisibility(View.GONE);
        } else {
            mGameInfoLayout.setVisibility(View.GONE);
            mLayout.setVisibility(View.VISIBLE);
        }
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

        if (mSelectedSquares == null) {
            mSelectedSquares = new ArrayList<>();
        }
        if (mGame != null) {
            mGame.setSelectedSquares(mSelectedSquares);
            updateGameOnServer();
        }
    }

    private void updateGameOnServer() {
        if (mPaidPlayersCall != null) {
            mPaidPlayersCall.cancel();
            mPaidPlayersCall = null;
        }

        mPaidPlayersCall = ApiFactory.getApiService().updateGame(mGame);
        mPaidPlayersCall.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call,
                                   retrofit2.Response<Void> response) {
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });
    }

    private void updateLiveState() {
        try {
            Util.Log("Selected squares size = " + mSelectedSquares.size());
            if (mSelectedSquares.size() == 100 != mGameLive || mGame.allowIncomplete()) {
                mGameLive = mSelectedSquares.size() == 100
                        || (mEvent.getTime() == -1 && mGame.allowIncomplete())
                        || mEvent.getTime() == -2;
                mBoardAdapter.setLive(mGameLive);
                mRowAdapter.setLive(mGameLive);
                mColumnAdapter.setLive(mGameLive);
                mBoardAdapter.notifyDataSetChanged();
                mRowAdapter.notifyDataSetChanged();
                mColumnAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Util.Log("Invalid game object: " + e);
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

    public void setPlayerPaid(final String playerId, final boolean checked, int adapterPosition) {
        ArrayList<PaidPlayer> paidPlayers = mGame.getPaidPlayers();
        if (paidPlayers == null) paidPlayers = new ArrayList<>();

        boolean foundPaidPlayer = false;
        for (PaidPlayer paidPlayer : paidPlayers) {
            if (paidPlayer.getUserId().equals(playerId)) {
                paidPlayer.setPaid(checked);
                foundPaidPlayer = true;
                break;
            }
        }

        if (!foundPaidPlayer) {
            paidPlayers.add(new PaidPlayer(playerId, checked));
        }
        mGame.setPaidPlayers(paidPlayers);
        initPaidPlayers();
    }

    public void onMenuButtonClicked(View view) {
        openContextMenu(view);
    }

    public void onScoresPressed(View view) {
        if (pager.getCurrentItem() == 0) {
            pager.setCurrentItem(1);
        } else {
            pager.setCurrentItem(0);
        }
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
        public void destroyItem(View arg0, int arg1, Object arg2) {
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

    class ScoresPagerAdapter extends PagerAdapter {

        public Object instantiateItem(ViewGroup collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.page_scores_q1;
                    break;
                case 1:
                    resId = R.id.page_scores_q2;
                    break;
                case 2:
                    resId = R.id.page_scores_q3;
                    break;
                case 3:
                    resId = R.id.page_scores_final;
                    break;
            }
            return findViewById(resId);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Q1";
            } else if (position == 1) {
                return "Q2";
            } else if (position == 2) {
                return "Q3";
            } else {
                return "FINAL";
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }
    }
}
