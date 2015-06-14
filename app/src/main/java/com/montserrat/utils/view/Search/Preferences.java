package com.montserrat.utils.view.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.montserrat.app.model.PartialCourse;

import java.util.List;

/**
 * Created by SSS on 2015-06-10.
 */
public class Preferences {
    private SharedPreferences preferences;
    private Gson gson;
    private SharedPreferences.Editor editor;

    public enum Type{
        History
    }

    private final static String HISTORY = "history";

    public Preferences(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
    }

    public List<PartialCourse> getHistory(){
        String data = preferences.getString("history", "");
        List list = gson.fromJson(data, List.class);
        List<PartialCourse> partialCourseList = list;

        return null;
    }
}
