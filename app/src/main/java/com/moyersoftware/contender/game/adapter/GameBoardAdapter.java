package com.moyersoftware.contender.game.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moyersoftware.contender.R;

import butterknife.ButterKnife;

public class GameBoardAdapter extends RecyclerView.Adapter<GameBoardAdapter.ViewHolder> {

    // Constants
    private final static int CELLS_COUNT = 100;

    // Usual variables
    private Context mContext;

    public GameBoardAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_board, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return CELLS_COUNT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}