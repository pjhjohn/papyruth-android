package com.papyruth.utils.support.retrofit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class NetworkUtils {
    public static boolean isNetworkConnected(@NonNull final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!= null && networkInfo.isConnected()) {
            Timber.d("Active Network : %s", networkInfo.getTypeName());
        }
        return networkInfo != null && networkInfo.isConnected();
    }
}