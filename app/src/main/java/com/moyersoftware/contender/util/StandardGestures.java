package com.moyersoftware.contender.util;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.GameBoardAdapter;
import com.moyersoftware.contender.game.adapter.GameRowAdapter;

public class StandardGestures implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private final GameBoardAdapter mBoardsAdapter;
    private final GameRowAdapter mRowAdapter;
    private final GameRowAdapter mColumnAdapter;
    private final RecyclerView mRowRecycler;
    private final RecyclerView mColumnRecycler;
    private final RecyclerView mBoardRecycler;
    private final Context mContext;
    private View view;
    private ScaleGestureDetector gestureScale;
    private float scaleFactor = 1;
    private boolean inScale;
    private int mDefaultHeight;

    public StandardGestures(Context context, GameBoardAdapter boardAdapter,
                            GameRowAdapter rowAdapter, GameRowAdapter columnAdapter,
                            RecyclerView rowRecycler, RecyclerView columnRecycler,
                            RecyclerView boardRecycler) {
        mContext = context;
        gestureScale = new ScaleGestureDetector(context, this);
        mBoardsAdapter = boardAdapter;
        mRowAdapter = rowAdapter;
        mColumnAdapter = columnAdapter;
        mRowRecycler = rowRecycler;
        mColumnRecycler = columnRecycler;
        mBoardRecycler = boardRecycler;

        mDefaultHeight = (int) context.getResources().getDimension(R.dimen.board_cell_size);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.view = view;
        gestureScale.onTouchEvent(event);
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();

        int height = (int) (mDefaultHeight * scaleFactor);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenSize;
        if (size.x < size.y) {
            screenSize = size.x;
        } else {
            screenSize = size.y;
        }
        int minHeight = (screenSize - Util.convertDpToPixel(31)) / 11;
        if (height < minHeight) {
            height = minHeight;
        }
        int maxHeight = Util.convertDpToPixel(100);
        if (height > maxHeight) {
            height = maxHeight;
        }

        Util.setCellSize(height);

        mBoardsAdapter.scale(height);
        mColumnAdapter.scale(height);
        mRowAdapter.scale(height);
        mBoardsAdapter.notifyDataSetChanged();
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

        mBoardRecycler.setPadding(height, 0, 0, 0);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        inScale = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        inScale = false;
    }
}