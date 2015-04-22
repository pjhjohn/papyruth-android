package com.montserrat.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

/**
 * Created by pjhjohn on 2015-04-10.
 * Contains Various Parameters for the application : Should be singleton.
 */
public class AppManager {
    /* Singleton */
    private static AppManager instance;
    private AppManager() {}
    public static AppManager getInstance() {
        if ( instance == null ) return instance = new AppManager();
        else return instance;
    }

    /* Members */
    private Context context = null;
    public void setContext(Context context) { this.context = context; }

    public Resources getResources() throws Resources.NotFoundException {
        if ( this.context == null ) throw new Resources.NotFoundException("You must set Context to AppManager before calling getResources()");
        else return this.context.getResources();
    }
    public Context getContext() {
        return this.context;
    }
    public String getString(int stringResId) throws Resources.NotFoundException {
        if(this.context==null) throw new Resources.NotFoundException("You must set Context to AppManager before calling getResources()");
        else return this.context.getResources().getString(stringResId);
    }
}
