package com.moyersoftware.contender.game.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.menu.data.Player;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GamePaidPlayersAdapter extends RecyclerView.Adapter<GamePaidPlayersAdapter.ViewHolder> {

    // Usual variables
    private GameBoardActivity mActivity;
    private ArrayList<Player> mPlayers = new ArrayList<>();
    private ArrayList<String> mPaidPlayers;

    public GamePaidPlayersAdapter(GameBoardActivity activity, ArrayList<Player> friends) {
        mActivity = activity;
        mPlayers = friends;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_paid_player, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Player player = mPlayers.get(position);
        holder.nameTxt.setText(player.getName());
        holder.nameTxt.setChecked(mPaidPlayers.contains(player.getUserId()));
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public void setPaidPlayers(ArrayList<String> paidPlayers) {
        mPaidPlayers = paidPlayers;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener {

        @Bind(R.id.friend_invite_name_txt)
        CheckBox nameTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            nameTxt.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            mActivity.setPlayerPaid(mPlayers.get(getAdapterPosition()).getUserId(), checked);
        }
    }
}