package com.montserrat.app;

import android.app.Application;

import com.montserrat.app.model.User;
import com.montserrat.utils.etc.RetrofitApi;
import com.montserrat.utils.etc.RetrofitLogger;
import com.squareup.picasso.Picasso;

import retrofit.RestAdapter;
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
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));

        /* Retrofit Api */
        new RetrofitApi.Builder()
            .setRoot("mont.izz.kr:3001")
            .setVersion(AppConst.API_VERSION)
            .enableSSL(false)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setLog(new RetrofitLogger("RetrofitApi", "^[A\\-\\<\\{].*"))
            .build();

        /* Picasso Debugging flags */
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);
        Picasso.with(getApplicationContext()).setLoggingEnabled(true);
    }
}
