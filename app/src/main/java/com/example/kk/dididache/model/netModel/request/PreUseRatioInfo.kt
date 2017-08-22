package com.example.kk.dididache.model.netModel.request

import com.example.kk.dididache.App
import com.example.kk.dididache.getTimeNow
import com.example.kk.dididache.toStr
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/22.
 */
class PreUseRatioInfo(var x: Double, var y: Double, var timeStart: String, var timeEnd: String, var timeNow: String = { val now = Calendar.getInstance().getTimeNow();now.toStr() }())
