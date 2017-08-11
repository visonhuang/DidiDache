package com.example.kk.dididache

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import kotlin.properties.Delegates

/**
 * Created by 小吉哥哥 on 2017/8/11.
 */
class App : Application() {
    //单例化App
    companion object {
        val mainThreadHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
        var instance: App by Delegates.notNull()
        var mtoast: Toast? = null
        var mMsg: Any? = null
    }

    override fun onCreate() {
        initMapSdk(this)
        super.onCreate()
        instance = this
    }

    //初始化map
    private fun initMapSdk(ctx: Context) {
        SDKInitializer.initialize(ctx)
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }
}