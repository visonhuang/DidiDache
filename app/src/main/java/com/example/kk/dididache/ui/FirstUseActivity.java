package com.example.kk.dididache.ui;

/**
 * Created by KK on 2017/8/20.
 */
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.kk.dididache.MethodsKt;
import com.example.kk.dididache.R;
import com.example.kk.dididache.widget.CirclePageIndicator;

public class FirstUseActivity extends AppCompatActivity{

    private Button dialogButton;
    private ViewPager mViewPager;
    private CirclePageIndicator mPageIndicator;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_use);
        CardView cardView = (CardView) findViewById(R.id.enter_button);
        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstUseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        dialogButton = findViewById(R.id.dialog_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MethodsKt.showSetIpPortDialog(FirstUseActivity.this);
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mPageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);

        mFragmentCardAdapter = new CardFragmentPagerAdapter(getSupportFragmentManager(),
                dpToPixels(2, this));

        mFragmentCardShadowTransformer = new ShadowTransformer(mViewPager, mFragmentCardAdapter);
        mFragmentCardShadowTransformer.enableScaling(true);

        mViewPager.setAdapter(mFragmentCardAdapter);
        mViewPager.setPageTransformer(false, mFragmentCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);
        mPageIndicator.setViewPager(mViewPager);
    }



    public static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

}
