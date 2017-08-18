package com.example.kk.dididache.model.netModel.request

import com.baidu.mapapi.model.LatLng

/**
 * Created by 小吉哥哥 on 2017/8/18.
 */
data class ExceptionInfo(var timeStart: String, var timeEnd: String, var minX: Double, var minY: Double, var maxX: Double, var maxY: Double) {
    constructor(start: String, end: String, northeast: LatLng, southwest: LatLng) : this(start, end, southwest.longitude, southwest.latitude, northeast.longitude, northeast.latitude)
}
