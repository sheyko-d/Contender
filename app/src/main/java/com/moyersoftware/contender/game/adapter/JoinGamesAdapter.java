package com.moyersoftware.contender.game.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.JoinActivity;
import com.moyersoftware.contender.game.data.Game;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JoinGamesAdapter extends RecyclerView.Adapter<JoinGamesAdapter.ViewHolder> {

    private String mMyId;
    private String mMyEmail;
    private String mMyName;
    private String mMyPhoto;
    private JoinActivity mActivity;
    private ArrayList<Game> mGames;

    public JoinGamesAdapter(JoinActivity activity, ArrayList<Game> games, String myId,
                            String myEmail, String myName, String myPhoto) {
        mActivity = activity;
        mGames = games;
        mMyId = myId;
        mMyEmail = myEmail;
        mMyName = myName;
        mMyPhoto = myPhoto;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_join, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Game game = mGames.get(position);

        holder.nameTxt.setText(game.getName());
        holder.authorTxt.setText(mActivity.getString(R.string.join_author_txt, Util.parseUsername
                (game.getAuthor().getEmail())));
        Picasso.with(mActivity).load(game.getImage()).placeholder(R.drawable.placeholder)
                .centerCrop().fit().into(holder.img);

        if (game.getPlayers() != null && game.getPlayers().contains(new Player(mMyId, null,
                mMyEmail, mMyName, mMyPhoto))) {
            holder.joinBtn.setText(R.string.join_disabled_btn);
        } else {
            holder.joinBtn.setText(R.string.join_btn);
        }
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
        @Bind(R.id.game_author_txt)
        TextView authorTxt;
        @Bind(R.id.game_join_btn)
        Button joinBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            joinBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Game game = mGames.get(getAdapterPosition());
            // Check if user already joined this game before
            if (game.getPlayers() != null && game.getPlayers().contains(new Player(mMyId, null,
                    mMyEmail, mMyName, mMyPhoto))) {
                // Open the game board screen
                mActivity.playGame(game.getId());
            } else {
                // Ask for password
                mActivity.joinGame(game.getId(), game.getPassword());
            }
        }
    }
}