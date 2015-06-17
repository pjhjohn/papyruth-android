package com.montserrat.utils.view.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.response.PartialCoursesResponse;

import java.util.ArrayList;
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
        PartialCoursesResponse partialCourseList = gson.fromJson(data, PartialCoursesResponse.class);

        return partialCourseList.courses;
    }
    public boolean addHistory(PartialCourse course){
        PartialCoursesResponse partialCoursesResponse = new PartialCoursesResponse();
        List<PartialCourse> partialCourseList = getHistory();

        if(partialCourseList == null){
            partialCourseList = new ArrayList<>();
        }else {
            partialCourseList.remove(0);
        }
        partialCourseList.add(course);
        partialCoursesResponse.courses.addAll(partialCourseList);

        editor = this.preferences.edit();
        String json = gson.toJson(partialCoursesResponse);
        editor.putString(HISTORY, json);
        editor.apply();

        return true;
    }
    public boolean clear(Type type){
        if(preferences.contains(HISTORY)){
            editor = this.preferences.edit();
            editor.remove(HISTORY);
            editor.apply();
            return true;
        }
        return false;
    }
}
