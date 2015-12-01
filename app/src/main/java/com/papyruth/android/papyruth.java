package com.papyruth.android;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.model.unique.AppTracker;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.retrofit.ApiManager;
import com.papyruth.support.opensource.retrofit.RetrofitLogger;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import retrofit.RestAdapter;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 * MontserratApp.onCreate handles application initialization which should be called once.
 */
public class papyruth extends Application {
    private static Tracker tracker;
    public synchronized Tracker getTracker(){
        if(tracker == null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.ga_tracker);
            AppTracker.getInstance().setTracker(this);
        }
        tracker.setAppName(getResources().getString(R.string.app_title));
        if (User.getInstance().getId() != null){
            tracker.set("&uid", User.getInstance().getId().toString());
        }
        tracker.set("&ul", Locale.getDefault().getDisplayLanguage());
        tracker.enableAutoActivityTracking(true);
        return tracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /* Timber */
        Timber.plant(new Timber.DebugTree());

        /* AppManager Context */
        AppManager.getInstance().setContext(this.getApplicationContext());
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));

        /* Retrofit Api */
        Api.createInstance(ApiManager.createPapyruthApi(this, RestAdapter.LogLevel.FULL, new RetrofitLogger("RetrofitApi", "^[AC\\-\\<\\{].*")));

        /* Picasso Debugging flags */
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(false);
        Picasso.with(getApplicationContext()).setLoggingEnabled(false);
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
