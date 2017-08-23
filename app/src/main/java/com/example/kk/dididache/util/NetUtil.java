package com.example.kk.dididache.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.kk.dididache.App;

/**
 * Created by KK on 2017/8/23.
 */

public class NetUtil {

    /**
     * @return wifi可用返回 true
     */
    public static boolean isWifiOk() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.Companion.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    /**
     * @return 移动网络可用返回 true
     */
    public static boolean isMobileOk() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.Companion.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}
