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
import com.example.kk.dididache.showToast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    //定位相关
    private val locClient by lazy { LocationClient(this) }
    private val map by lazy { mapView.map }
    private val senorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }//传感器管理
    private val locationListener by lazy { MyLocationListener() } //定位监听
    private var curPoint: LatLng = LatLng(0.0, 0.0)//当前经纬度
        get() = LatLng(locData?.latitude ?: 0.0, locData?.longitude ?: 0.0)
    private var locData: MyLocationData? = null//坐标信息
    private var curDirection = 0
    private var lastX: Double = 0.0
    private var isFirstLoc = true


    override fun onCreate(savedInstanceState: Bundle?) {
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

    //定位监听器
    inner class MyLocationListener : BDLocationListener {
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
                map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                        MapStatus.Builder()
                                .target(LatLng(p0.latitude, p0.longitude))
                                .zoom(18.0F).build()))
            }
        }
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

}
