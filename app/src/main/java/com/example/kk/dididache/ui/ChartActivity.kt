package com.example.kk.dididache.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.example.kk.dididache.R
import com.example.kk.dididache.model.DataKeeper
import com.github.mikephil.charting.data.CombinedData
import kotlinx.android.synthetic.main.activity_chart.*
import java.io.Serializable

class ChartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        bigChart.data = DataKeeper.getInstance().combinedData
        bigChart.invalidate()
    }
}
