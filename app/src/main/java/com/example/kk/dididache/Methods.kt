package com.example.kk.dididache

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.animation.Interpolator
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*
import java.util.logging.Logger
import kotlin.properties.Delegates

/**
 * Created by 小吉哥哥 on 2017/8/11.
 */
object T {
    val mainThreadHandler: Handler by lazy { Handler(Looper.getMainLooper()) }//主线程
    var mtoast: Toast? = null
    var mMsg: Any? = null
}

fun Any.inUiThread(run: () -> Unit) = T.mainThreadHandler.post(run)

fun Any.showToast(msg: Any) {

    inUiThread {
        if (null == T.mtoast || msg.toString() !== T.mMsg!!.toString()) {
            if (null == T.mtoast) {
                T.mtoast = Toast.makeText(App.instance, msg.toString(), Toast.LENGTH_SHORT)
            } else {
                T.mtoast!!.setText(msg.toString())
            }
            T.mMsg = msg
        }
        T.mtoast?.show()
    }
}

fun Calendar.toStr(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val result = sdf.format(time)
    return result//"${get(Calendar.YEAR)}-${get(Calendar.MONTH)}-${get(Calendar.DATE)} ${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}:${get(Calendar.SECOND)}"
}

fun Calendar.toStr(format: String): String {
    val sdf = SimpleDateFormat(format)
    val result = sdf.format(time)
    return result
}

fun Calendar.getTimeNow(): Calendar {
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, -201)
    return c
}

fun String.toCalender(): Calendar {
    val c: Calendar = Calendar.getInstance()
    val info0 = split(" ")
    val info1 = info0[0].split("-")
    val info2 = info0[1].split(":")
    c.set(info1[0].toInt(), info1[1].toInt(), info1[2].toInt(), info2[0].toInt(), info2[1].toInt(), info2[2].toInt())
    return c
}

fun String.toCalender(format: String): Calendar {
    val c: Calendar = Calendar.getInstance()
    c.time = SimpleDateFormat(format).parse(this)
    return c
}

val Any.Tagg: String
    get() {
        val tag = this.javaClass.name.split(".")
        return tag[tag.size - 1].replace("$", "->") + "===="
    }
//默认半径0.001
fun LatLng.isInRadius(center: LatLng, radius: Double = 0.001): Boolean = latitude * latitude + longitude * longitude < radius * radius

//果冻回弹插值器
class MyOverShootInterpolator(val factor: Double) : Interpolator {
    constructor() : this(0.3)

    override fun getInterpolation(p0: Float): Float {
        return (Math.pow(2.0, (-5 * p0).toDouble()) * Math.sin((p0 - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
    }

}