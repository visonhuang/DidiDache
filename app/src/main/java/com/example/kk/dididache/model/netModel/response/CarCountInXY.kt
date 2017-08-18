package com.example.kk.dididache.model.netModel.response

import com.baidu.mapapi.map.WeightedLatLng
import com.baidu.mapapi.model.LatLng

/**
 * Created by 小吉哥哥 on 2017/8/18.
 */
data class CarCountInXY(var x: Double, var y: Double, var c: Int) : WeightedLatLng(LatLng(y, x), c.toDouble())
