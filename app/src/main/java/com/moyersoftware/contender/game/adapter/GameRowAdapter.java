package com.moyersoftware.contender.game.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moyersoftware.contender.R;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class GameRowAdapter extends RecyclerView.Adapter<GameRowAdapter.ViewHolder> {

    // Usual variables
    private ArrayList<Integer> mNumbers;
    private Boolean mLive = false;

    public GameRowAdapter(ArrayList<Integer> numbers) {
        mNumbers = numbers;
    }

    public void setLive(Boolean live){
        mLive = live;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mLive){
            ((TextView) holder.itemView).setText(String.valueOf(mNumbers.get(position)));
        } else {
            ((TextView) holder.itemView).setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return mNumbers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}