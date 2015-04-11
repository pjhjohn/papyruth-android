package com.montserrat.controller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by pjhjohn on 2015-04-10.
 * Contains Various Parameters for the application : Should be singleton.
 */
public class ApplicationManager {
    private static String APP_PREFERENCE_NAME = "montserrat_shared_preference";
    private ApplicationManager () {
        this.appContext = null;
        this.sharedpref = null;
    }
    private static ApplicationManager instance;
    public static ApplicationManager getInstance() {
        if ( instance == null ) {
            return instance = new ApplicationManager();
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
