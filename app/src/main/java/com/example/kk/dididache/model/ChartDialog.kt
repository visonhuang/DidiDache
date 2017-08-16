package com.example.kk.dididache.model

import android.animation.Animator
import android.support.v7.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Button
import android.widget.LinearLayout
import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.R
import com.example.kk.dididache.ui.MainActivity
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*

/**
 * Created by 小吉哥哥 on 2017/8/15.
 */

class ChartDialog(var context: Context?) {

    constructor(context: Context?, build: ChartDialog.() -> Unit) : this(context) {
        this.build()
        insideBuild()
    }

    var contentView: View = View(context)//表容器
    val scrim: View by lazy { (context as MainActivity).find<View>(R.id.scrim) }//遮罩
    val chart: CombinedChart by lazy { contentView.find<CombinedChart>(R.id.chart) }//表
    val cancelButton: Button by lazy { contentView.find<Button>(R.id.cancelButton) }
    val detailButton: Button by lazy { contentView.find<Button>(R.id.detailButton) }
    val underChartLinear: LinearLayout by lazy { contentView.find<LinearLayout>(R.id.underChartLinear) }
    var _chartClick: (View) -> Unit = {}//点击表
    var _dismiss: () -> Unit = {}//dialog消失
    var _detail: ChartDialog.() -> Unit = {}//点击详情
    var _cancel: ChartDialog.() -> Unit = {}//点击取消

    var isShowing: Boolean = false
        get() = contentView.visibility == View.VISIBLE

    fun onChartClick(c: (View) -> Unit) {
        _chartClick = c
    }

    fun onDismiss(d: () -> Unit) {
        _dismiss = d
    }

    fun onDetail(d: ChartDialog.() -> Unit) {
        _detail = d
    }

    fun onCancel(c: ChartDialog.() -> Unit) {
        _cancel = c
    }

    private fun insideBuild() {
        contentView = (context as MainActivity).find(R.id.chartContainer)
        scrim.onClick { dismiss() }
        chart.onClick { _chartClick(chart) }//设置表点击事件监听
        detailButton.onClick { _detail() }
        cancelButton.onClick {
            dismiss()
            _cancel()
        }
    }

    fun show() {
        EventBus.getDefault().register(this)
        getDate()//发出网络请求
        contentView.visibility = View.VISIBLE
        scrim.visibility = View.VISIBLE
        contentView.alpha = 0F
        contentView.scaleX = 0.8F
        contentView.scaleY = 0.8F
        contentView.animate()
                .scaleX(1F)
                .scaleY(1F)
                .alpha(1F)
                .setDuration(500)
                .setInterpolator(MyOverShootInterpolator()).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {

            }
        }).start()

    }

    fun dismiss() {
        EventBus.getDefault().unregister(this)
        context = null//释放context
        contentView.animate()
                .scaleX(0.5F)
                .scaleY(0.5F)
                .alpha(0F)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator()).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                contentView.visibility = View.GONE
                scrim.visibility = View.GONE
                _dismiss()
            }
        }).start()

    }


    //收到数据
    @Subscribe
    fun setData(list: ArrayList<TaxiCount>) {
        val data = CombinedData()
        data.setData(getBarData(list))
        data.setData(getLineDate(list))
        DataKeeper.getInstance().combinedData = data//存放数据
        chart.data = data
        chart.invalidate()
    }

    //发出网络请求
    fun getDate() {
        val time = Calendar.getInstance()
        time.set(2017, 2, 28, 0, 0, 1)
        Http.getInstance().getTaxiCountByTime(TaxiCountInfo(LatLng(0.0, 0.0), time, "past"))
    }

    private fun getLineDate(list: ArrayList<TaxiCount>): LineData {
        val d = LineData()
        val entries = (0 until list.size).map { Entry(it.toFloat(), (list[it].taxiCount).toFloat()) }
        val set = LineDataSet(entries, "Line")
        set.color = 0xfffaf852.toInt()
        d.addDataSet(set)
        return d
    }

    private fun getBarData(list: ArrayList<TaxiCount>): BarData {
        val d = BarData()
        val entries = (0 until list.size).map { BarEntry(it.toFloat(), (list[it].taxiCount).toFloat()) }
        val set = BarDataSet(entries, "Bar")
        set.color = 0xff47ee5c.toInt()
        d.addDataSet(set)
        return d
    }

    //回弹插值器
    class MyOverShootInterpolator(val factor: Double) : Interpolator {
        constructor() : this(0.3)

        override fun getInterpolation(p0: Float): Float {
            return (Math.pow(2.0, (-5 * p0).toDouble()) * Math.sin((p0 - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
        }

    }

}
