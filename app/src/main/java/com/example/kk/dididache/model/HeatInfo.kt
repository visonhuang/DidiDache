package com.example.kk.dididache.model

import com.baidu.mapapi.model.LatLng
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/14.
 */
data class HeatInfo(var start: String, var end: String, var lng1: Double, var lat1: Double, var lng2: Double, var lat2: Double) {
    companion object {
        var lastTime: String? = null
    }
    constructor(start: String,end: String,northeast:LatLng,southwest:LatLng):this(start,end,southwest.longitude,southwest.latitude,northeast.longitude,northeast.latitude)
}