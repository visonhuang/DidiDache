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
import com.example.kk.dididache.toStr
import com.example.kk.dididache.ui.MainActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
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
    var xAxis = mutableListOf<String>()
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

    fun show(time: Calendar) {
        EventBus.getDefault().register(this)
        setChartOptions(time)
        getDate(time)//发出网络请求
        contentView.visibility = View.VISIBLE
        scrim.visibility = View.VISIBLE
        chart.zoom(0F,0F,0F,0F)
        animate(true)
    }

    fun dismiss() {
        EventBus.getDefault().unregister(this)
        context = null//释放context
        animate(false)
    }


    //收到数据
    @Subscribe
    fun setData(list: ArrayList<TaxiCount>) {
        val data = CombinedData()
        data.setData(getBarData(list))
        data.setData(getLineDate(list))
        DataKeeper.getInstance().combinedData = data//存放数据
        chart.data = data
        animateChart()
    }

    //发出网络请求
    fun getDate(time: Calendar) {
        val start = time.clone() as Calendar
        val end = time.clone() as Calendar
        start.add(Calendar.MINUTE, -60)
        end.add(Calendar.MINUTE, 60)
        Http.getInstance().getTaxiCountByTime(TaxiCountInfo(LatLng(0.0, 0.0), start, end, 0))
    }

    private fun getLineDate(list: ArrayList<TaxiCount>): LineData {
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

    private fun getBarData(list: ArrayList<TaxiCount>): BarData {
        val d = BarData()
        val entries = (0 until list.size).map { BarEntry(it.toFloat(), (list[it].taxiCount).toFloat()) }
        val set = BarDataSet(entries, "Bar")
        set.color = 0xff3b5c9a.toInt()
        d.addDataSet(set)
        d.barWidth = 0.55F
        return d
    }

    /**
     *过度动画，isshow = true则播放出现动画，否则播放消失动画
     */
    fun animate(isShow: Boolean) {
        if (isShow) {
            cancelButton.scaleX = 0F
            cancelButton.scaleY = 0F
            cancelButton.animate()
                    .scaleX(1F)
                    .scaleY(1F)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(200)
                    .setStartDelay(100)
                    .start()

            detailButton.scaleX = 0F
            detailButton.scaleY = 0F
            detailButton.animate()
                    .scaleX(1F)
                    .scaleY(1F)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(200)
                    .setStartDelay(150)
                    .start()

            contentView.alpha = 0F
            contentView.scaleX = 0.8F
            contentView.scaleY = 0.8F
            contentView.animate()
                    .scaleX(1F)
                    .scaleY(1F)
                    .alpha(1F)
                    .setDuration(200)
                    .setInterpolator(AccelerateDecelerateInterpolator()).setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationStart(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {

                }
            }).start()
        } else {
            cancelButton.animate()
                    .scaleX(0F)
                    .setDuration(200)
                    .start()

            detailButton.animate()
                    .scaleX(0F)
                    .setDuration(200)
                    .start()
            contentView.animate()

                    .scaleY(0F)
                    .alpha(0F)
                    .setDuration(200)
                    .setStartDelay(70)
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
    }


    fun animateChart() = chart.animateY(1000, Easing.EasingOption.EaseInQuad)

    private fun setChartOptions(time: Calendar) {
        //设置x轴
        val p0 = time.clone() as Calendar
        p0.add(Calendar.MINUTE, -60)
        xAxis.clear()
        for (i in 0..8) {
            xAxis.add(p0.toStr("HH:mm"))
            p0.add(Calendar.MINUTE, 15)
        }
        chart.description.isEnabled = false//去掉注释
        chart.legend.isEnabled = false//去调颜色标注
        chart.axisRight.isEnabled = false //去掉右边y轴
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.axisMinimum = 0F
        chart.axisLeft.granularity = 1F
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM//将x轴放在下面
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.granularity = 1F
        chart.xAxis.setValueFormatter { value, _ -> xAxis[value.toInt() % 9] }
    }

}
