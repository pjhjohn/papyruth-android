package com.montserrat.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-10.
 * Contains Various Parameters for the application : Should be singleton.
 */
public class AppManager {
    /* Singleton */
    private static AppManager instance;
    private AppManager() {
        mainToolbarColor = AppConst.COLOR_POINT_CLARITY;
    }
    public static AppManager getInstance() {
        if ( instance == null ) return instance = new AppManager();
        else return instance;
    }

    /* Members */
    private Context context = null;
    private SharedPreferences pref = null;
    private int mainToolbarColor;

    public void setContext(Context context) {
        this.context = context;
        this.pref = this.context.getSharedPreferences(AppConst.Preference.STORAGE_NAME, Context.MODE_PRIVATE);
    }

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

    /* Mapping SharedPreferences methods */
    public void putBoolean(String key, boolean value) {
        this.pref.edit().putBoolean(key, value).apply();
    }
    public void putInt(String key, int value) {
        this.pref.edit().putInt(key, value).apply();
    }
    public void putLong(String key, long value) {
        this.pref.edit().putLong(key, value).apply();
    }
    public void putFloat(String key, float value) {
        this.pref.edit().putFloat(key, value).apply();
    }
    public void putString(String key, String value) {
        this.pref.edit().putString(key, value).apply();
    }
    public void putStringSet(String key, Set<String> value) {
        this.pref.edit().putStringSet(key, value).apply();
    }

    public boolean getBoolean(String key, boolean fallback) {
        return this.pref.getBoolean(key, fallback);
    }
    public int getInt(String key, int fallback) {
        return this.pref.getInt(key, fallback);
    }
    public long getLong(String key, long fallback) {
        return this.pref.getLong(key, fallback);
    }
    public float getFloat(String key, float fallback) {
        return this.pref.getFloat(key, fallback);
    }
    public String getString(String key, String fallback) {
        return this.pref.getString(key, fallback);
    }
    public Set<String> getStringSet(String key, Set<String> fallback) {
        return this.pref.getStringSet(key, fallback);
    }
    public Map<String, ?> getAll(String key, Map<String, ?> fallback) {
        return this.pref.getAll();
    }

    public void putStringParsed(String key, Object value){
        Gson gson = new Gson();
        String json = gson.toJson(value);
        Timber.d("type2 : %s", value.getClass().getSimpleName());

        this.putString(key, json);
    }

    public Object getStringParsed(String key, Class<?> classtype){
        Gson gson = new Gson();
        if(!this.contains(key)){
            return null;
        }
        Timber.d("type : %s", classtype);
        return gson.fromJson(
            this.getString(key, ""),
            classtype
        );
    }

    public void remove(String key) {
        this.pref.edit().remove(key).apply();
    }

    public boolean clear(String key){
        if (this.contains(key)) {
            AppManager.getInstance().remove(key);
            return true;
        }
        return false;
    }

    public void clear() {
        this.pref.edit().clear().apply();
    }

    public boolean contains(String key){
        return this.pref.contains(key);
    }

    public int getMainToolbarColor() {
        return this.mainToolbarColor;
    }

    public void setMainToolbarColor(int color) {
        this.mainToolbarColor = color;
    }
}