package com.moyersoftware.contender.menu.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.data.Game;
import com.moyersoftware.contender.menu.GamesFragment;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    private GamesFragment mFragment;
    private ArrayList<Game> mGames;

    public GamesAdapter(GamesFragment fragment, ArrayList<Game> games) {
        mFragment = fragment;
        mGames = games;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Game game = mGames.get(position);

        holder.nameTxt.setText(game.getName());
        holder.timeTxt.setText(Util.formatDate(game.getTime()));
        holder.scoreTxt.setText(mFragment.getResources().getString(R.string.games_score,
                game.getSelectedSquares() != null ? game.getSelectedSquares().size() : 0));
        Picasso.with(mFragment.getActivity()).load(game.getImage()).placeholder
                (android.R.color.white).centerCrop().fit().placeholder(R.drawable.placeholder)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.game_img)
        ImageView img;
        @Bind(R.id.game_name_txt)
        TextView nameTxt;
        @Bind(R.id.game_time_txt)
        TextView timeTxt;
        @Bind(R.id.game_score_txt)
        TextView scoreTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mFragment.joinGame(mGames.get(getAdapterPosition()).getId());
        }
    }
}