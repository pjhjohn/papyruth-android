package com.montserrat.app;

import android.app.Application;

import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.ApiManager;
import com.montserrat.utils.support.retrofit.RetrofitLogger;
import com.montserrat.utils.support.retrofit.apis.Api;
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
        Api.createInstance(ApiManager.createPapyruthApi(this, RestAdapter.LogLevel.FULL, new RetrofitLogger("RetrofitApi", "^[AC\\-\\<\\{].*")));

        /* Picasso Debugging flags */
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(false);
        Picasso.with(getApplicationContext()).setLoggingEnabled(false);
    }
}
