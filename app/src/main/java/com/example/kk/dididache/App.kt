package com.example.kk.dididache

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer

import com.baidu.mapapi.map.MapView
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.litepal.LitePalApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by 小吉哥哥 on 2017/8/11.
 */
class App : Application() {
    //单例化App
    companion object {
        var instance: App by Delegates.notNull()
        //地图主题
        val PATH = "custom_config_blue.txt"
        val mTfLight by lazy { Typeface.createFromAsset(instance.assets, "OpenSans-Light.ttf") }
        val mTfRegular by lazy { Typeface.createFromAsset(instance.assets, "OpenSans-Regular.ttf") }
    }

    override fun onCreate() {
        Logger.addLogAdapter(AndroidLogAdapter())
        initMapSdk(this)
        LitePalApplication.initialize(applicationContext)
        super.onCreate()
        instance = this
    }

    //初始化map
    private fun initMapSdk(ctx: Context) {
        SDKInitializer.initialize(ctx)
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL)
        setMapCustomFile(this, PATH)

    }

    private fun setMapCustomFile(context: Context, PATH: String) {
        var out: FileOutputStream? = null
        var inputStream: InputStream? = null
        var moduleName: String? = null
        try {
            inputStream = context.assets
                    .open("customConfigdir/" + PATH)
            val b = ByteArray(inputStream!!.available())
            inputStream.read(b)

            moduleName = context.filesDir.absolutePath
            val f = File(moduleName + "/" + PATH)
            if (f.exists()) {
                f.delete()
            }
            f.createNewFile()
            out = FileOutputStream(f)
            out.write(b)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close()
                }
                if (out != null) {
                    out.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        Log.d(Tagg, moduleName + "/" + PATH)
        MapView.setCustomMapStylePath(moduleName + "/" + PATH)

    }
}