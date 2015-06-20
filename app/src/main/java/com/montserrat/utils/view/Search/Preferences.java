package com.montserrat.utils.view.search;

import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.response.SimpleCoursesResponse;

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

    public List<CourseData> getHistory(){
        String data = AppManager.getInstance().getString(AppConst.Preference.HISTORY, "");
        SimpleCoursesResponse simpleCoursesResponse = gson.fromJson(data, SimpleCoursesResponse.class);

        return simpleCoursesResponse.courses;
    }
    public boolean addHistory(CourseData course){
        SimpleCoursesResponse simpleCoursesResponse = new SimpleCoursesResponse();
        List<CourseData> courseDataList = getHistory();

        if(courseDataList == null){
            courseDataList = new ArrayList<>();
        }else {
            courseDataList.remove(0);
        }
        courseDataList.add(course);
        simpleCoursesResponse.courses.addAll(courseDataList);

        String json = gson.toJson(simpleCoursesResponse);
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
