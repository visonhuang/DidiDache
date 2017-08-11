package com.example.kk.dididache

import android.view.Gravity
import android.widget.Toast

/**
 * Created by 小吉哥哥 on 2017/8/11.
 */

fun Any.inUiThread(run: () -> Unit) = App.mainThreadHandler.post(run)

fun Any.showToast(msg: Any, gravity: Int = Gravity.BOTTOM or Gravity.CENTER) {

    inUiThread {
        if (null == App.mtoast || msg.toString() !== App.mMsg!!.toString()) {
            if (null == App.mtoast) {
                App.mtoast = Toast.makeText(App.instance, msg.toString(), Toast.LENGTH_SHORT)
            } else {
                App.mtoast!!.setText(msg.toString())
            }
            App.mtoast?.setGravity(gravity, 0, 0)
            App.mMsg = msg
        }
        App.mtoast?.show()
    }
}

val Any.Tagg: String
    get() {
        val tag = this.javaClass.name.split("\\.")
        return tag[tag.size - 1]+"===="
    }