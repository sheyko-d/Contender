package com.moyersoftware.contender.game;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.GameBoardAdapter;
import com.moyersoftware.contender.game.adapter.GameRowAdapter;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameBoardActivity extends AppCompatActivity {

    // Constants
    private final static int COLUMNS_COUNT = 10;

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
    @Bind(R.id.board_team1_img)
    ImageView mTeam1Img;
    @Bind(R.id.board_team2_img)
    ImageView mTeam2Img;

    // Usual variables
    private int mTotalScrollY;
    private LinearLayoutManager mColumnLayoutManager;
    private LinearLayoutManager mRowLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);
        ButterKnife.bind(this);

        initGameImage();
        initBoardRecycler();
        initRowRecycler();
        initColumnRecycler();
        initHorizontalScrollView();
        initBottomSheet();
    }

    private void initGameImage() {
        Picasso.with(this).load("http://womensenews.org/files/NFL-football.jpg").into(mGameImg);
    }

    private void initBoardRecycler() {
        mBoardRecycler.setLayoutManager(new GridLayoutManager(this, COLUMNS_COUNT));
        mBoardRecycler.setHasFixedSize(true);
        mBoardRecycler.setAdapter(new GameBoardAdapter(this));
        mBoardRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mTotalScrollY += dy;

                mColumnLayoutManager.scrollToPositionWithOffset(0, -mTotalScrollY);
            }
        });
    }

    private void initRowRecycler() {
        mRowLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mRowRecycler.setLayoutManager(mRowLayoutManager);
        mRowRecycler.setHasFixedSize(true);
        mRowRecycler.setAdapter(new GameRowAdapter(this));
    }

    private void initColumnRecycler() {
        mColumnLayoutManager = new LinearLayoutManager(this);
        mColumnRecycler.setLayoutManager(mColumnLayoutManager);
        mColumnRecycler.setHasFixedSize(true);
        mColumnRecycler.setAdapter(new GameRowAdapter(this));
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

        Picasso.with(this).load("http://a2.espncdn.com/combiner/i?img=%2Fi%2Fteamlogos%2Fnfl%2F500%2Fne.png")
                .into(mTeam1Img);
        Picasso.with(this).load("http://content.sportslogos.net/logos/7/166/full/919.gif")
                .into(mTeam2Img);
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
}
