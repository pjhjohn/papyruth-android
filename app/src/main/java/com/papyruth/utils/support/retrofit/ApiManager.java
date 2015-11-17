package com.papyruth.utils.support.retrofit;

import android.content.Context;

import com.papyruth.android.AppConst;
import com.papyruth.utils.support.retrofit.apis.Papyruth;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import timber.log.Timber;

public class ApiManager {
    private static final int SIZE_OF_CACHE = 1024 * 1024 * 3; // 3MB
    public static final int TIMEOUT = 30;

    private static RestAdapter getRestAdapter(Context context) {
        return getRestAdapter(context, RestAdapter.LogLevel.BASIC, new RetrofitLogger(AppConst.LOGGER_TAG));
    }
    private static RestAdapter getRestAdapter(Context context, RestAdapter.LogLevel loglevel) {
        return getRestAdapter(context, loglevel, new RetrofitLogger(AppConst.LOGGER_TAG));
    }
    private static RestAdapter getRestAdapter(Context context, RestAdapter.Log logger) {
        return getRestAdapter(context, RestAdapter.LogLevel.BASIC, logger);
    }
    private static RestAdapter getRestAdapter(Context context, RestAdapter.LogLevel loglevel, RestAdapter.Log logger) {
        final Context finalContext = context.getApplicationContext();
        /* Create Cache */
        Cache responseCache = null;
        try {
            responseCache = new Cache(new File(finalContext.getCacheDir(), "Papyruth"), SIZE_OF_CACHE);
            Timber.d("Created cache with size %d", SIZE_OF_CACHE);
        } catch (IOException e) {
            Timber.d("Unable to create cache with size %d", SIZE_OF_CACHE);
        }

        /* Create OkHttpClient */
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setCache(responseCache);
        okHttpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.networkInterceptors().add(chain -> {
            Request request = chain.request();
            // Add Cache Control only for GET methods
            if (request.method().equals("GET")) {
                if (NetworkUtils.isNetworkConnected(finalContext)) {
                    // 1 day
                    request.newBuilder()
                        .header("Cache-Control", "only-if-cached")
                        .build();
                } else {
                    // 4 weeks stale
                    request.newBuilder()
                        .header("Cache-Control", "public, max-stale=2419200")
                        .build();
                }
            }
            Response response = chain.proceed(request);
            // Re-write response CC header to force use of cache
            return response.newBuilder()
//                .header("Cache-Control", "public, max-age=86400") // 1 day
                .header("Cache-Control", "public, max-age=60") // 1 min
                .build();
        });

        /* Create Executor */
        Executor executor = Executors.newCachedThreadPool();

        /* Build RestAdapter */
        return new RestAdapter.Builder()
            .setEndpoint(String.format("https://%s/api/%s/", AppConst.API_BASE_URL, AppConst.API_VERSION))
            .setExecutors(executor, executor)
            .setClient(new OkClient(okHttpClient))
            .setLogLevel(loglevel)
            .setLog(logger)
            .build();
    }

    public static Papyruth createPapyruthApi(Context context) {
        return getRestAdapter(context).create(Papyruth.class);
    }
    public static Papyruth createPapyruthApi(Context context, RestAdapter.LogLevel loglevel) {
        return getRestAdapter(context, loglevel).create(Papyruth.class);
    }
    public static Papyruth createPapyruthApi(Context context, RestAdapter.Log logger) {
        return getRestAdapter(context, logger).create(Papyruth.class);
    }
    public static Papyruth createPapyruthApi(Context context, RestAdapter.LogLevel loglevel, RestAdapter.Log logger) {
        return getRestAdapter(context, loglevel, logger).create(Papyruth.class);
    }
}