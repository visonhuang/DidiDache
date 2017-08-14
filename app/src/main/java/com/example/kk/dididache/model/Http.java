package com.example.kk.dididache.model;

import android.util.JsonReader;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.example.kk.dididache.MethodsKt;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Dispatcher;
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
    private static OkHttpClient client;
    private static Dispatcher dispatcher;
    private static int TAG_HEATPOINTS = 1;//请求热力点的tag

    public static Http getInstance() {
        if (http == null) http = new Http();
        if (client == null) client = new OkHttpClient();
        if (dispatcher == null) dispatcher = client.dispatcher();
        return http;
    }

    //请求热力点
    public void getHeatPoints(LatLngBounds bounds) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LatLongList latLngs;
                Request request = new Request.Builder()
                        .tag(TAG_HEATPOINTS)
                        .url("http://192.168.1.114:8080/gps/find")
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build();
                try {
                    //接收数据
                    Response response = client.newCall(request).execute();
                    Logger.d("接收热力点", "数据长度" + String.valueOf(response.body().contentLength()));
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    latLngs = new Gson().fromJson(reader, LatLongList.class);

                    //数据处理
                    DataKeeper.getInstance().setHeatPoints(latLngs);//保存到数据持有者
                    EventBus.getDefault().post(latLngs);
                } catch (IOException e) {
                    MethodsKt.showToast(Http.this, "热力点出现了一些问题");
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //取消请求
    public void cancelCall(int tag) {
        synchronized (dispatcher) {
            //取消队列中带tag的call
            for (Call call : dispatcher.queuedCalls()) {
                if (tag == (int) call.request().tag()) {
                    call.cancel();
                    Logger.i(call + "被取消");
                }
            }
            //取消正在请求中带有tag的call
            for (Call call : dispatcher.runningCalls()) {
                if (tag == (int) call.request().tag()) {
                    call.cancel();
                    Logger.i(call + "被取消");
                }
            }
        }
    }

    private static double nextDouble(final double min, final double max) {
        return min + ((max - min) * new Random().nextDouble());
    }
}
