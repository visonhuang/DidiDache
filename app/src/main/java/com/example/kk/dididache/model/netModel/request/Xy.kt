package com.example.kk.dididache.model.netModel.request

import com.baidu.mapapi.model.LatLng

/**
 * Created by 小吉哥哥 on 2017/8/18.
 */
data class Xy(var x: Double, var y: Double) {

    constructor(latLng: LatLng) : this(latLng.longitude, latLng.latitude)

    fun toLatLng() = LatLng(y, x)

}