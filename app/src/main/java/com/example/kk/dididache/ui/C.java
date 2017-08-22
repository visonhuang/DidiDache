package com.example.kk.dididache.ui;

import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.example.kk.dididache.App;
import com.example.kk.dididache.MethodsKt;
import com.example.kk.dididache.MyOverShootInterpolator;
import com.example.kk.dididache.R;
import com.example.kk.dididache.control.adapter.ChartAdapter;
import com.example.kk.dididache.model.DataKeeper;
import com.example.kk.dididache.model.netModel.response.Exception;
import com.example.kk.dididache.widget.InkPageIndicator;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by linzongzhan on 2017/8/20.
 */

public class C extends AppCompatActivity {

//    private List<String> xAxis = new ArrayList<>();
//    private Calendar time = Calendar.getInstance();
//    private CombinedChart bigChart;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        time = (Calendar) getIntent().getSerializableExtra("time");
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chart);
//        bigChart = (CombinedChart) findViewById(R.id.bigChart);
//        bigChart.setData(DataKeeper.getInstance().getCombinedData());
//        setChartOptions();
//        bigChart.invalidate();
//    }
//
//    private void setChartOptions () {
//        Calendar p0 = (Calendar) time.clone();
//        p0.add(Calendar.MINUTE,-60);
//        xAxis.clear();
//        for (int i = 0;i <= 8;i++) {
//            xAxis.add(toString1("HH.mm",p0));
//            p0.add(Calendar.MINUTE,15);
//        }
//        bigChart.getDescription().setEnabled(false);
//        bigChart.getLegend().setEnabled(false);
//        bigChart.getAxisRight().setEnabled(false);
//        bigChart.getAxisLeft().setEnabled(true);
//        bigChart.getAxisLeft().setAxisMinimum(0F);
//        bigChart.getAxisLeft().setGranularity(1F);
//        bigChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        bigChart.getXAxis().setAxisMinimum(0F);
//        bigChart.getXAxis().setGranularity(1F);
//        bigChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return xAxis.get((int)value % 9);
//            }
//        });
//    }
//
//    private String toString1 (String format,Calendar calendar) {
//        SimpleDateFormat sdf = new SimpleDateFormat(format);
//        calendar.add(Calendar.MONTH,-1);
//        String result = sdf.format(time);
//        calendar.add(Calendar.MONTH,1);
//        return result;
//    }

    private CardView exceptionCardView;
    private ViewPager viewPager;
    private TextView where;
    private TextView when;
    private TextView difference;
    private TextView reason;
    private Toolbar toolbar;
    private InkPageIndicator inkPageIndicator;

    private List<View> viewList;

    private CombinedChart bigChart;
    private PieChart pieChart;

    private Calendar time = Calendar.getInstance();
    private List<String> xAxis = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setEnterSharedElementCallback(new SharedElementCallback() {
//                @Override
//                public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
//                    super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
//                }
//
//                @Override
//                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
//
//                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
//                }
//
//                @Override
//                public void onRejectSharedElements(List<View> rejectedSharedElements) {
//                    super.onRejectSharedElements(rejectedSharedElements);
//                }
//
//                @Override
//                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                    super.onMapSharedElements(names, sharedElements);
//                }
//
//                @Override
//                public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
//                    return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
//                }
//
//                @Override
//                public View onCreateSnapshotView(Context context, Parcelable snapshot) {
//                    return super.onCreateSnapshotView(context, snapshot);
//                }
//
//                @Override
//                public void onSharedElementsArrived(List<String> sharedElementNames, List<View> sharedElements, OnSharedElementsReadyListener listener) {
//                    addAnimationForCardView();
//                    super.onSharedElementsArrived(sharedElementNames, sharedElements, listener);
//                }
//            });
//
//
//        }
        setContentView(R.layout.activity_chart);

        initView();
        setToolBar();
        initChart();
        getMessage();
        setMessageForTextView();


    }

    /**
     * 初始化控件
     */
    private void initView() {
        exceptionCardView = (CardView) findViewById(R.id.cardView_exception);
        viewPager = (ViewPager) findViewById(R.id.chart_viewpager);
        where = (TextView) findViewById(R.id.text_where);
        when = (TextView) findViewById(R.id.text_when);
        difference = (TextView) findViewById(R.id.text_difference);
        reason = (TextView) findViewById(R.id.text_reason);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        inkPageIndicator = (InkPageIndicator) findViewById(R.id.chart_inkPageIndicator);

        bigChart = new CombinedChart(this);
        pieChart = new PieChart(this);

        viewPager.setAdapter(new ChartAdapter(bigChart, pieChart));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                DataKeeper.getInstance().setPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setSelectViewPagerConfig();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            exceptionCardView.setVisibility(View.GONE);
        }

        viewPager.setCurrentItem(DataKeeper.getInstance().getPage(),false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementReturnTransition(getWindow().getSharedElementEnterTransition().clone());
            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    exceptionCardView.setVisibility(View.VISIBLE);
                    addAnimationForCardView();
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        }
    }

    /**
     * 设置ToolBar
     */
    private void setToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.vector_drawable_chart_back);//设置返回图标
           // actionBar.setBackgroundDrawable();
        }
    }

    /**
     * 从Intent中获得信息
     */
    private void getMessage() {
        bigChart.setData(DataKeeper.getInstance().getCombinedData());
        pieChart.setData(DataKeeper.getInstance().getPieData());
    }

    /**
     * 初始化图表
     */
    private void initChart() {

        Calendar p0 = (Calendar) time.clone();
        p0.add(Calendar.MINUTE, -60);
        xAxis.clear();
        for (int i = 0; i <= 8; i++) {
            xAxis.add(MethodsKt.toStr(p0, "HH.mm"));
            p0.add(Calendar.MINUTE, 15);
        }
        bigChart.getDescription().setText("车流量变化图");
        bigChart.getDescription().setXOffset(0f);
        bigChart.getDescription().setTextSize(20);
        bigChart.getDescription().setYOffset(200f);
        bigChart.getLegend().setEnabled(false);
        bigChart.getAxisRight().setEnabled(false);
        bigChart.getAxisLeft().setEnabled(true);
        bigChart.getAxisLeft().setAxisMinimum(0F);
        bigChart.getAxisLeft().setGranularity(1F);
        bigChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        bigChart.getXAxis().setAxisMinimum(0F);
        bigChart.getXAxis().setGranularity(1F);
        bigChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxis.get((int) value % 9);
            }
        });

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDragDecelerationFrictionCoef(0.95F);
        pieChart.setCenterTextTypeface(App.Companion.getMTfLight());
        pieChart.setCenterText(genText());
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58F);
        pieChart.setTransparentCircleRadius(61F);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0F);
        pieChart.setRotationEnabled(true);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTypeface(App.Companion.getMTfRegular());
        pieChart.setEntryLabelTextSize(11f);
    }

    /**
     * 为TextView设置信息
     */
    private void setMessageForTextView() {
        Exception exception = DataKeeper.getInstance().getException();
        if (exception != null) {
            where.setText("经度：" + exception.getX() + "  纬度：" + exception.getY());
            time = DataKeeper.getInstance().getTime();
            when.setText(MethodsKt.toStr(time, "HH:mm"));
            difference.setText(exception.getException());
            reason.setText(exception.getReason());
        } else {
            where.setText("-");
            when.setText("-");
            difference.setText("-");
            reason.setText("-");
        }
    }

    /**
     * 将图表加入List中
     */
    private void addMessageToList() {

        viewList.add(bigChart);
        viewList.add(pieChart);
    }

    /**
     * 为ViewPager设置适配器
     *
     * @param viewPager
     */
    private void setAdapterForViewPager(final ViewPager viewPager) {
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(viewList.get(position));
            }
        };

        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * 为CardView添加动画
     */
    private void addAnimationForCardView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0.25f,Animation.RELATIVE_TO_SELF,0);
        translateAnimation.setDuration(200);
        translateAnimation.setFillAfter(true);
        //translateAnimation.setStartOffset(500);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f,1f);
        alphaAnimation.setDuration(200);
        alphaAnimation.setFillAfter(true);
        //alphaAnimation.setStartOffset(500);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.9f,1f,0.9f,1f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);
        //scaleAnimation.setStartOffset(500);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        exceptionCardView.startAnimation(animationSet);
    }

    /**
     * 设置ViewPager与点的关联
     */
    private void setSelectViewPagerConfig() {
        inkPageIndicator.setViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }
        return true;
    }

//    private String toString1 (String format,Calendar calendar) {
//        SimpleDateFormat sdf = new SimpleDateFormat(format);
//        calendar.add(Calendar.MONTH,-1);
//        String result = sdf.format(time);
//        calendar.add(Calendar.MONTH,1);
//        return result;
//    }

    private SpannableString genText() {
        SpannableString s = new SpannableString("出租车载客率\npowered by QG Studio");
        s.setSpan(new RelativeSizeSpan(1.2f), 0, 6, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 6, s.length() - 9, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 6, s.length() - 9, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 6, s.length() - 9, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 9, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 9, s.length(), 0);
        return s;
    }
}
