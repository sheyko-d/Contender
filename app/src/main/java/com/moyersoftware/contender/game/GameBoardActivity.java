package com.moyersoftware.contender.game;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.print.PrintHelper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
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
import com.moyersoftware.contender.game.adapter.GamePlayersAdapter;
import com.moyersoftware.contender.game.adapter.GameRowAdapter;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.Game;
import com.moyersoftware.contender.game.data.SelectedSquare;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

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
    View mLayout;
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
    @Bind(R.id.board_print_img)
    ImageView mPrintImg;
    @Bind(R.id.board_pdf_img)
    ImageView mPdfImg;
    @Bind(R.id.board_progress_txt)
    View mProgressBar;
    @Bind(R.id.board_invite_friends_img)
    ImageView mInviteFriendsImg;
    @Bind(R.id.board_info_q1_winner_img)
    ImageView mWinner1Img;
    @Bind(R.id.board_info_q2_winner_img)
    ImageView mWinner2Img;
    @Bind(R.id.board_info_q3_winner_img)
    ImageView mWinner3Img;
    @Bind(R.id.board_info_final_winner_img)
    ImageView mWinnerFinalImg;

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
    private GamePlayersAdapter mPlayersAdapter;
    private String mMyEmail;
    private String mDeviceOwnerId;
    private AlertDialog mInviteFriendsDialog;
    private ArrayList<Friend> mFriends = new ArrayList<>();
    private GameFriendsAdapter mFriendsAdapter;
    private ArrayList<String> mInvitedFriendIds = new ArrayList<>();
    private int mRemoveSquarePos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);
        ButterKnife.bind(this);

        mGameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        if (TextUtils.isEmpty(mGameId)) return;

        initUser();
        initRowRecycler();
        initColumnRecycler();
        initBoardRecycler();
        initHorizontalScrollView();
        initBottomSheet();
        initDatabase();
        loadPlayers();
        loadFriends();
    }

    private void loadPlayers() {
        mDatabase.child("games").child(mGameId).child("players").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPlayers.clear();
                        if (mAuthorId.equals(mMyId)) {
                            mPlayers.add(new Player(mMyId, null, mMyEmail, mMyName, mMyPhoto));
                        }
                        for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                            Player player = playerSnapshot.getValue(Player.class);
                            if (player.getCreatedByUserId() == null || player.getCreatedByUserId()
                                    .equals(mDeviceOwnerId)) {
                                mPlayers.add(player);
                            }
                        }
                        if (mPlayersAdapter != null) {
                            mPlayersAdapter.notifyDataSetChanged();
                        }
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
            mMyName = Util.getDisplayName();
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
                if (!dataSnapshot.exists()) {
                    Toast.makeText(GameBoardActivity.this, "Game not found", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    Game game = dataSnapshot.getValue(Game.class);

                    initGameDetails(game);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initGameDetails(Game game) {
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

        // Update winners
        mWinner1Img.setVisibility(game.getQuarter1Winner() == null ? View.INVISIBLE : View.VISIBLE);
        if (game.getQuarter1Winner() != null) {
            Picasso.with(this).load(game.getQuarter1Winner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinner1Img);
            if (!game.getQuarter1Winner().isConsumed()
                    && game.getQuarter1Winner().getPlayer().getUserId().equals(mMyId)) {
                showWinDialog(game.getQuarter1Price(), "1st", null);
                mDatabase.child("games").child(mGameId).child("quarter1Winner").child("consumed")
                        .setValue(true);
            } else if (!game.getQuarter1Winner().isConsumed() && game.getQuarter1Winner()
                    .getPlayer().getCreatedByUserId().equals(mDeviceOwnerId)) {
                showWinDialog(game.getQuarter1Price(), "1st", game.getQuarter1Winner().getPlayer()
                        .getName());
                mDatabase.child("games").child(mGameId).child("quarter1Winner").child("consumed")
                        .setValue(true);
            }
        }
        mWinner2Img.setVisibility(game.getQuarter2Winner() == null ? View.INVISIBLE : View.VISIBLE);
        if (game.getQuarter2Winner() != null) {
            Picasso.with(this).load(game.getQuarter2Winner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinner2Img);
        }
        mWinner3Img.setVisibility(game.getQuarter3Winner() == null ? View.INVISIBLE : View.VISIBLE);
        if (game.getQuarter3Winner() != null) {
            Picasso.with(this).load(game.getQuarter3Winner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinner3Img);
        }
        mWinnerFinalImg.setVisibility(game.getFinalWinner() == null ? View.INVISIBLE
                : View.VISIBLE);
        if (game.getFinalWinner() != null) {
            Picasso.with(this).load(game.getFinalWinner().getPlayer().getPhoto()).fit()
                    .placeholder(R.drawable.avatar_placeholder).into(mWinnerFinalImg);
        }

        // Get players
        mPlayerEmails.clear();
        mPlayerEmails.add(game.getAuthor().getEmail());
        mAuthorId = game.getAuthor().getUserId();
        if (game.getPlayers() != null) {
            for (Player player : game.getPlayers()) {
                mPlayerEmails.add(player.getEmail());
            }
        }

        // Update selected squares
        mSelectedSquares.clear();
        ArrayList<SelectedSquare> selectedSquares = game.getSelectedSquares();
        if (selectedSquares == null) selectedSquares = new ArrayList<>();

        for (SelectedSquare selectedSquare : selectedSquares) {
            mSelectedSquares.add(selectedSquare);
        }
        mBoardAdapter.refresh(mSelectedSquares);

        updateLiveState();

        mDatabase.child("events").child(game.getEventId()).addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Event event = dataSnapshot.getValue(Event.class);
                        if (event != null) {
                            mGameLive = (event.getTime() != -1 && System.currentTimeMillis()
                                    > event.getTime()) || mSelectedSquares.size() == 100;
                            mColumnAdapter.setLive(mGameLive);
                            mRowAdapter.setLive(mGameLive);
                            mBoardAdapter.setLive(mGameLive);
                            mColumnAdapter.notifyDataSetChanged();
                            mRowAdapter.notifyDataSetChanged();
                            mBoardAdapter.notifyDataSetChanged();

                            mTimeTxt.setText(event.getTimeText());

                            mAwayNameTxt.setText(event.getTeamAway().getName());
                            mHomeNameTxt.setText(event.getTeamHome().getName());

                            // Init bottom section info
                            mInfoAwayNameTxt.setText(event.getTeamAway().getName());
                            mInfoHomeNameTxt.setText(event.getTeamHome().getName());

                            Picasso.with(GameBoardActivity.this).load(event.getTeamAway()
                                    .getImage()).into(mTeam1Img);
                            Picasso.with(GameBoardActivity.this).load(event.getTeamHome()
                                    .getImage()).into(mTeam2Img);

                            if (event.getTeamAway().getScore() != null) {
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
    }

    private void showWinDialog(int price, String quarter, String name) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
            dialogBuilder.setTitle("\uD83C\uDFC6  Congratulations");
            if (TextUtils.isEmpty(name)) {
                dialogBuilder.setMessage("You won " + price + " points in the " + quarter
                        + " quarter");
            } else {
                dialogBuilder.setMessage(name + " won " + price + " points in the " + quarter
                        + " quarter");
            }
            dialogBuilder.setNegativeButton("OK", null);
            dialogBuilder.create().show();
        } catch (Exception e) {
            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notif)
                            .setContentTitle("Congratulations!");
            if (TextUtils.isEmpty(name)) {
                mBuilder.setContentText("You won " + price + " points in the " + quarter
                        + " quarter");
            } else {
                mBuilder.setContentText(name + " won " + price + " points in the " + quarter
                        + " quarter");
            }
            Intent resultIntent = new Intent(this, GameBoardActivity.class)
                    .putExtra(EXTRA_GAME_ID, mGameId);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);// Sets an ID for the notification
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = mBuilder.build();
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.defaults |= Notification.DEFAULT_SOUND;
            // Builds the notification and issues it.
            mNotifyMgr.notify(1, notification);
        }
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
        mRowAdapter = new GameRowAdapter(mRowNumbers);
        mRowRecycler.setAdapter(mRowAdapter);
    }

    private void initColumnRecycler() {
        mColumnLayoutManager = new LinearLayoutManager(this);
        mColumnRecycler.setLayoutManager(mColumnLayoutManager);
        mColumnRecycler.setHasFixedSize(true);
        mColumnAdapter = new GameRowAdapter(mColumnNumbers);
        mColumnRecycler.setAdapter(mColumnAdapter);
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
        BottomSheetBehavior behavior = BottomSheetBehavior.from(mBottomSheet);
        behavior.setPeekHeight(Util.convertDpToPixel(48));
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
        mBoardRecycler.post(new Runnable() {
            @Override
            public void run() {
                makePdf();
            }
        });
    }

    private void makePdf() {
        int boardSize = (int) (getResources().getDimension(R.dimen.board_cell_size) * 11
                + Util.convertDpToPixel(32));
        mLayout.getLayoutParams().width = boardSize;
        mLayout.getLayoutParams().height = boardSize;
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
                startActivity(Intent.createChooser(intent, "Send email..."));

                mLayout.getLayoutParams().width = ViewPager.LayoutParams.MATCH_PARENT;
                mLayout.getLayoutParams().height = ViewPager.LayoutParams.MATCH_PARENT;
                mLayout.requestLayout();
                mBoardAdapter.setPrintMode(false);

                mProgressBar.setVisibility(View.GONE);
                mLayout.setVisibility(View.VISIBLE);
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

            if (mPendingUpload) {
                mHandler.removeCallbacks(updateSquaresRunnable);
            }
            mPendingUpload = true;
            mHandler.postDelayed(updateSquaresRunnable, 500);
        }

        updateLiveState();
    }

    Runnable updateSquaresRunnable = new Runnable() {
        @Override
        public void run() {
            //noinspection unchecked
            AsyncTaskCompat.executeParallel(new UpdateSquareTask());
        }
    };

    public void onAddPlayerButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle("Select player");
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_player, null);
        RecyclerView recycler = (RecyclerView) dialogView.findViewById
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
        mMyEmail = player.getEmail();
        mMyName = player.getName();
        mMyPhoto = player.getPhoto();
        mPlayersAdapter.setCurrentPlayerId(mMyId);
    }

    public void onInviteFriendsButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle("Invite friends");
        @SuppressLint("InflateParams")
        RecyclerView recycler = (RecyclerView) LayoutInflater.from(this).inflate
                (R.layout.dialog_invite_friends, null);
        mFriendsAdapter = new GameFriendsAdapter(this, mFriends, mInvitedFriendIds);
        recycler.setAdapter(mFriendsAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        dialogBuilder.setView(recycler);
        dialogBuilder.setNegativeButton("Close", null);
        mInviteFriendsDialog = dialogBuilder.create();
        mInviteFriendsDialog.show();
    }

    private void loadFriends() {
        mDatabase.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFriends.clear();
                for (DataSnapshot friendshipSnapshot : dataSnapshot.getChildren()) {
                    final Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                    if (friendship.getUser1Id().equals(mMyId)
                            || friendship.getUser2Id().equals(mMyId)) {
                        String friendId = friendship.getUser1Id().equals(mMyId)
                                ? friendship.getUser2Id() : friendship.getUser1Id();

                        mDatabase.child("users").child(friendId).addListenerForSingleValueEvent
                                (new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);

                                        if (!friendship.isPending()) {
                                            mFriends.add(new Friend(dataSnapshot.getKey(),
                                                    user.getName(), user.getUsername(),
                                                    user.getImage(), user.getEmail(), false));
                                            if (mFriendsAdapter != null) {
                                                mFriendsAdapter.notifyDataSetChanged();
                                            }
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

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", friend.getEmail(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Join the Contender game!");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "moyersoftware.com/contender#"
                + mGameId);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
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
        for (SelectedSquare selectedSquare:mSelectedSquares){
            if (selectedSquare.getPosition()==mRemoveSquarePos){
                mSelectedSquares.remove(selectedSquare);
                break;
            }
        }

        mBoardAdapter.refresh(mSelectedSquares, mRemoveSquarePos);

        if (mPendingUpload) {
            mHandler.removeCallbacks(updateSquaresRunnable);
        }
        mPendingUpload = true;
        mHandler.postDelayed(updateSquaresRunnable, 500);
        return super.onContextItemSelected(item);
    }

    private class UpdateSquareTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected final Void doInBackground(Void... params) {
            mIgnoreUpdate = true;
            mDatabase.child("games").child(mGameId).child("selectedSquares")
                    .setValue(mSelectedSquares);
            mPendingUpload = false;
            return null;
        }

    }

    private void updateLiveState() {
        if (mSelectedSquares.size() == 100 != mGameLive) {
            mGameLive = mSelectedSquares.size() == 100;
            mPrintImg.setVisibility(mGameLive ? View.VISIBLE : View.GONE);
            mPdfImg.setVisibility(mGameLive && mAuthorId.equals(mMyId) ? View.VISIBLE : View.GONE);
            mBoardAdapter.setLive(mGameLive);
            mRowAdapter.setLive(mGameLive);
            mColumnAdapter.setLive(mGameLive);
            mBoardAdapter.notifyDataSetChanged();
            mRowAdapter.notifyDataSetChanged();
            mColumnAdapter.notifyDataSetChanged();

            mInviteFriendsImg.setVisibility(mGameLive && mAuthorId.equals(mMyId) ? View.VISIBLE
                    : View.GONE);
        }
    }
}
