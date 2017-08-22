package com.example.kk.dididache.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.kk.dididache.MethodsKt;
import com.example.kk.dididache.R;
import com.example.kk.dididache.control.adapter.SearchItemAdapter;
import com.example.kk.dididache.model.DataKeeper;
import com.example.kk.dididache.model.Event.DriveTimeEvent;
import com.example.kk.dididache.model.Http;
import com.example.kk.dididache.model.netModel.request.DriveTimeInfo;
import com.example.kk.dididache.model.netModel.request.Xy;
import com.example.kk.dididache.model.netModel.response.DriveTime;
import com.example.kk.dididache.util.DrivingRouteOverlay;
import com.example.kk.dididache.util.OverlayManager;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RoutePlanActivity extends AppCompatActivity
        implements OnGetRoutePlanResultListener, View.OnClickListener {

    public LocationClient mLocationClient;
    private boolean isFirstLocate = true;

    CardView mBtnPre = null; // 上一个节点
    CardView mBtnNext = null; // 下一个节点
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
    private TextView startNodeText;
    private TextView endNodeText;
    private CardView mCardView;

    public static final int START_REQUEST_CODE = 1;
    public static final int END_REQUEST_CODE = 2;
    public static final String START_KEY = "start_key";
    public static final String END_KEY = "end_key";
    public static final String KEY = "key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_plan_route);

        ImageView backImage = (ImageView) findViewById(R.id.back_image);
        backImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EventBus.getDefault().register(this);

        MapView.setMapCustomEnable(true);//设置个性化

        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        mBaidumap.setMyLocationEnabled(true);
        mBtnPre = (CardView) findViewById(R.id.pre);
        mBtnNext = (CardView) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        mCardView = (CardView) findViewById(R.id.card_view);
        startNodeText = (TextView) findViewById(R.id.start_node);
        endNodeText = (TextView) findViewById(R.id.end_node);
        startNodeText.setOnClickListener(this);
        endNodeText.setOnClickListener(this);

        requestLocation();
    }

    // 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_banma);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
//            return BitmapDescriptorFactory.fromResource(R.drawable.logo);
            return null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_node:
                Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, startNodeText, "shareElementName").toBundle();
                Intent intent1 = new Intent(this, ChooseAreaActivity.class);
                ActivityCompat.startActivityForResult(this,intent1, START_REQUEST_CODE, options);
                break;
            case R.id.end_node:
                Bundle options2 = ActivityOptionsCompat.makeSceneTransitionAnimation(this, endNodeText, "shareElementName").toBundle();
                Intent intent2 = new Intent(this, ChooseAreaActivity.class);
                ActivityCompat.startActivityForResult(this, intent2, END_REQUEST_CODE, options2);
                break;
            default:
                break;
        }
    }

    /**
     * 节点浏览示例
     * 发起路线规划搜索示例
     */
    public void searchButtonProcess() {
        // 重置浏览节点的路线数据
        route = null;
//        mBtnPre.setVisibility(View.INVISIBLE);
//        mBtnNext.setVisibility(View.INVISIBLE);
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
        popupText.setGravity(Gravity.CENTER_HORIZONTAL);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK){
            return;
        }
        switch (requestCode){
            case START_REQUEST_CODE:
                Toast.makeText(this, "onActivityResult", Toast.LENGTH_SHORT).show();
                stLatLng = (LatLng) data.getParcelableExtra(ChooseAreaActivity.LATLNG_BACK);
                Logger.d(stLatLng.toString() + "");
                startNodeText.setText(data.getStringExtra(ChooseAreaActivity.NAME_BACK));
                if(!TextUtils.isEmpty(endNodeText.getText().toString())){
                    searchButtonProcess();
                }
                break;
            case END_REQUEST_CODE:
                enLatLng = (LatLng) data.getParcelableExtra(ChooseAreaActivity.LATLNG_BACK);
                endNodeText.setText(data.getStringExtra(ChooseAreaActivity.NAME_BACK));
                if(!TextUtils.isEmpty(startNodeText.getText().toString())){
                    searchButtonProcess();
                }
                break;
            default:
                break;
        }
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
                List<DrivingRouteLine> lineList = null;
                lineList = nowResultdrive.getRouteLines();
                ArrayList<ArrayList<Xy>> xyDoubleList = new ArrayList<>();
                for(int i = 0; i < lineList.size(); i++){
                    DrivingRouteLine line = lineList.get(i);
                    ArrayList<Xy> xyList = new ArrayList<>();
                    for(int j = 0; j < line.getAllStep().size(); j++){
                        DrivingRouteLine.DrivingStep step = line.getAllStep().get(j);
                        for(int z = 0; z < step.getWayPoints().size(); z++){
                            xyList.add(new Xy(step.getWayPoints().get(z)));
                        }
                    }
                    xyDoubleList.add(xyList);
                }
                Calendar calendar = DataKeeper.getInstance().getTime();
                String time = MethodsKt.toStr(calendar);
                DriveTimeInfo info = new DriveTimeInfo(time, xyDoubleList);
                Http.getInstance().doPost(Http.ADRESS.driveTime, info);

            }else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    @Subscribe
    public void getBestLine(DriveTimeEvent driveTimeEvent){
        if(driveTimeEvent == null) return;
        DriveTime driveTime = driveTimeEvent.getDriveTime();
        int position = driveTime.getIndex();
        route = nowResultdrive.getRouteLines().get(position);
        DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
        mBaidumap.setOnMarkerClickListener(overlay);
        routeOverlay = overlay;
        overlay.setData(nowResultdrive.getRouteLines().get(0));
        overlay.addToMap();
        overlay.zoomToSpan();
        mBtnPre.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.VISIBLE);
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    private void navigateTo(BDLocation location) {
        Toast.makeText(this, "nav to " + location.getAddrStr(), Toast.LENGTH_SHORT).show();
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaidumap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            mBaidumap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.
                Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaidumap.setMyLocationData(locationData);
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
        mLocationClient.stop();
        mMapView.onDestroy();
        mBaidumap.setMyLocationEnabled(false);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
}
