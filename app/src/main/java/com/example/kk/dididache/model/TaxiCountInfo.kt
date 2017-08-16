package com.example.kk.dididache.model

import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.toStr
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/15.
 */

data class TaxiCountInfo(var longitude: Double, var latitude: Double, var time: String, var status: String) {
    constructor(latLng: LatLng, time: Calendar, status: String) : this(latLng.longitude, latLng.latitude, time.toStr(), status)
}