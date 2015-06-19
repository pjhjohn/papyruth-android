package com.montserrat.utils.view.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.response.PartialCoursesResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SSS on 2015-06-10.
 */
public class Preferences {
    private Gson gson;

    public enum Type{
        History
    }

    public Preferences(){
        gson = new Gson();
    }

    public List<PartialCourse> getHistory(){
        String data = AppManager.getInstance().getString(AppConst.Preference.HISTORY, "");
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

        String json = gson.toJson(partialCoursesResponse);
        AppManager.getInstance().putString(AppConst.Preference.HISTORY, json);

        return true;
    }
    public boolean clear(Type type){
        if(type == Type.History) {
            if (AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
                AppManager.getInstance().remove(AppConst.Preference.HISTORY);
                return true;
            }
        }
        return false;
    }
}
