package com.montserrat.controller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by pjhjohn on 2015-04-10.
 * Contains Various Parameters for the application : Should be singleton.
 */
public class AppManager {
    private static String APP_PREFERENCE_NAME = "montserrat_shared_preference";
    private AppManager () {
        this.appContext = null;
        this.sharedpref = null;
    }
    private static AppManager instance;
    public static AppManager getInstance() {
        if ( instance == null ) {
            return instance = new AppManager();
        } else return instance;
    }

    private Context appContext;
    private void setApplicationContext(Context context) {
        this.appContext = context;
        this.sharedpref = this.appContext.getSharedPreferences(APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences sharedpref;
    private SharedPreferences getSharedPreference() {
        return this.sharedpref;
    }
}
