package com.example.kk.dididache.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.kk.dididache.R;
import com.example.kk.dididache.util.DrivingRouteOverlay;
import com.example.kk.dididache.util.OverlayManager;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanActivity extends AppCompatActivity
        implements OnGetSuggestionResultListener, OnGetRoutePlanResultListener {

    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    private TextView popupText = null; //泡泡view
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    RoutePlanSearch mSearch = null;
    DrivingRouteResult nowResultdrive = null;
    PlanNode stNode;
    PlanNode enNode;
    LatLng stLatLng;
    LatLng enLatLng;

    private SuggestionSearch mSuggestionSearch = null;
    private List<String> stSuggest;
    private List<String> enSuggest;
    private List<SuggestionResult.SuggestionInfo> stInfoList;
    private List<SuggestionResult.SuggestionInfo> enInfoList;
    private AutoCompleteTextView startNodeText;
    private AutoCompleteTextView endNodeText;
    private ArrayAdapter<String> startAdapter;
    private ArrayAdapter<String> endAdapter;
    private static final int START_NODE_TEXT = 1;
    private static final int END_NODE_TEXT = 2;
    private static int NODE_TEXT = START_NODE_TEXT;
    private static final int ITEM_SELECTED = 1;
    private static final int ITEM_NO_SELECTED = 2;
    private static int START_IS_SELECTED = ITEM_NO_SELECTED;
    private static int END_IS_SELECTED = ITEM_NO_SELECTED;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_route);
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        startNodeText = (AutoCompleteTextView) findViewById(R.id.start_node);
        endNodeText = (AutoCompleteTextView) findViewById(R.id.end_node);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        startAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line);
        startNodeText.setAdapter(startAdapter);

        endAdapter = new ArrayAdapter<String>(this,

                android.R.layout.simple_dropdown_item_1line);
        endNodeText.setAdapter(endAdapter);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        startNodeText.addTextChangedListener(new TextWatcher() {

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

        startNodeText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    NODE_TEXT = START_NODE_TEXT;
                    return;
                }
            }
        });

        startNodeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stLatLng = stInfoList.get(position).pt;
                START_IS_SELECTED = ITEM_SELECTED;
                Toast.makeText(RoutePlanActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();
            }
        });

        startNodeText.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(START_IS_SELECTED != ITEM_SELECTED){
                    startNodeText.setText("");
                }
                START_IS_SELECTED = ITEM_NO_SELECTED;
            }
        });

        endNodeText.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(END_IS_SELECTED != ITEM_SELECTED){
                    endNodeText.setText("");
                }
                END_IS_SELECTED = ITEM_NO_SELECTED;
            }
        });

        endNodeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                enLatLng = enInfoList.get(position).pt;
                END_IS_SELECTED = ITEM_SELECTED;
            }

        });

        endNodeText.addTextChangedListener(new TextWatcher() {

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

        endNodeText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    NODE_TEXT = END_NODE_TEXT;
                    return;
                }
            }
        });
    }

    /**
     * 节点浏览示例
     *
     * @param v/**
     * 发起路线规划搜索示例
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        route = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaidumap.clear();
        // 处理搜索按钮响应
        // 设置起终点信息，对于tranist search 来说，城市名无意义
        if(stLatLng == null){
            Logger.d("stLatLng == null");
        }
        if(enLatLng == null){
            Logger.d("enLatLng == null");
        }
        stNode = PlanNode.withLocation(stLatLng);
        enNode = PlanNode.withLocation(enLatLng);
        mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
    }

    public void nodeClick(View v){
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;

        if(route == null || route.getAllStep() == null){
            return;
        }
        if(nodeIndex == -1 && v.getId() == R.id.pre){
            return;
        }
        // 设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }
        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }
        //获取节结果信息
        step = route.getAllStep().get(nodeIndex);
        nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
        nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        // 移动节点至中心
        mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(RoutePlanActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            result.getSuggestAddrInfo();
            return;
        }
        if(result.error == SearchResult.ERRORNO.NO_ERROR){
            nodeIndex = -1;

            if(result.getRouteLines().size() >= 1){
                nowResultdrive = result;

                route = nowResultdrive.getRouteLines().get(0);
                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(nowResultdrive.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                mBtnPre.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
            }else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     * @param res
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if(res == null || res.getAllSuggestions() == null){
            return;
        }
//        stNode = PlanNode.withLocation(res.getAllSuggestions().get(0).pt);
//        enNode = PlanNode.withLocation(res.getAllSuggestions().get(1).pt);


        if(NODE_TEXT == START_NODE_TEXT){
            stInfoList = res.getAllSuggestions();
            stSuggest = new ArrayList<>();
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                if (info.key != null) {
                    stSuggest.add(info.key);
                }
            }
            startAdapter = new ArrayAdapter<String>(RoutePlanActivity.this, android.R.layout.simple_dropdown_item_1line, stSuggest);
            startNodeText.setAdapter(startAdapter);
            startAdapter.notifyDataSetChanged();
        }
        if(NODE_TEXT == END_NODE_TEXT){
            enInfoList = res.getAllSuggestions();
            enSuggest = new ArrayList<>();
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                if (info.key != null) {
                    enSuggest.add(info.key);
                }
            }
            endAdapter = new ArrayAdapter<String>(RoutePlanActivity.this, android.R.layout.simple_dropdown_item_1line, enSuggest);
            endNodeText.setAdapter(endAdapter);
            endAdapter.notifyDataSetChanged();
        }
    }

    // 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return null;
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mSearch != null) {
            mSearch.destroy();
        }
        mMapView.onDestroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }
}
