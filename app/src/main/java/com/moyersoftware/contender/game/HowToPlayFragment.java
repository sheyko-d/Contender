package com.moyersoftware.contender.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;

public class HowToPlayFragment extends Fragment {
    // Store instance variables
    private String title;
    private String description;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static HowToPlayFragment newInstance(int page, String title, String description) {
        HowToPlayFragment fragmentFirst = new HowToPlayFragment();
        Bundle args = new Bundle();
        args.putInt("page", page);
        args.putString("title", title);
        args.putString("description", description);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("page", 0);
        title = getArguments().getString("title");
        description = getArguments().getString("description");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_how_to_play, container, false);
        TextView titleTxt = (TextView) view.findViewById(R.id.text);
        TextView descriptionTxt = (TextView) view.findViewById(R.id.description);
        ImageView image = (ImageView) view.findViewById(R.id.image);
        titleTxt.setText(title);
        descriptionTxt.setText(description);
        image.setImageResource(HowToPlayActivity.mImages[page]);
        return view;
    }
}