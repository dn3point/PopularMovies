package com.iamzhaoyuan.android.popularmovies.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by yuan on 1/8/16.
 */
public class NetworkUtil {
    private static NetworkUtil instance = null;

    private NetworkUtil() {

    }

    public static synchronized NetworkUtil getInstance() {
        if (instance == null) instance = new NetworkUtil();
        return instance;
    }

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        if (activeNetwork != null) {
            isConnected = activeNetwork.isConnectedOrConnecting();
        }
        return isConnected;
    }
}
