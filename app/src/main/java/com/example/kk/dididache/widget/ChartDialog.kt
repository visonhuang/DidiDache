package com.example.kk.dididache.widget

import android.animation.Animator
import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.MyOverShootInterpolator
import com.example.kk.dididache.R
import com.example.kk.dididache.model.DataKeeper
import com.example.kk.dididache.model.Event.TaxiCountEvent
import com.example.kk.dididache.model.Http
import com.example.kk.dididache.model.netModel.response.TaxiCount
import com.example.kk.dididache.model.netModel.request.TaxiCountInfo
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
    val cancelButton: FloatingActionButton by lazy { (context as MainActivity).find<FloatingActionButton>(R.id.cancelButton) }
    val detailButton: FloatingActionButton by lazy { (context as MainActivity).find<FloatingActionButton>(R.id.detailButton) }
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

        chart.zoom(0F, 0F, 0F, 0F)
        animate(true)
    }

    fun dismiss() {
        EventBus.getDefault().unregister(this)
        context = null//释放context
        animate(false)
    }


    //收到数据
    @Subscribe
    fun setData(event: TaxiCountEvent) {
        if (event.list.isEmpty()) return
        val data = CombinedData()
        data.setData(getBarData(event.list))
        data.setData(getLineDate(event.list))
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
        Http.getInstance().doPost(Http.ADRESS.carCountChange, TaxiCountInfo(LatLng(0.0, 0.0), start, end, 0))
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
            contentView.visibility = View.VISIBLE
            scrim.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            detailButton.visibility = View.VISIBLE
            scrim.alpha = 0f
            scrim.animate()
                    .alpha(1F)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(300)
                    .start()
            cancelButton.translationX = -400F
            cancelButton.animate()
                    .translationX(0F)
                    .setInterpolator(MyOverShootInterpolator())
                    .setDuration(300)
                    .start()

            detailButton.translationX = 400F
            detailButton.animate()
                    .translationX(0F)
                    .setInterpolator(MyOverShootInterpolator())
                    .setDuration(300)
                    .start()
            contentView.translationY = -400F
            contentView.alpha = 0F
            contentView.animate()
                    .translationY(0F)
                    .alpha(1F)
                    .setDuration(300)
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
        } else {
            scrim.animate()
                    .alpha(0F)
                    .setDuration(300)
                    .start()
            cancelButton.animate()
                    .translationX(-400F)
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()

            detailButton.animate()
                    .translationX(400F)
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            contentView.animate()
                    .translationY(-400F)
                    .alpha(0F)
                    .setDuration(300)
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
                    cancelButton.visibility = View.GONE
                    detailButton.visibility = View.GONE
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
        chart.axisLeft.axisLineWidth = 2F
        chart.axisLeft.axisMinimum = 0F
        chart.axisLeft.granularity = 1F
        chart.axisLeft.textColor = 0xff626161.toInt()
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM//将x轴放在下面
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.granularity = 1F
        chart.xAxis.textColor = 0xff626161.toInt()
        chart.xAxis.axisLineWidth = 2F
        chart.xAxis.setValueFormatter { value, _ -> xAxis[value.toInt() % 9] }
    }

}
