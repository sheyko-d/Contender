package com.moyersoftware.contender.game.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class GameRowAdapter extends RecyclerView.Adapter<GameRowAdapter.ViewHolder> {

    // Usual variables
    private ArrayList<Integer> mNumbers;
    private Boolean mLive = false;
    private int mHeight;

    public GameRowAdapter(Context context, ArrayList<Integer> numbers) {
        mNumbers = numbers;
        mHeight = Util.getCellSize();
    }

    public void setLive(Boolean live) {
        mLive = live;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.getLayoutParams().height = mHeight;
        holder.itemView.getLayoutParams().width = mHeight;

        if (mLive) {
            ((TextView) holder.itemView).setText(String.valueOf(mNumbers.get(position)));
        } else {
            ((TextView) holder.itemView).setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return mNumbers.size();
    }

    public void scale(int height) {
        mHeight = height;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}