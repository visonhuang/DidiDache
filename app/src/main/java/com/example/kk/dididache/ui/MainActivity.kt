package com.example.kk.dididache.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import com.baidu.location.*
import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener
import com.baidu.mapapi.model.LatLng

import com.example.kk.dididache.R
import com.example.kk.dididache.Tagg
import com.example.kk.dididache.model.Http
import com.example.kk.dididache.showToast
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.imageView
import org.jetbrains.anko.verticalLayout

class MainActivity : AppCompatActivity(), SensorEventListener {
    //定位相关
    private val locClient by lazy { LocationClient(this) }
    private val map by lazy { mapView.map }
    private val senorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }//传感器管理
    private val locationListener by lazy { MyLocationListener() } //定位监听
    private var curPoint: LatLng = LatLng(0.0, 0.0)//当前经纬度
        get() = LatLng(locData?.latitude ?: 0.0, locData?.longitude ?: 0.0)
    private var locData: MyLocationData? = null//坐标信息
    private var curDirection = 0//当前方向
    private var lastX: Double = 0.0
    private var isFirstLoc = true
    private var heatMap: HeatMap? = null
        set(value) {
            field?.removeHeatMap()
            field = value
            map.addHeatMap(field)//自动添加
        }
    //地图状态变化监听器
    private var mapStateChangeListener: OnMapStatusChangeListener = object : OnMapStatusChangeListener {
        override fun onMapStatusChangeStart(p0: MapStatus?) {
            Log.d(Tagg, "地图状态开始改变")
        }

        override fun onMapStatusChange(p0: MapStatus?) {
            Log.d(Tagg, "地图状态改变中")
        }

        override fun onMapStatusChangeFinish(p0: MapStatus?) {
            Log.d(Tagg, "地图状态改变结束")
            //Http.getInstance().getCarsUnderBounds(map.mapStatus.bound)
        }
    }
    private var onMapClickListener: BaiduMap.OnMapClickListener = object : BaiduMap.OnMapClickListener {
        override fun onMapClick(p0: LatLng?) {
            val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.news_item, null, false)
            map.showInfoWindow(InfoWindow(view, p0, 200))
        }

        override fun onMapPoiClick(p0: MapPoi?): Boolean {
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initLoc()//初始化定位
        requsetPermission()//请求权限

    }

    //SensorEventListener
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //SensorEventListener
    override fun onSensorChanged(p0: SensorEvent) {
        val x = p0.values[SensorManager.DATA_X].toDouble()
        if (Math.abs(x - lastX) > 1.0) {
            curDirection = x.toInt()
            locData = MyLocationData.Builder()
                    .accuracy(locData?.accuracy ?: 0F)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(curDirection.toFloat()).latitude(curPoint.latitude)
                    .longitude(curPoint.longitude)
                    .build()
            map.setMyLocationData(locData)
        }
        lastX = x
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
        else initLoc()//同意权限开启定位
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()) {
                    (0 until grantResults.size)
                            .filter { it != PackageManager.PERMISSION_GRANTED }
                            .forEach {
                                showToast("有部分权限未授权")
                                finish()
                                return
                            }
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
            Log.d(Tagg, "收到定位,${p0?.latitude} and ${p0?.longitude}")
            Log.d(Tagg, "地图边界,北${map.mapStatus.bound.northeast} and 南${map.mapStatus.bound.northeast}")
            if (p0 == null) return
            //复制坐标
            curPoint = LatLng(p0.latitude, p0.longitude)
            locData = MyLocationData.Builder()
                    .accuracy(p0.radius)
                    .direction(curDirection.toFloat())
                    .latitude(p0.latitude)
                    .longitude(p0.longitude)
                    .build()
            map.setMyLocationData(locData)
            if (isFirstLoc) {
                isFirstLoc = false
                backToMyLoc(18.0F)
                Http.getInstance().getCarsUnderBounds(map.mapStatus.bound)
            }
        }
    }

    //初始化定位
    private fun initLoc() {
        //注册定位监听器
        map.isMyLocationEnabled = true
        locClient.registerLocationListener(locationListener)
        val option = LocationClientOption()
        option.isOpenGps = true//开gps
        option.coorType = "bd09ll"//设置坐标类型
        option.scanSpan = 1000//扫描速度
        locClient.locOption = option
        locClient.start()
        Log.d(Tagg, "开启定位")
        map.setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null))
        initMap()//初始化地图
        initView()//初始化视图
    }

    //初始化视图
    private fun initView() {
        gotoMyLoc.setOnClickListener { backToMyLoc(map.mapStatus.zoom) }
    }

    private fun initMap() {
        map.setOnMapStatusChangeListener(mapStateChangeListener)//地图状态变化监听
        map.setOnMapClickListener(onMapClickListener)//地图点击监听
    }

    //回到定位位置
    private fun backToMyLoc(zoom: Float) {
        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                MapStatus.Builder()
                        .target(LatLng(curPoint.latitude, curPoint.longitude))
                        .zoom(zoom).build()))
    }

    @Subscribe
    fun addHeatMap(list: ArrayList<LatLng>) {
        Log.d(Tagg, "$list")
        heatMap = HeatMap.Builder().data(list).build()
    }

    override fun onResume() {
        mapView.onResume()
        //为系统的方向传感器注册监听器
        senorManager.registerListener(this, senorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI)
        super.onResume()
    }

    override fun onDestroy() {
        locClient.stop()// 退出时销毁定位
        map.isMyLocationEnabled = false// 关闭定位图层
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        senorManager.unregisterListener(this)//取消注册传感器监听
        EventBus.getDefault().unregister(this)//取消订阅
        super.onStop()
    }

}
