package com.montserrat.app;

import android.app.Application;

import com.montserrat.utils.request.Api;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 */
public class MontserratApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        AppManager.getInstance().setContext(this.getApplicationContext());

        /* Api Endpoint Setup */
        new Api.Builder()
            .setRoot("mont.izz.kr:3001")
            .setVersion(AppConst.API_VERSION)
            .enableSSL(false)
            .build();
    }
}
