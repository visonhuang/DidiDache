package com.example.kk.dididache.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng

import com.example.kk.dididache.R
import com.example.kk.dididache.Tagg
import com.example.kk.dididache.model.Point
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val locClient by lazy { LocationClient(this) }

    private val map by lazy { mapView.map }
    //传感器管理
    private val senorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    //定位监听
    private val locationListener by lazy { MyLocationListener() }
    //当前经纬度
    private var curPoint: LatLng = LatLng(0.0, 0.0)
        get() = LatLng(locData?.latitude ?: 0.0, locData?.longitude ?: 0.0)
    //坐标信息

    private var locData: MyLocationData? = null
    private var curDirection = 0
    private var lastX: Double = 0.0
    private var isFirstLoc = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLoc()//初始化定位
    }

    //SensorEventListener
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //SensorEventListener
    override fun onSensorChanged(p0: SensorEvent) {
        Log.d(Tagg, "传感器数据变化")
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

    //定位监听器
    inner class MyLocationListener : BDLocationListener {

        override fun onConnectHotSpotMessage(p0: String?, p1: Int) {
            //热点
        }

        override fun onReceiveLocation(p0: BDLocation?) {
            Log.d(Tagg, "收到定位,${p0?.locType} and ${p0?.longitude}")
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
                map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                        MapStatus.Builder()
                                .target(LatLng(p0.latitude, p0.longitude))
                                .zoom(18.0F).build()))
            }
        }
    }

    override fun onResume() {
        mapView.onResume()
        //为系统的方向传感器注册监听器
        senorManager.registerListener(this, senorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI)
        super.onResume()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        //取消注册传感器监听
        senorManager.unregisterListener(this)
        super.onStop()
    }

    private fun initLoc() {
        //注册定位监听器
        try {
            map.isMyLocationEnabled = true
            locClient.registerLocationListener(locationListener)
            val option = LocationClientOption()
            option.isOpenGps = true//开gps
            option.coorType = "bd09ll"//设置坐标类型
            option.scanSpan = 1000//扫描速度
            locClient.locOption = option
            locClient.start()
            Log.d(Tagg, "开启定位")
            map.setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, null))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
