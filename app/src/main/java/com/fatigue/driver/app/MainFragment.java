package com.fatigue.driver.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Eric on 11/14/2016.
 */

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_main, container, false);


        /*
        RelativeLayout card = (RelativeLayout) view.findViewById(R.id.connection_status_layout);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int half_width = (width
                - (int)getResources().getDimension(R.dimen.card_default_padding)*2
                - (int)getResources().getDimension(R.dimen.activity_horizontal_margin_cards)*2
                - (int)getResources().getDimension(R.dimen.card_corners)*2)
                /2;

        LinearLayout lay1 = (LinearLayout) view.findViewById(R.id.layout_connection_text);
        LinearLayout lay2 = (LinearLayout) view.findViewById(R.id.layout_connection_vars);
        lay1.getLayoutParams().width = half_width;
        lay2.getLayoutParams().width = half_width;
        */

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}