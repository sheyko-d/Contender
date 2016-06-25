package com.moyersoftware.contender.game.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.data.Game;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JoinGamesAdapter extends RecyclerView.Adapter<JoinGamesAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Game> mGames;

    public JoinGamesAdapter(Context context, ArrayList<Game> games) {
        mContext = context;
        mGames = games;
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
        holder.authorTxt.setText(mContext.getString(R.string.join_author_txt, game.getAuthorUsername()));
        Picasso.with(mContext).load(game.getImage()).placeholder(android.R.color.white)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.game_img)
        ImageView img;
        @Bind(R.id.game_name_txt)
        TextView nameTxt;
        @Bind(R.id.game_author_txt)
        TextView authorTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}