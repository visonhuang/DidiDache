package com.example.kk.dididache.control.adapter;

/**
 * Created by KK on 2017/8/20.
 */
import android.support.v7.widget.CardView;

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 8;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();

}
