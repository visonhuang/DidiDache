package com.example.kk.dididache.model.Event

import com.example.kk.dididache.model.netModel.response.CarCountInXY
import com.example.kk.dididache.model.netModel.response.DriveTime
import com.example.kk.dididache.model.netModel.response.Exception
import com.example.kk.dididache.model.netModel.response.TaxiCount
import com.example.kk.dididache.model.netModel.response.UseRatio

/**
 * Created by 小吉哥哥 on 2017/8/18.
 */
data class HeatMapEvent(var list: ArrayList<CarCountInXY>)//静态热力图事件

data class TaxiCountEvent(var list: ArrayList<TaxiCount>)//流量变化事件

data class UseRatioEvent(var useRatio: UseRatio)//利用率事件

data class DriveTimeEvent(var driveTime: DriveTime)//路程规划事件

data class ExceptionEvent(var exceptions: ArrayList<Exception>)//异常事件