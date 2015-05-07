package com.montserrat.app;

import android.app.Application;

import com.montserrat.utils.request.Api;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 * MontserratApp.onCreate handles application initialization which should be called once.
 */
public class MontserratApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /* Timber */
        Timber.plant(new Timber.DebugTree());

        /* AppManager Context */
        AppManager.getInstance().setContext(this.getApplicationContext());

        /* Api Endpoint */
        new Api.Builder()
            .setRoot("mont.izz.kr:3001")
            .setVersion(AppConst.API_VERSION)
            .enableSSL(false)
            .build();
    }
}
