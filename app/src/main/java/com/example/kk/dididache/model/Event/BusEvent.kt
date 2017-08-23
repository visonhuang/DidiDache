package com.example.kk.dididache.model.Event

import com.example.kk.dididache.model.netModel.response.*

/**
* Created by 小吉哥哥 on 2017/8/18.
*/
data class HeatMapEvent(var list: ArrayList<CarCountInXY>?, var state: Int) {
    constructor(feedBack: ArrayFeedBack<CarCountInXY>) : this(feedBack.data, feedBack.state)
}//静态热力图事件

data class TaxiCountEvent(var list: ArrayList<TaxiCount>?, var state: Int) {
    constructor(feedBack: ArrayFeedBack<TaxiCount>) : this(feedBack.data, feedBack.state)
}//流量变化事件

data class UseRatioEvent(var useRatio: UseRatio?, var state: Int) {
    constructor(feedBack: ObjectFeedBack<UseRatio>) : this(feedBack.data, feedBack.state)
}//利用率事件

data class DriveTimeEvent(var driveTime: DriveTime?, var state: Int) {
    constructor(feedBack: ObjectFeedBack<DriveTime>) : this(feedBack.data, feedBack.state)
}//路程规划事件

data class ExceptionEvent(var exceptions: ArrayList<Exception>?, var state: Int) {
    constructor(feedBack: ArrayFeedBack<Exception>) : this(feedBack.data, feedBack.state)
}//异常事件