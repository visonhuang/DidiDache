package com.example.kk.dididache.widget

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.baidu.mapapi.model.LatLng
import com.example.kk.dididache.App
import com.example.kk.dididache.MyOverShootInterpolator
import com.example.kk.dididache.R
import com.example.kk.dididache.control.adapter.ChartAdapter
import com.example.kk.dididache.model.DataKeeper
import com.example.kk.dididache.model.Event.TaxiCountEvent
import com.example.kk.dididache.model.Event.UseRatioEvent
import com.example.kk.dididache.model.Http
import com.example.kk.dididache.model.netModel.response.TaxiCount
import com.example.kk.dididache.model.netModel.request.TaxiCountInfo
import com.example.kk.dididache.model.netModel.request.UseRatioInfo
import com.example.kk.dididache.toStr
import com.example.kk.dididache.ui.MainActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
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

    var viewPager = (context as MainActivity).viewPager
    var indicator = (context as MainActivity).indicator
    var contentView: View = View(context)//表容器
    val scrim: View by lazy { (context as MainActivity).find<View>(R.id.scrim) }//遮罩
    val combinedChart: CombinedChart = CombinedChart(context)//by lazy { contentView.combinedChart }//表
    val pieChart: PieChart = PieChart(context)
    val cancelButton: ImageView by lazy { (context as MainActivity).find<ImageView>(R.id.cancelButton) }
    val detailButton: ImageView by lazy { (context as MainActivity).find<ImageView>(R.id.detailButton) }
    val underChartLinear: LinearLayout by lazy { contentView.find<LinearLayout>(R.id.underChartLinear) }
    var _chartClick: () -> Unit = {}//点击表
    var _dismiss: () -> Unit = {}//dialog消失
    var _detail: ChartDialog.() -> Unit = {}//点击详情
    var _cancel: ChartDialog.() -> Unit = {}//点击取消
    var xAxis = mutableListOf<String>()
    var isShowing: Boolean = false
        get() = contentView.visibility == View.VISIBLE
    var hasException = false
        set(value) {
            if (value) underChartLinear.visibility = View.VISIBLE
            else {
                underChartLinear.visibility = View.GONE
                DataKeeper.getInstance().exception = null
            }
        }

    fun onChartClick(c: () -> Unit) {
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

        /******实验*******/
        viewPager.adapter = ChartAdapter(combinedChart, pieChart)
        indicator.setViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
//                DataKeeper.getInstance().page = position
//                when (position) {
//                    0 -> animateCombinedChart()
//                    else -> animatePieChart()
//                }
            }
        })
        /********实验*******/

        scrim.onClick { dismiss() }
        combinedChart.onClick { _chartClick() }//设置表点击事件监听
        pieChart.setOnClickListener { _chartClick() }
        pieChart.onClick { _chartClick() }
        detailButton.onClick { _detail() }
        cancelButton.onClick {
            dismiss()
            _cancel()
        }
    }

    fun show(time: Calendar) {
        EventBus.getDefault().register(this)
        setChartOptions(combinedChart, time)
        getDate(time)//发出网络请求
        combinedChart.zoom(0F, 0F, 0F, 0F)
        animate(true)
    }

    fun dismiss() {
        EventBus.getDefault().unregister(this)
        context = null//释放context
        animate(false)
    }


    //收到数据
    @Subscribe
    fun setTaxiCountData(event: TaxiCountEvent) {
        if (event.list.isEmpty()) return
        val data = CombinedData()
        data.setData(getBarData(event.list))
        data.setData(getLineDate(event.list))
        DataKeeper.getInstance().combinedData = data//存放数据
        combinedChart.data = data
        animateCombinedChart()
    }


    @Subscribe
    fun setUseRatioData(event: UseRatioEvent) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        val used = event.useRatio.taxiUse.toFloat() / event.useRatio.taxiSum.toFloat() * 100

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
        DataKeeper.getInstance().pieData = pieData
        pieChart.data = pieData
        animatePieChart()
    }

    //发出网络请求
    fun getDate(time: Calendar) {
        val start = time.clone() as Calendar
        val end = time.clone() as Calendar
        start.add(Calendar.MINUTE, -60)
        end.add(Calendar.MINUTE, 60)
        Http.getInstance().doPost(Http.ADRESS.carCountChange, TaxiCountInfo(LatLng(0.0, 0.0), start, end, 0))
        Http.getInstance().doPost(Http.ADRESS.useRatio, UseRatioInfo(0.0, 0.0, start.toStr(), end.toStr(), 0))
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
            //波澜动画
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                ViewAnimationUtils
//                        .createCircularReveal(scrim, (scrim.left + scrim.right) / 2, (scrim.top + scrim.bottom) / 2, 0F, Math.max(scrim.width, scrim.height).toFloat())
//                        .start()
//            }
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


    fun animateCombinedChart() = combinedChart.animateY(1000, Easing.EasingOption.EaseInQuad)
    fun animatePieChart() = pieChart.animateY(1000, Easing.EasingOption.EaseInQuad)

    private fun setChartOptions(combinedChart: CombinedChart, time: Calendar) {
        //设置x轴
        val p0 = time.clone() as Calendar
        p0.add(Calendar.MINUTE, -60)
        xAxis.clear()
        for (i in 0..8) {
            xAxis.add(p0.toStr("HH:mm"))
            p0.add(Calendar.MINUTE, 15)
        }
        combinedChart.description.isEnabled = true//去掉注释
        combinedChart.description.text = "车流量变化图"
        combinedChart.legend.isEnabled = false//去调颜色标注
        combinedChart.axisRight.isEnabled = false //去掉右边y轴
        combinedChart.axisLeft.setDrawGridLines(true)
        combinedChart.axisLeft.axisLineWidth = 2F
        combinedChart.axisLeft.axisMinimum = 0F
        combinedChart.axisLeft.granularity = 1F
        combinedChart.axisLeft.textColor = 0xff626161.toInt()
        combinedChart.xAxis.position = XAxis.XAxisPosition.BOTTOM//将x轴放在下面
        combinedChart.xAxis.axisMinimum = 0f
        combinedChart.xAxis.granularity = 1F
        combinedChart.xAxis.textColor = 0xff626161.toInt()
        combinedChart.xAxis.axisLineWidth = 2F
        combinedChart.xAxis.setValueFormatter { value, _ -> xAxis[value.toInt() % 9] }

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        //pieChart.setExtraOffsets(5F, 10F, 5F, 5F)
        pieChart.dragDecelerationFrictionCoef = 0.95F
        pieChart.setCenterTextTypeface(App.mTfLight)
        pieChart.centerText = genText()
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58F
        pieChart.transparentCircleRadius = 61F
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTypeface(App.mTfRegular)
        pieChart.setEntryLabelTextSize(7f)

    }

    private fun genText(): SpannableString {
        val s = SpannableString("出租车载客率\npowered by QG Studio")
        s.setSpan(RelativeSizeSpan(.7f), 0, 6, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 6, s.length - 9, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 6, s.length - 9, 0)
        s.setSpan(RelativeSizeSpan(.5f), 6, s.length - 9, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 9, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 9, s.length, 0)
        return s
    }
}
