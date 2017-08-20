package com.example.kk.dididache.control.adapter

import android.database.DataSetObserver
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.PieChart

/**
 * Created by 小吉哥哥 on 2017/8/20.
 */
class ChartAdapter(var combinedChart: CombinedChart, var pieChart: PieChart) : PagerAdapter() {

    override fun isViewFromObject(view: View?, `object`: Any?) = view == `object`

    override fun getCount() = 2

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?)
            = when (position) {
        0 -> container.removeView(combinedChart)
        else -> container.removeView(pieChart)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = when (position) {
            0 -> combinedChart
            else -> pieChart
        }

        container.addView(view)
        return view
    }
}