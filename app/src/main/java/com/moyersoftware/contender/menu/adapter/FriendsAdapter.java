package com.moyersoftware.contender.menu.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.menu.data.Friend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Friend> mFriends;

    public FriendsAdapter(Context context, ArrayList<Friend> friends) {
        mContext = context;
        mFriends = friends;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Friend friend = mFriends.get(position);

        holder.nameTxt.setText(friend.getName());
        holder.usernameTxt.setText(friend.getUsername());
        Picasso.with(mContext).load(friend.getImage()).placeholder(android.R.color.white)
                .centerCrop().fit().into(holder.img);
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.friend_img)
        ImageView img;
        @Bind(R.id.friend_name_txt)
        TextView nameTxt;
        @Bind(R.id.friend_username_txt)
        TextView usernameTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}