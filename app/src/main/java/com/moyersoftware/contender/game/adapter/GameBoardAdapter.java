package com.moyersoftware.contender.game.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.data.SelectedSquare;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GameBoardAdapter extends RecyclerView.Adapter<GameBoardAdapter.ViewHolder> {

    // Constants
    private final static int CELLS_COUNT = 100;

    // Usual variables
    private GameBoardActivity mActivity;
    private HashMap<Integer, SelectedSquare> mSelectedPositions = new HashMap<>();

    public GameBoardAdapter(GameBoardActivity activity) {
        mActivity = activity;
    }

    public void refresh(ArrayList<SelectedSquare> selectedSquares) {
        mSelectedPositions.clear();
        for (SelectedSquare selectedSquare: selectedSquares){
            mSelectedPositions.put(selectedSquare.getPosition(), selectedSquare);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_board, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mSelectedPositions.containsKey(position)) {
            SelectedSquare selectedSquare = mSelectedPositions.get(position);
            holder.nameTxt.setText(selectedSquare.getAuthorUsername());
            Picasso.with(mActivity).load(selectedSquare.getAuthorPhoto()).placeholder
                    (R.drawable.avatar_placeholder).centerCrop().fit().into(holder.img);
        } else {
            holder.nameTxt.setText("");
            holder.img.setImageResource(0);
        }
    }

    @Override
    public int getItemCount() {
        return CELLS_COUNT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.cell_name_txt)
        TextView nameTxt;
        @Bind(R.id.cell_img)
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mActivity.selectSquare(getAdapterPosition());
        }
    }
}