package com.example.kk.dididache.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.kk.dididache.R;
import com.example.kk.dididache.control.adapter.SearchItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity implements OnGetSuggestionResultListener {

    private List<String> mSuggest;
    private List<SuggestionResult.SuggestionInfo> infoList;
    private AutoCompleteTextView nodeText;
    private ArrayAdapter<String> nodeAdapter;
    private SuggestionSearch mSuggestionSearch = null;
    private LinearLayout itemLinear;
    public static final String LATLNG_BACK = "latlng_back";
    public static final String NAME_BACK = "name_back";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }
        setContentView(R.layout.activity_choose_area);

        itemLinear = (LinearLayout) findViewById(R.id.item_linear);
        ViewTreeObserver vto2 = itemLinear.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                itemLinear.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                nodeText.setDropDownWidth(itemLinear.getWidth());
            }
        });

        nodeText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        nodeText.setThreshold(1);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        nodeText.addTextChangedListener(new TextWatcher() {


            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(cs.toString()).city("广州").citylimit(true));
            }
        });

        nodeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LatLng latLng = infoList.get(position).pt;
                Intent intent = new Intent();
                intent.putExtra(LATLNG_BACK, latLng);
                intent.putExtra(NAME_BACK, mSuggest.get(position));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     * @param res
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if(res == null || res.getAllSuggestions() == null){
            return;
        }
//        stNode = PlanNode.withLocation(res.getAllSuggestions().get(0).pt);
//        enNode = PlanNode.withLocation(res.getAllSuggestions().get(1).pt);

            infoList = res.getAllSuggestions();
            mSuggest = new ArrayList<>();
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                if (info.key != null) {
                    mSuggest.add(info.key);
                }
            }
            nodeAdapter = new SearchItemAdapter(this, mSuggest);
            nodeText.setAdapter(nodeAdapter);
            nodeAdapter.notifyDataSetChanged();
        }

    @Override
    protected void onDestroy() {
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        ImageView animImage = (ImageView) findViewById(R.id.anim_image);
        AnimationDrawable animationDrawable = (AnimationDrawable) animImage.getDrawable();
        animationDrawable.start();
        super.onResume();
    }
}
