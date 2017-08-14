package com.example.kk.dididache.model;

/**
 * Created by 小吉哥哥 on 2017/8/14.
 */

public class DataKeeper {
    private static DataKeeper keeper;
    private LatLongList heatPoints;

    public static DataKeeper getInstance() {
        if (keeper == null) {
            keeper = new DataKeeper();
        }
        return keeper;
    }

    public LatLongList getHeatPoints() {
        return heatPoints;
    }

    public void setHeatPoints(LatLongList latLongList) {
        this.heatPoints = latLongList;
    }
}
