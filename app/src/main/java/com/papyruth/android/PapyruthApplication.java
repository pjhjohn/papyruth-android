package com.papyruth.android;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.retrofit.ApiManager;
import com.papyruth.support.opensource.retrofit.RetrofitLogger;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import retrofit.RestAdapter;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 * MontserratApp.onCreate handles application initialization which should be called once.
 */
public class PapyruthApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            System.exit(1);
        });

        /* Fabric for Crashlytics */
        Fabric fabric = new Fabric.Builder(this).kits(new Crashlytics()).debuggable(true).build();
        Fabric.with(fabric);

        /* Timber */
        Timber.plant(new Timber.DebugTree());

        /* AppManager Context */
        AppManager.getInstance().setContext(this.getApplicationContext());
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));

        /* Retrofit Api */
        Api.createInstance(ApiManager.createPapyruthApi(this, RestAdapter.LogLevel.FULL, new RetrofitLogger("RetrofitApi", "^[AC\\-\\<\\{].*")));

        /* Picasso Debugging flags */
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(BuildConfig.DEBUG);
        Picasso.with(getApplicationContext()).setLoggingEnabled(BuildConfig.DEBUG);
    }

    /*
     * MultiDex Support for Pre-Lolipop devices to prevent possible crash with 65536+ methods
     * in build.gradle - dependency : compile 'com.android.support:multidex:1.0.1'
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
