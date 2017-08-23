package com.example.kk.dididache.model.netModel.request

import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.App
import com.example.kk.dididache.getTimeNow
import com.example.kk.dididache.toStr
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/22.
 */
class PreTaxiCountInfo(var x: Double, var y: Double, var timeStart: String, var timeEnd: String, var timeNow: String, var barCount: Int = 10) {
    constructor(latLng: LatLng, timeStart: Calendar, timeEnd: Calendar) : this(latLng.longitude, latLng.latitude, timeStart.toStr(), timeEnd.toStr(), { val now = Calendar.getInstance().getTimeNow(); now.toStr() }())
}