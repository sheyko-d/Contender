package com.moyersoftware.contender.game.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.HostActivity;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HostEventsAdapter extends RecyclerView.Adapter<HostEventsAdapter.ViewHolder> {

    // Constants
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // Usual variables
    private HostActivity mActivity;
    private ArrayList<Event> mEvents;

    public HostEventsAdapter(HostActivity activity, ArrayList<Event> events) {
        mActivity = activity;
        mEvents = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType
                == TYPE_ITEM ? R.layout.item_event : R.layout.item_event_header, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return mEvents.get(position).getId() == null ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = mEvents.get(position);

        if (getItemViewType(position) == TYPE_ITEM) {
            assert holder.homeNameTxt != null;
            assert holder.timeTxt != null;

            holder.awayNameTxt.setText(event.getTeamAway().getName());
            Picasso.with(mActivity).load(event.getTeamAway().getImage()).into(holder.awayImg);

            holder.homeNameTxt.setText(event.getTeamHome().getName());
            Picasso.with(mActivity).load(event.getTeamHome().getImage()).into(holder.homeImg);

            holder.timeTxt.setText(Util.formatTime(event.getTime()+1000*60*60));
        } else {
            holder.awayNameTxt.setText(event.getWeek());
        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Nullable
        @Bind(R.id.event_away_img)
        ImageView awayImg;
        @Bind(R.id.event_away_name_txt)
        TextView awayNameTxt;
        @Nullable
        @Bind(R.id.event_home_img)
        ImageView homeImg;
        @Nullable
        @Bind(R.id.event_home_name_txt)
        TextView homeNameTxt;
        @Nullable
        @Bind(R.id.event_time_txt)
        TextView timeTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mActivity.setSelectedEvent(mEvents.get(getAdapterPosition()));
        }
    }
}