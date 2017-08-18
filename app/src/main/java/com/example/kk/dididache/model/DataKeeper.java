package com.example.kk.dididache.model;


import com.github.mikephil.charting.data.CombinedData;

/**
 * Created by 小吉哥哥 on 2017/8/14.
 */

public class DataKeeper {
    private static DataKeeper keeper;
    private CombinedData combinedData;

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
