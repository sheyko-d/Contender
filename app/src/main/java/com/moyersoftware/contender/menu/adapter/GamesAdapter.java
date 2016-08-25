package com.moyersoftware.contender.menu.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.menu.GamesFragment;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    private MainActivity mActivity;
    private GamesFragment mFragment;
    private ArrayList<GameInvite.Game> mGames;
    private String mMyId;
    private ArrayList<Long> mGameTimes;

    public GamesAdapter(MainActivity activity, GamesFragment fragment,
                        ArrayList<GameInvite.Game> games, String myId) {
        mActivity = activity;
        mFragment = fragment;
        mGames = games;
        mMyId = myId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GameInvite.Game game = mGames.get(position);

        holder.nameTxt.setText(game.getName());

        try {
            if (mGameTimes != null) {
                if (mGameTimes.get(position) == -2) {
                    holder.finalLayout.setVisibility(View.VISIBLE);
                    holder.timeTxt.setVisibility(View.GONE);
                } else {
                    holder.timeTxt.setVisibility(View.VISIBLE);
                    holder.timeTxt.setText(Util.formatDateTime(mGameTimes.get(position)));
                    holder.finalLayout.setVisibility(View.GONE);

                    if (!TextUtils.isEmpty(game.getInviteName())) {
                        holder.timeTxt.setText(holder.timeTxt.getText() + "\n\uD83C\uDFC8 "
                                + game.getInviteName());
                    }
                }
            }
        } catch (Exception e) {
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

        boolean invite = !TextUtils.isEmpty(game.getInviteName());
        holder.accept.setVisibility(invite ? View.VISIBLE : View.GONE);
        holder.reject.setVisibility(invite ? View.VISIBLE : View.GONE);
        holder.scoreTxt.setVisibility(invite ? View.GONE : View.VISIBLE);
        holder.layout.setClickable(!invite);
        holder.itemView.setClickable(!invite);
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
        @Bind(R.id.game_final_layout)
        View finalLayout;
        @Bind(R.id.accept)
        View accept;
        @Bind(R.id.reject)
        View reject;
        @Bind(R.id.game_layout)
        View layout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            try {
                itemView.setClickable(!TextUtils.isEmpty(mGames.get(getAdapterPosition())
                        .getInviteName()));
            } catch (Exception e) {
                // Can't set item clickable
            }
            itemView.setOnLongClickListener(this);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.playGame(mGames.get(getAdapterPosition()).getId());
                    removeInvite();
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeInvite();
                }
            });
        }

        private void removeInvite() {
            Util.Log("remove invite: " + mGames.get(getAdapterPosition()).getInviteId());
            FirebaseDatabase.getInstance().getReference().child("game_invites")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(mGames.get(getAdapterPosition()).getInviteId()).removeValue();

            mFragment.initDatabase();
        }

        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(mGames.get(getAdapterPosition()).getInviteName())) {
                mFragment.joinGame(mGames.get(getAdapterPosition()).getId());
            } else {
                mActivity.playGame(mGames.get(getAdapterPosition()).getId());
                removeInvite();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (TextUtils.isEmpty(mGames.get(getAdapterPosition()).getInviteName())) {
                mFragment.deleteGame(mGames.get(getAdapterPosition()));
            }
            return true;
        }
    }
}