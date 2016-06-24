package com.moyersoftware.contender.game.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moyersoftware.contender.R;

import butterknife.ButterKnife;

public class GameRowAdapter extends RecyclerView.Adapter<GameRowAdapter.ViewHolder> {

    // Constants
    private final static int ROW_COUNT = 10;

    // Usual variables
    private Context mContext;

    public GameRowAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((TextView)holder.itemView).setText((position+1)+"");
    }

    @Override
    public int getItemCount() {
        return ROW_COUNT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}