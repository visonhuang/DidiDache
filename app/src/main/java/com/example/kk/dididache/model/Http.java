package com.example.kk.dididache.model;

import com.example.kk.dididache.MethodsKt;
import com.example.kk.dididache.model.Event.DriveTimeEvent;
import com.example.kk.dididache.model.Event.ExceptionEvent;
import com.example.kk.dididache.model.Event.HeatMapEvent;
import com.example.kk.dididache.model.Event.TaxiCountEvent;
import com.example.kk.dididache.model.Event.UseRatioEvent;
import com.example.kk.dididache.model.netModel.request.DriveTimeInfo;
import com.example.kk.dididache.model.netModel.request.ExceptionInfo;
import com.example.kk.dididache.model.netModel.request.PreTaxiCountInfo;
import com.example.kk.dididache.model.netModel.request.PreUseRatioInfo;
import com.example.kk.dididache.model.netModel.request.RealTimeHeatInfo;
import com.example.kk.dididache.model.netModel.request.UseRatioInfo;
import com.example.kk.dididache.model.netModel.response.ArrayFeedBack;
import com.example.kk.dididache.model.netModel.response.CarCountInXY;
import com.example.kk.dididache.model.netModel.request.HeatInfo;
import com.example.kk.dididache.model.netModel.response.DriveTime;
import com.example.kk.dididache.model.netModel.response.Exception;
import com.example.kk.dididache.model.netModel.response.ObjectFeedBack;
import com.example.kk.dididache.model.netModel.response.TaxiCount;
import com.example.kk.dididache.model.netModel.request.TaxiCountInfo;
import com.example.kk.dididache.model.netModel.response.UseRatio;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    public static final int TAG_HEAT_POINTS = 1;//请求热力点的tag
    public static final int TAG_TAXICOUNT = 2;//请求柱状图的tag
    public static final int TAG_REALTIME_HEAT_POINTS = 3;//请求热力点的tag
    public static final int TAG_USE_RATIO = 4;//请求使用率的tag
    public static final int TAG_DRIVE_TIME = 5;//请求路线规划的tag
    public static final int TAG_EXCEPTION = 6;//请求路线规划的tag


    //请求地址枚举类
    public enum ADRESS {
        realTimeHeatMap("show/dynamichot"),
        heatMap("show/statichot"),
        preHeatMap("show/prediction"),
        carCountChange("show/flowchange"),
        preCarCountChange("estimation/flowchange"),
        exception("estimation/trafficexception"),
        useRatio("show/useratio"),
        preUseRatio("estimation/useratio"),
        driveTime("estimation/drivetime");

        public String value;

        public String getUrl() {
            return "http://" + MethodsKt.getIpPort() + "/" + value;
        }

        ADRESS(String value) {
            this.value = value;
        }
    }


    public static Http getInstance() {
        if (http == null) http = new Http();
        if (http.client == null)
            http.client = new OkHttpClient().newBuilder().readTimeout(MethodsKt.getTimeOut(), TimeUnit.SECONDS).build();
        if (http.dispatcher == null) http.dispatcher = http.client.dispatcher();
        return http;
    }

    public void doPost(ADRESS adress, Object body) {
        switch (adress) {
            case heatMap:
                getHeatPoints((HeatInfo) body, false);
                break;
            case useRatio:
                getUseRatio((UseRatioInfo) body, false);
                break;
            case preUseRatio:
                getUseRatio((PreUseRatioInfo) body, true);
                break;
            case driveTime:
                getRoutePlan((DriveTimeInfo) body);
                break;
            case exception:
                getExceptions((ExceptionInfo) body);
                break;
            case preHeatMap:
                getHeatPoints((HeatInfo) body, true);
                break;
            case carCountChange:
                getTaxiCountByTime((TaxiCountInfo) body, false);
                break;
            case preCarCountChange:
                getTaxiCountByTime((PreTaxiCountInfo) body, true);
                break;
            case realTimeHeatMap:
                getRealTimeHeatPoints((RealTimeHeatInfo) body);
                break;
            default:
                throw new IllegalArgumentException("wtf!!!!!!!");
        }
    }

    //请求静态热力点
    private void getHeatPoints(final HeatInfo info, final boolean isFuture) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayFeedBack<CarCountInXY> feedBack;
                try {
                    //接收数据
                    Response response = getResponse(TAG_HEAT_POINTS, info, isFuture ? ADRESS.preHeatMap.getUrl() : ADRESS.heatMap.getUrl());
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    feedBack = new Gson().fromJson(reader, new TypeToken<ArrayFeedBack<CarCountInXY>>() {
                    }.getType());
                    //数据处理
                    EventBus.getDefault().post(new HeatMapEvent(feedBack));
                } catch (java.lang.Exception e) {
                    showCause(e);
                    MethodsKt.showToast(Http.this, "热力点出现了一些问题");
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //请求实时热力图
    private void getRealTimeHeatPoints(final RealTimeHeatInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayFeedBack<CarCountInXY> feedBack;
                try {
                    //接收数据
                    Response response = getResponse(TAG_REALTIME_HEAT_POINTS, info, ADRESS.realTimeHeatMap.getUrl());
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    feedBack = new Gson().fromJson(reader, new TypeToken<ArrayFeedBack<CarCountInXY>>() {
                    }.getType());
                    //数据处理
                    EventBus.getDefault().post(new HeatMapEvent(feedBack));
                } catch (java.lang.Exception e) {
                    showCause(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //请求某个坐标下一段时间内出租车变化情况
    private void getTaxiCountByTime(final Object info, final boolean isFuture) {

//        ArrayList<TaxiCount> list = new ArrayList<>();
//        for (int i = 0; i < info.getBarCount(); i++) {
//            list.add(new TaxiCount(new Random().nextInt(100)));
//        }
//        EventBus.getDefault().post(new TaxiCountEvent(list));

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayFeedBack<TaxiCount> feedBack;
                try {
                    //接收数据
                    Response response = getResponse(TAG_TAXICOUNT, info, isFuture ? ADRESS.preCarCountChange.getUrl() : ADRESS.carCountChange.getUrl());
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    feedBack = new Gson().fromJson(reader, new TypeToken<ArrayFeedBack<TaxiCount>>() {
                    }.getType());
                    Logger.json(new Gson().toJson(feedBack));
                    //数据处理
                    EventBus.getDefault().post(new TaxiCountEvent(feedBack));
                } catch (java.lang.Exception e) {
                    showCause(e);
                    e.printStackTrace();
                }
            }
        }).start();


    }

    //请求使用率
    private void getUseRatio(final Object info, final boolean isFuture) {

//        EventBus.getDefault().post(new UseRatioEvent(new UseRatio(100, 10)));
        new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectFeedBack<UseRatio> feedBack;
                try {
                    //接收数据
                    Response response = getResponse(TAG_USE_RATIO, info, isFuture ? ADRESS.preUseRatio.getUrl() : ADRESS.useRatio.getUrl());
                    //Logger.d(response.body().string());
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    feedBack = new Gson().fromJson(reader, new TypeToken<ObjectFeedBack<UseRatio>>() {
                    }.getType());
                    //数据处理
                    Logger.json(new Gson().toJson(feedBack));
                    EventBus.getDefault().post(new UseRatioEvent(feedBack));
                } catch (java.lang.Exception e) {
                    showCause(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //请求最佳路线
    private void getRoutePlan(final DriveTimeInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectFeedBack<DriveTime> feedBack;
                try {
                    //接收数据
                    Response response = getResponse(TAG_DRIVE_TIME, info, ADRESS.driveTime.getUrl());
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    feedBack = new Gson().fromJson(reader, new TypeToken<ObjectFeedBack<DriveTime>>() {
                    }.getType());
                    //数据处理
                    EventBus.getDefault().post(new DriveTimeEvent(feedBack));
                } catch (java.lang.Exception e) {
                    showCause(e);
                    ObjectFeedBack<DriveTime> errorFeedBack = new ObjectFeedBack<>();
                    errorFeedBack.state = -1;
                    EventBus.getDefault().post(new DriveTimeEvent(errorFeedBack));
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //请求异常
    private void getExceptions(final ExceptionInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayFeedBack<Exception> feedBack;
                try {
                    //接收数据
                    Response response = getResponse(TAG_EXCEPTION, info, ADRESS.exception.getUrl());
                    com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new InputStreamReader(response.body().byteStream()));
                    feedBack = new Gson().fromJson(reader, new TypeToken<ArrayFeedBack<Exception>>() {
                    }.getType());
                    //数据处理
                    Logger.json(new Gson().toJson(feedBack));
                    EventBus.getDefault().post(new ExceptionEvent(feedBack));
                } catch (java.lang.Exception e) {
                    showCause(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private Response getResponse(int tag, Object info, String url) {
        MediaType mediaType = MediaType.parse("application/json");
        Logger.d(url);
        Logger.json(new Gson().toJson(info));
        RequestBody body = RequestBody.create(mediaType, new Gson().toJson(info));
        Request request = new Request.Builder()
                .tag(tag)
                .url(url)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            return null;
        }
    }

    //取消请求
    public void cancelCall(int tag) {
        synchronized (dispatcher) {
            //取消队列中带tag的call
            for (Call call : dispatcher.queuedCalls()) {
                if (tag == (int) call.request().tag()) {
                    call.cancel();
                    switch (tag) {
                        case TAG_HEAT_POINTS:
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
                        case TAG_HEAT_POINTS:
                            Logger.i("热力点请求被取消");
                            break;
                    }
                }
            }
        }
    }

    private void showCause(java.lang.Exception e) {
        if (e.getClass() == JsonSyntaxException.class) {
            MethodsKt.showToast(this, "服务器返回异常");
        } else if (e.getClass() == NullPointerException.class) {
            MethodsKt.showToast(this, "服务器无返回");
        }
    }

}
