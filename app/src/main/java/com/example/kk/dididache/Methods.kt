package com.example.kk.dididache

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.animation.Interpolator
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.model.netModel.response.TaxiCount
import com.example.kk.dididache.model.netModel.response.UseRatio
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import org.jetbrains.anko.*
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
    val days = getToday()
    c.add(Calendar.DATE, -days)
    c.add(Calendar.HOUR_OF_DAY, -5)
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

fun getIpPort(): String {
    val p = App.instance.getSharedPreferences("ipPort", Context.MODE_PRIVATE)
    return p.getString("ipPort", "0.0.0.0:0")
}

fun setIpPort(ipPort: String) {
    val e = App.instance.getSharedPreferences("ipPort", Context.MODE_PRIVATE).edit()
    e.putString("ipPort", ipPort)
    e.apply()
}

fun getToday(): Int {
    val p = App.instance.getSharedPreferences("Today", Context.MODE_PRIVATE)
    return p.getInt("Today", 201)
}

fun setToday(days: Int) {
    val e = App.instance.getSharedPreferences("Today", Context.MODE_PRIVATE).edit()
    e.putInt("Today", days)
    e.apply()
}

fun getTimeOut(): Int {
    val p = App.instance.getSharedPreferences("TimeOut", Context.MODE_PRIVATE)
    return p.getInt("TimeOut", 10)
}

fun setTimeOut(timeOut: Int) {
    val e = App.instance.getSharedPreferences("TimeOut", Context.MODE_PRIVATE).edit()
    e.putInt("TimeOut", timeOut)
    e.apply()
}

fun showSetIpPortDialog(context: Context) {
    context.alert {
        customView {
            val e = editText {
                hint = "ip:port："+getIpPort()
            }
            yesButton { setIpPort(e.text.toString()) }
            noButton { }
        }
    }.show()
}

fun getLocTime(): Int {
    val p = App.instance.getSharedPreferences("LocTime", Context.MODE_PRIVATE)
    return p.getInt("LocTime", 10)
}

fun setLocTime(locTime: Int) {
    val e = App.instance.getSharedPreferences("LocTime", Context.MODE_PRIVATE).edit()
    e.putInt("LocTime", locTime)
    e.apply()
}

fun getheatTime(): Int {
    val p = App.instance.getSharedPreferences("heatTime", Context.MODE_PRIVATE)
    return p.getInt("heatTime", 10)
}

fun setheatTime(heatTime: Int) {
    val e = App.instance.getSharedPreferences("heatTime", Context.MODE_PRIVATE).edit()
    e.putInt("heatTime", heatTime)
    e.apply()
}

fun getpreHeatTime(): Int {
    val p = App.instance.getSharedPreferences("preHeatTime", Context.MODE_PRIVATE)
    return p.getInt("preHeatTime", 10)
}

fun setpreHeatTime(preHeatTime: Int) {
    val e = App.instance.getSharedPreferences("preHeatTime", Context.MODE_PRIVATE).edit()
    e.putInt("preHeatTime", preHeatTime)
    e.apply()
}

fun showSetLocTimeDialog(context: Context) {
    context.alert {
        customView {
            verticalLayout {
                val loctime = editText {
                    hint = "请求间隔(ms)："+getLocTime().toString()
                }
                val heatTime = editText {
                    hint = "动态时长(s)："+ getheatTime().toString()
                }
                val preHeatTime = editText {
                    hint = "静态半时长(s)："+ getpreHeatTime().toString()
                }
                yesButton {
                    setLocTime(loctime.text.toString().toInt())
                    setheatTime(heatTime.text.toString().toInt())
                    setpreHeatTime(preHeatTime.text.toString().toInt())
                }
                noButton { }
            }

        }
    }.show()
}

fun showSetTodayDialog(context: Context) {
    context.alert {
        customView {
            val e = editText {
                hint = "减去天数："+getToday().toString()
            }
            yesButton { setToday(e.text.toString().toInt()) }
            noButton { }
        }
    }.show()
}

fun showSetTimeOutDialog(context: Context) {
    context.alert {
        customView {
            val e = editText {
                hint = "超时时长(s)："+getTimeOut().toString()
            }
            yesButton { setTimeOut(e.text.toString().toInt()) }
            noButton { }
        }
    }.show()
}

fun getLineDate(list: ArrayList<TaxiCount>): LineData {
    val d = LineData()
    val entries = (0 until list.size).map { Entry(it.toFloat(), (list[it].taxiCount).toFloat()) }
    val set = LineDataSet(entries, "Line")
    set.color = 0xff00ffff.toInt()
    set.lineWidth = 1.5f
    set.setCircleColor(0x9900ffff.toInt())
    set.setCircleColorHole(0xff00ffff.toInt())
    set.circleHoleRadius = 2F
    set.circleRadius = 4F
    set.setDrawValues(false)
    d.addDataSet(set)
    return d
}

fun getBarData(list: ArrayList<TaxiCount>): BarData {
    val d = BarData()
    val entries = (0 until list.size).map { BarEntry(it.toFloat(), (list[it].taxiCount).toFloat()) }
    val set = BarDataSet(entries, "Bar")
    set.color = 0xff3b5c9a.toInt()
    d.addDataSet(set)
    d.barWidth = 0.55F
    return d
}

fun getPieData(useRatio: UseRatio): PieData {
    val entries = mutableListOf<PieEntry>()
    val colors = mutableListOf<Int>()
    val used = useRatio.taxiUse.toFloat() / useRatio.taxiSum.toFloat() * 100

    entries.add(PieEntry(used, "已载客"))
    entries.add(PieEntry(100 - used, "空 车"))

    val dataSet = PieDataSet(entries, "")
    dataSet.sliceSpace = 5F
    dataSet.selectionShift = 5F

    colors.add(0xff4da8ec.toInt())
    colors.add(0xff85c8f3.toInt())
    dataSet.colors = colors
    val pieData = PieData(dataSet)
    pieData.setValueFormatter(PercentFormatter())
    pieData.setValueTextSize(8F)
    pieData.setValueTextColor(Color.WHITE)
    pieData.setValueTypeface(App.mTfLight)
    return pieData
}

//默认半径0.001
fun LatLng.isInRadius(center: LatLng, radius: Double = 0.001): Boolean = (latitude - center.latitude) * (latitude - center.latitude) + (longitude - center.longitude) * (longitude - center.longitude) < radius * radius

//果冻回弹插值器
class MyOverShootInterpolator(val factor: Double) : Interpolator {
    constructor() : this(0.3)

    override fun getInterpolation(p0: Float): Float {
        return (Math.pow(2.0, (-5 * p0).toDouble()) * Math.sin((p0 - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
    }

}