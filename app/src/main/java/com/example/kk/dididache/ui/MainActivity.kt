package com.example.kk.dididache.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.app.SharedElementCallback
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
import android.view.View
import com.baidu.location.*
import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.*
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import com.example.kk.dididache.*
import com.example.kk.dididache.control.adapter.SearchItemAdapter
import com.example.kk.dididache.control.adapter.SelectTimeManager
import com.example.kk.dididache.model.*
import com.example.kk.dididache.model.Event.ExceptionEvent
import com.example.kk.dididache.model.Event.HeatMapEvent
import com.example.kk.dididache.model.netModel.request.ExceptionInfo
import com.example.kk.dididache.model.netModel.request.HeatInfo
import com.example.kk.dididache.model.netModel.request.RealTimeHeatInfo
import com.example.kk.dididache.widget.ChartDialog
import com.google.gson.Gson
import com.google.gson.internal.bind.DateTypeAdapter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() {
    //定位相关
    private val locClient by lazy { LocationClient(this) }
    private var locationListener: MyLocationListener? = MyLocationListener() //定位监听
    private var curPoint: LatLng = LatLng(0.0, 0.0)//当前经纬度
        get() = LatLng(locData?.latitude ?: 0.0, locData?.longitude ?: 0.0)
    private var locData: MyLocationData? = null//坐标信息
    private var exceptions: ExceptionEvent? = null
    private var exceptionOverLays: MutableList<Overlay> = mutableListOf()
    private var poiOverLays: MutableList<Overlay> = mutableListOf()
    //UI相关
    private val map by lazy { mapView.map }
    private var heatMap: HeatMap? = null
        set(value) {
            field?.removeHeatMap()
            field = value
            map.addHeatMap(field)//自动添加
        }
    private var chartDialog: ChartDialog? = null
    private var isUnusualShowing = false//是否在显示异常点
    private var suggestAdapter: SearchItemAdapter? = null
    private val poimarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.destination_point_blue)
    private val exceptionIcon = BitmapDescriptorFactory.fromResource(R.drawable.unusual)

    private var timeManager: SelectTimeManager? = null

    //搜索相关
    private var suggestSearch = SuggestionSearch.newInstance()
    private var poiSearch = PoiSearch.newInstance()

    //地图状态变化监听器
    private var mapStateChangeListener: OnMapStatusChangeListener = object : OnMapStatusChangeListener {
        override fun onMapStatusChangeStart(p0: MapStatus?) {

        }

        override fun onMapStatusChange(p0: MapStatus?) {

        }

        override fun onMapStatusChangeFinish(p0: MapStatus?) {
            Logger.i("地图边界${p0?.bound}")
            //Http.getInstance().getCarsUnderBounds(map.mapStatus.bound)
        }
    }
    private var onMapClickListener: BaiduMap.OnMapClickListener = object : BaiduMap.OnMapClickListener {
        override fun onMapClick(p0: LatLng) {
            chartDialog?.show(timeManager!!.timeSelected, p0)
            if (exceptions != null) {
                //TODO 判断该处是否有异常
                exceptions!!.exceptions
                        .filter { (it.x in p0.longitude - 0.0001..p0.longitude + 0.0001) && (it.y in p0.latitude - 0.0001..p0.latitude + 0.0001) }
                        .forEach {
                            chartDialog?.hasException = true
                            DataKeeper.getInstance().exception = it
                            //保存后结束方法
                            return
                        }
                chartDialog?.hasException = false
            } else chartDialog?.hasException = false

        }

        override fun onMapPoiClick(p0: MapPoi): Boolean {
            chartDialog?.show(timeManager!!.timeSelected, p0.position)
            if (exceptions != null) {
                //TODO 判断该处是否有异常
                exceptions!!.exceptions
                        .filter { (it.x in p0.position.longitude - 0.0001..p0.position.longitude + 0.0001) && (it.y in p0.position.latitude - 0.0001..p0.position.latitude + 0.0001) }
                        .forEach {
                            chartDialog?.hasException = true
                            DataKeeper.getInstance().exception = it
                            //保存后结束方法
                            return true
                        }
                chartDialog?.hasException = false
            } else chartDialog?.hasException = false
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requsetPermission()//请求权限
    }

    private fun requsetPermission() {
        val permissionList = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.READ_PHONE_STATE)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!permissionList.isEmpty())
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 1)
        else {

            initMap()//初始化地图
            initLocation()
            initLoc()
        }//同意权限开启定位
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()) {
                    grantResults.map {
                        if (it != PackageManager.PERMISSION_GRANTED) {
                            showToast("有部分权限未授权")
                            finish()
                            return
                        }
                    }
                    initMap()//初始化地图
                    initLocation()
                    initLoc()//授权完毕，初始化定位
                } else {
                    showToast("发生未知错误")
                    return
                }
            }
        }
    }

    //定位监听器,还可以对热点监听等等
    inner class MyLocationListener : BDAbstractLocationListener() {

        override fun onReceiveLocation(p0: BDLocation?) {
            if (p0 == null) return
            //复制坐标
            curPoint = LatLng(p0.latitude, p0.longitude)
            locData = MyLocationData.Builder()
                    .accuracy(p0.radius)
                    .latitude(p0.latitude)
                    .longitude(p0.longitude)
                    .build()
            map.setMyLocationData(locData)

            /***********/
            if (timeManager!!.isNow) {
                val timeBound = chartDialog!!.getTimeBound(Calendar.getInstance().getTimeNow(), false)
                val info = RealTimeHeatInfo(timeBound.first.toStr(), timeBound.second.toStr())
                Http.getInstance().cancelCall(Http.TAG_HEAT_POINTS)
                Http.getInstance().doPost(Http.ADRESS.realTimeHeatMap, info)
            }

            /***********/

        }
    }

    //默认定位在广州市中心
    private fun initLocation() {
        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                MapStatus.Builder()
                        .target(LatLng(23.135288368164, 113.27128276473))
                        .zoom(10.0F).build()))
    }

    //初始化定位
    private fun initLoc() {
        //注册定位监听器
        map.isMyLocationEnabled = true
        locClient.registerLocationListener(locationListener)
        val option = LocationClientOption()
        option.isOpenGps = true//开gps
        option.coorType = "bd09ll"//设置坐标类型
        option.scanSpan = 5000//扫描速度
        locClient.locOption = option
        Logger.i("开启定位")
        map.setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, BitmapDescriptorFactory.fromResource(R.drawable.my_location_point)))
        initView()//初始化视图
    }

    //初始化视图
    private fun initView() {
        timeManager = SelectTimeManager(this) {
            onSelect {
                val timeBound = chartDialog!!.getTimeBound(timeSelected, false)
                Http.getInstance().cancelCall(Http.TAG_HEAT_POINTS)
                Http.getInstance().doPost(Http.ADRESS.heatMap, HeatInfo(timeBound.first.toStr(), timeBound.second.toStr(), map.mapStatus.bound.northeast, map.mapStatus.bound.southwest))

                val timeBound2 = chartDialog!!.getTimeBound(timeSelected, true)
                Http.getInstance().cancelCall(Http.TAG_EXCEPTION)
                Http.getInstance().doPost(Http.ADRESS.exception, ExceptionInfo(timeBound2.first.toStr(), timeBound2.second.toStr(), map.mapStatus.bound.northeast, map.mapStatus.bound.southwest))

            }
        }
        freshTime.onClick {
            map.clear()
            timeManager?.freshTime()
        }

        timeButton.onClick { timeManager?.show() }

        gotoMyLoc.onClick { backToMyLoc(18.0F) }

        openUnusual.onClick {
            if (timeManager!!.timeMode == -1) {
                isUnusualShowing = !isUnusualShowing
                if (isUnusualShowing) {
                    //显示异常点
                    openUnusual.unusualImageView.imageResource = R.drawable.cancel_unusual
                    if (exceptions == null || exceptions!!.exceptions.isEmpty()) {
                        //空不显示
                        return@onClick
                    } else {
                        exceptionOverLays = map.addOverlays(exceptions!!.exceptions.map { MarkerOptions().position(LatLng(it.y, it.x)).icon(exceptionIcon) })
                    }
                } else {
                    //去除异常点
                    openUnusual.unusualImageView.imageResource = R.drawable.open_unusual
                    exceptionOverLays.map { it.remove() }
                }
            } else showToast("非过去时间无法显示异常")

        }
        queryPath.onClick {
            if (timeManager!!.timeMode != -1) {
                DataKeeper.getInstance().time = timeManager!!.timeSelected
                startActivity<RoutePlanActivity>()
            } else showToast("过去时间无法预测")
        }

        chartDialog = ChartDialog(this@MainActivity, timeManager!!) {
            onChartClick {
                val intent = Intent(this@MainActivity, C::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) this@MainActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, viewPager, "viewPagerTransition").toBundle())
                else this@MainActivity.startActivity(intent)
            }
            onDetail {
                val intent = Intent(this@MainActivity, C::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) this@MainActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, viewPager, "viewPagerTransition").toBundle())
                else this@MainActivity.startActivity(intent)

            }
        }

        searchTextView.setAdapter(suggestAdapter)
        searchTextView.threshold = 1
        searchTextView.dropDownVerticalOffset = 5
        suggestSearch.setOnGetSuggestionResultListener {
            if (it == null || it.allSuggestions == null) return@setOnGetSuggestionResultListener
            suggestAdapter = SearchItemAdapter(this, it.allSuggestions.map { it.key })
            searchTextView.setAdapter(suggestAdapter)
            suggestAdapter?.notifyDataSetChanged()
        }

        //调整drop down宽度
        searchTextView.onTouch { _, _ ->
            searchTextView.dropDownWidth = searchCardView.width
        }

        searchTextView.setOnItemClickListener { adapterView, view, i, l ->
            searchTextView.clearFocus()
        }
        searchTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                if (p0.isEmpty()) return
                suggestSearch
                        .requestSuggestion(SuggestionSearchOption().city("广州").citylimit(true).keyword(p0.toString()))
            }
        })
        searchButton.onClick {
            val info: String = searchTextView.text.toString()
            if (info.isEmpty()) {
                showToast("搜索内容为空")
                return@onClick
            }
            poiSearch.searchInCity(PoiCitySearchOption().city("广州").keyword(info))

        }


        poiSearch.setOnGetPoiSearchResultListener(object : OnGetPoiSearchResultListener {
            override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {

            }

            override fun onGetPoiResult(p0: PoiResult?) {
                if (p0 == null || p0.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    showToast("未找到结果")
                    return
                }
                if (p0.error == SearchResult.ERRORNO.NO_ERROR) {
                    poiOverLays.map { it.remove() }
                    poiOverLays = map.addOverlays(p0.allPoi.map {
                        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                                MapStatus.Builder()
                                        .target(LatLng(it.location.latitude, it.location.longitude))
                                        .zoom(18F).build()))
                        MarkerOptions().position(it.location).icon(poimarkerIcon)

                    })
                }
            }

            override fun onGetPoiDetailResult(p0: PoiDetailResult?) {

            }
        })

        //android5.0以上采用共享元素
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //若不克隆，两个动画将会是同一个，也就是说设置的监听器在启动下一个活动也会执行
            window.sharedElementReenterTransition = window.sharedElementExitTransition.clone()
            window.sharedElementReenterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(p0: Transition?) {
                }

                override fun onTransitionResume(p0: Transition?) {

                }

                override fun onTransitionPause(p0: Transition?) {

                }

                override fun onTransitionCancel(p0: Transition?) {

                }

                override fun onTransitionStart(p0: Transition?) {
                    viewPager.setCurrentItem(DataKeeper.getInstance().page, false)
                }
            })
        }

    }

    private fun initMap() {
        MapView.setMapCustomEnable(true)//设置个性化
        map.setOnMapStatusChangeListener(mapStateChangeListener)//地图状态变化监听
        map.setOnMapClickListener(onMapClickListener)//地图点击监听
        map.setOnMapLoadedCallback {
            Logger.i("地图加载完成")
            map.setMapStatusLimits(map.mapStatus.bound)
        }
        mapView.showZoomControls(false)
    }

    //回到定位位置
    private fun backToMyLoc(zoom: Float) {
        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                MapStatus.Builder()
                        .target(LatLng(curPoint.latitude, curPoint.longitude))
                        .zoom(zoom).build()))
    }

    @Subscribe
    fun addHeatMap(event: HeatMapEvent) {
        if (event.list.isEmpty()) return
        Logger.json(Gson().toJson(event))
        heatMap = HeatMap.Builder().weightedData(event.list.map { WeightedLatLng(LatLng(it.y, it.x), it.c.toDouble()) }).build()
    }

    @Subscribe
    fun getUnusual(event: ExceptionEvent) {
        exceptions = event
        if (isUnusualShowing) {
            if (!exceptionOverLays.isEmpty()) exceptionOverLays.map { it.remove() }
            exceptionOverLays = map.addOverlays(event.exceptions.map { MarkerOptions().position(LatLng(it.y, it.x)).icon(exceptionIcon) })
        }
    }

    override fun onResume() {
        EventBus.getDefault().register(this)
        mapView.onResume()
        locClient.registerLocationListener(locationListener)
        locClient.start()
        MapView.setMapCustomEnable(true)
        Logger.i("注册定位监听")
        super.onResume()
    }

    override fun onDestroy() {
        locClient.stop()// 退出时销毁定位
        map.isMyLocationEnabled = false// 关闭定位图层
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)//取消订阅
        locClient.unRegisterLocationListener(locationListener)
        locClient.stop()
        Logger.i("注销定位监听")
        Http.getInstance().cancelCall(Http.TAG_HEAT_POINTS)
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {


        super.onStop()
    }

    override fun onBackPressed() {
        if (chartDialog!!.isShowing) {
            chartDialog?.dismiss()
        } else if (timeManager != null && timeManager!!.isShowing) {
            timeManager?.dismiss()
        } else {
            super.onBackPressed()
        }
    }


}
