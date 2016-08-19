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
    private String mMyId;
    private ArrayList<Long> mGameTimes;

    public GamesAdapter(GamesFragment fragment, ArrayList<Game> games, String myId) {
        mFragment = fragment;
        mGames = games;
        mMyId = myId;
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
        if (mGameTimes != null) {
            holder.timeTxt.setText(Util.formatDateTime(mGameTimes.get(position)));
        }
        if (game.isCurrent()) {
            holder.scoreTxt.setText("Current");
        } else {
            holder.scoreTxt.setText(mFragment.getResources().getString(R.string.games_score,
                    game.getSelectedSquares() != null ? game.getSelectedSquares().size() : 0));
        }
        if (mMyId != null) {
            int totalWinnings = 0;
            if (game.getQuarter1Winner() != null && game.getQuarter1Winner().getPlayer().getUserId()
                    .equals(mMyId)) {
                totalWinnings += game.getQuarter1Price();
            }
            if (game.getQuarter2Winner() != null && game.getQuarter2Winner().getPlayer().getUserId()
                    .equals(mMyId)) {
                totalWinnings += game.getQuarter2Price();
            }
            if (game.getQuarter3Winner() != null && game.getQuarter3Winner().getPlayer().getUserId()
                    .equals(mMyId)) {
                totalWinnings += game.getQuarter3Price();
            }
            if (game.getFinalWinner() != null && game.getFinalWinner().getPlayer().getUserId()
                    .equals(mMyId)) {
                totalWinnings += game.getFinalPrice();
            }
            holder.winningsTxt.setVisibility(totalWinnings > 0 ? View.VISIBLE : View.GONE);
            holder.winningsTxt.setText(totalWinnings + " points");
        }

        holder.quarterTxt.setText(game.getCurrentQuarter() != null ? game.getCurrentQuarter() : "");
        Picasso.with(mFragment.getActivity()).load(game.getImage()).placeholder
                (android.R.color.white).centerCrop().fit().placeholder(R.drawable.placeholder)
                .into(holder.img);

        if (mGameTimes.get(position) == -2) {
            holder.finalTxt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public void updateGameTimes(ArrayList<Long> gameTimes) {
        mGameTimes = gameTimes;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @Bind(R.id.game_img)
        ImageView img;
        @Bind(R.id.game_name_txt)
        TextView nameTxt;
        @Bind(R.id.game_time_txt)
        TextView timeTxt;
        @Bind(R.id.game_score_txt)
        TextView scoreTxt;
        @Bind(R.id.game_quarter_txt)
        TextView quarterTxt;
        @Bind(R.id.game_winnings_txt)
        TextView winningsTxt;
        @Bind(R.id.game_final_txt)
        TextView finalTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mFragment.joinGame(mGames.get(getAdapterPosition()).getId());
        }

        @Override
        public boolean onLongClick(View view) {
            mFragment.deleteGame(mGames.get(getAdapterPosition()));
            return true;
        }
    }
}