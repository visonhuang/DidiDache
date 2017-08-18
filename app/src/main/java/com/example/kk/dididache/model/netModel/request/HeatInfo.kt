package com.example.kk.dididache.model.netModel.request

import com.baidu.mapapi.model.LatLng
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/14.
 * 未来和过去的热力图都用这个来请求
 */
data class HeatInfo(var timeStart: String, var timeEnd: String, var minX: Double, var minY: Double, var maxX: Double, var maxY: Double) {
    companion object {
        var lastTime: String? = null
    }
    constructor(start: String,end: String,northeast:LatLng,southwest:LatLng):this(start,end,southwest.longitude,southwest.latitude,northeast.longitude,northeast.latitude)
}