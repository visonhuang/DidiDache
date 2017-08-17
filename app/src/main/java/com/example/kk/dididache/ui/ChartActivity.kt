package com.example.kk.dididache.ui

import android.os.Bundle

import com.example.kk.dididache.R
import com.example.kk.dididache.model.DataKeeper
import com.example.kk.dididache.toStr
import com.github.mikephil.charting.components.XAxis
import kotlinx.android.synthetic.main.activity_chart.*
import java.util.*

class ChartActivity : BaseActivity() {
    var xAxis = mutableListOf<String>()
    var time: Calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        time = intent.getSerializableExtra("time") as Calendar
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        bigChart.data = DataKeeper.getInstance().combinedData
        setChartOptions()
        bigChart.invalidate()
    }

    private fun setChartOptions() {
        //设置x轴
        val p0 = time.clone() as Calendar
        p0.add(Calendar.MINUTE, -60)
        xAxis.clear()
        for (i in 0..8) {
            xAxis.add(p0.toStr("HH:mm"))
            p0.add(Calendar.MINUTE, 15)
        }
        bigChart.description.isEnabled = false//去掉注释
        bigChart.legend.isEnabled = false//去调颜色标注
        bigChart.axisRight.isEnabled = false //去掉右边y轴
        bigChart.axisLeft.setDrawGridLines(true)
        bigChart.axisLeft.axisMinimum = 0F
        bigChart.axisLeft.granularity = 1F
        bigChart.xAxis.position = XAxis.XAxisPosition.BOTTOM//将x轴放在下面
        bigChart.xAxis.axisMinimum = 0f
        bigChart.xAxis.granularity = 1F
        bigChart.xAxis.setValueFormatter { value, _ -> xAxis[value.toInt() % 9] }
    }
}
