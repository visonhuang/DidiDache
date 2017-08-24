package com.example.kk.dididache.model;


import com.example.kk.dididache.model.netModel.response.Exception;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.PieData;

import java.util.Calendar;

/**
 * Created by 小吉哥哥 on 2017/8/14.
 */

public class DataKeeper {
    private static DataKeeper keeper;
    private CombinedData combinedData;
    private PieData pieData;
    private Exception exception;
    private Calendar time;//传给其他活动的选中时间

    public Calendar getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Calendar timeStart) {
        this.timeStart = timeStart;
    }

    private Calendar timeStart;//传给C活动图表展示开始时间

    private int page; //图表展示页码

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public PieData getPieData() {
        return pieData;
    }

    public void setPieData(PieData pieData) {
        this.pieData = pieData;
    }

    public static DataKeeper getInstance() {
        if (keeper == null) {
            keeper = new DataKeeper();
        }
        return keeper;
    }


    public CombinedData getCombinedData() {
        return combinedData;
    }

    public void setCombinedData(CombinedData combinedData) {
        this.combinedData = combinedData;
    }
}
