package com.example.kk.dididache.model.netModel

import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.toStr
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/15.
 */

data class TaxiCountInfo(var longitude: Double, var latitude: Double, var timeStart: String, var timeEnd: String, var isFuture: Int, var barCount: Int = 9) {
    constructor(latLng: LatLng, timeStart: Calendar, timeEnd: Calendar, isFuture: Int) : this(latLng.longitude, latLng.latitude, timeStart.toStr(), timeEnd.toStr(), isFuture)
}