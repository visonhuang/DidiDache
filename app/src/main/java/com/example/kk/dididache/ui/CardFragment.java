package com.example.kk.dididache.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.kk.dididache.R;
import com.example.kk.dididache.control.adapter.CardAdapter;

/**
 * Created by KK on 2017/8/20.
 */

public class CardFragment extends Fragment {

    public static final String KEY = "card_fragment_key";
    private CardView mCardView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adapter, container, false);
        mCardView = (CardView) view.findViewById(R.id.cardView);
        mCardView.setMaxCardElevation(mCardView.getCardElevation()
                * CardAdapter.MAX_ELEVATION_FACTOR);
        ImageView cardImage = (ImageView) view.findViewById(R.id.card_view_image);
        int resId = getArguments().getInt(KEY);
        cardImage.setBackgroundResource(resId);
        return view;
    }

    public CardView getCardView() {
        return mCardView;
    }

    public static CardFragment newInstance(int resId) {
        Bundle args = new Bundle();
        args.putInt(KEY, resId);
        CardFragment fragment = new CardFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
