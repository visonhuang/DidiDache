package com.example.kk.dididache.model;

import android.util.JsonReader;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.example.kk.dididache.MethodsKt;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 小吉哥哥 on 2017/8/12.
 */

public class Http {
    private static Http http;

    public static Http getInstance() {
        if (http == null) http = new Http();
        return http;
    }

    public void getCarsUnderBounds(LatLngBounds bounds) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<LatLng> latLngs;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://192.168.43.142:8080/test3.json")
                        .get()
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    Log.d(MethodsKt.getTagg(this), "数据长度"+String.valueOf(response.body().contentLength()));
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    latLngs = new Gson().fromJson(reader,LatLongList.class);
                    EventBus.getDefault().post(latLngs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Log.d(MethodsKt.getTagg(this), "生成随机点");

//        for (int i = 0; i < 1000000; i++) {
//            latLngs.add(new LatLng(nextDouble(bounds.southwest.latitude, bounds.northeast.latitude), nextDouble(bounds.southwest.longitude, bounds.northeast.longitude)));
//        }
//        EventBus.getDefault().post(latLngs);
//        Log.d(MethodsKt.getTagg(this), "生成随机点" + latLngs.size());
    }

    private static double nextDouble(final double min, final double max) {
        return min + ((max - min) * new Random().nextDouble());
    }
}
