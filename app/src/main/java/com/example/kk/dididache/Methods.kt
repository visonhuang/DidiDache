package com.example.kk.dididache

import android.util.Log
import android.view.Gravity
import android.widget.Toast
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/11.
 */

fun Any.inUiThread(run: () -> Unit) = App.mainThreadHandler.post(run)

fun Any.showToast(msg: Any) {

    inUiThread {
        if (null == App.mtoast || msg.toString() !== App.mMsg!!.toString()) {
            if (null == App.mtoast) {
                App.mtoast = Toast.makeText(App.instance, msg.toString(), Toast.LENGTH_SHORT)
            } else {
                App.mtoast!!.setText(msg.toString())
            }
            App.mMsg = msg
        }
        App.mtoast?.show()
    }
}

fun Calendar.toStr(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    add(Calendar.MONTH,-1)
    val result = sdf.format(time)
    add(Calendar.MONTH,1)
    return result//"${get(Calendar.YEAR)}-${get(Calendar.MONTH)}-${get(Calendar.DATE)} ${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}:${get(Calendar.SECOND)}"
}
fun String.toCalender():Calendar{
    val c:Calendar = Calendar.getInstance()
    val info0 = split(" ")
    val info1 = info0[0].split("-")
    val info2 = info0[1].split(":")
    c.set(info1[0].toInt(),info1[1].toInt(),info1[2].toInt(),info2[0].toInt(),info2[1].toInt(),info2[2].toInt())
    return c
}

val Any.Tagg: String
    get() {
        val tag = this.javaClass.name.split(".")
        return tag[tag.size - 1].replace("$", "->") + "===="
    }
