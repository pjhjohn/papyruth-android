package com.papyruth.android;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.retrofit.ApiManager;
import com.papyruth.support.opensource.retrofit.RetrofitLogger;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.*;
import com.papyruth.support.utility.error.Error;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import retrofit.RestAdapter;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 * MontserratApp.onCreate handles application initialization which should be called once.
 */
public class PapyruthApplication extends Application implements Error.OnReportToGoogleAnalytics{
    private Tracker mTracker;
    synchronized public Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.ga_tracker);
        }
        mTracker.setAppName(getResources().getString(R.string.application_title));
        mTracker.set("&uid", User.getInstance().getId() == null ? "null" : User.getInstance().getId().toString());
        mTracker.set("&ul", Locale.getDefault().getDisplayLanguage());
        mTracker.enableAutoActivityTracking(true);
        AppTracker.getInstance().setTracker(mTracker);
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            ex.printStackTrace();
            this.onReportToGoogleAnalytics(ex.getMessage(), this.getClass().getSimpleName(), false);
        });

        /* Fabric for Crashlytics */
        Fabric.with(this, new Crashlytics());

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

    @Override
    public void onReportToGoogleAnalytics(String cause, String from, boolean isFatal) {
        Timber.d("Application.onReportToGoogleAnalytics from %s\nCause : %s", from, cause);
        String description = Error.description(String.format("UncaughtExeption from %s : %s", from, cause));
        mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(isFatal).build());
        System.exit(1);
    }
}
