package com.example.kk.dididache.model

import com.baidu.location.BDLocation

/**
 * Created by 小吉哥哥 on 2017/8/11.
 */
data class Point(var lat: Double, var log: Double) {
    constructor(p0: BDLocation?) : this(p0?.latitude ?: 0.0, p0?.longitude ?: 0.0)

    fun copyData(p0: BDLocation) {
        lat = p0.latitude
        log = p0.longitude
    }
}