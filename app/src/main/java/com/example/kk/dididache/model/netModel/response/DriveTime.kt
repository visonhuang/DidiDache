package com.example.kk.dididache.model.netModel.response

import com.example.kk.dididache.toCalender
import com.example.kk.dididache.toStr

/**
 * Created by 小吉哥哥 on 2017/8/18.
 * "driveTime": "耗时",          // 字符串类型
"time": "到达时间",   	    // 字符串类型
"index": "路径数组的下标"      // int类型
 */
data class DriveTime(var driveTime: String, var time: String, var index: Int) {

    fun timeToCalender() = time.toCalender()
    //按格式显示到达时间
    fun formatTime(format: String) = time.toCalender().toStr(format)
}