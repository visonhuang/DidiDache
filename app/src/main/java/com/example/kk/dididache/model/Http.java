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
    private OkHttpClient client;
    private Dispatcher dispatcher;
    public static final int TAG_HEATPOINTS = 1;//请求热力点的tag
    public static final int TAG_TAXICOUNT = 2;//请求柱状图的tag

    public static Http getInstance() {
        if (http == null) http = new Http();
        if (http.client == null) http.client = new OkHttpClient();
        if (http.dispatcher == null) http.dispatcher = http.client.dispatcher();
        return http;
    }

    //请求热力点
    public void getHeatPoints(final HeatInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LatLongList latLngs;
                MediaType mediaType = MediaType.parse("application/json");
                Logger.json(new Gson().toJson(info));
                RequestBody body = RequestBody.create(mediaType, new Gson().toJson(info));
                Request request = new Request.Builder()
                        .tag(TAG_HEATPOINTS)
                        .url("http://192.168.1.114:8080/gps/getdataforandroid")
                        .post(body)
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

    //请求某个坐标下一段时间内出租车变化情况
    public void getTaxiCountByTime(TaxiCountInfo info) {
        ArrayList<TaxiCount> list = new ArrayList<>();
        for (int i = 0; i < info.getBarCount(); i++) {
            list.add(new TaxiCount(new Random().nextInt(100)));
        }
        EventBus.getDefault().post(list);
    }

    //取消请求
    public void cancelCall(int tag) {
        synchronized (dispatcher) {
            //取消队列中带tag的call
            for (Call call : dispatcher.queuedCalls()) {
                if (tag == (int) call.request().tag()) {
                    call.cancel();
                    switch (tag) {
                        case TAG_HEATPOINTS:
                            Logger.i("热力点请求被取消");
                            break;
                    }

                }
            }
            //取消正在请求中带有tag的call
            for (Call call : dispatcher.runningCalls()) {
                if (tag == (int) call.request().tag()) {
                    call.cancel();
                    switch (tag) {
                        case TAG_HEATPOINTS:
                            Logger.i("热力点请求被取消");
                            break;
                    }
                }
            }
        }
    }

}
