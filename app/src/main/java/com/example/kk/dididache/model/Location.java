package com.example.kk.dididache.model;

import com.baidu.mapapi.model.LatLng;

import org.litepal.crud.DataSupport;

/**
 * Created by KK on 2017/8/24.
 */

public class Location extends DataSupport
{
    private String name;
    private double latitude;
    private double longtitude;

    private Location(){}

    public Location(String name, double latitude, double longtitude) {
        this.name = name;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public LatLng getLatLng(){
        return new LatLng(getLatitude(), getLongtitude());
    }
}
