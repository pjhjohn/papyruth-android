package com.montserrat.utils.view.search;

import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.response.SimpleCoursesResponse;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by SSS on 2015-06-10.
 */
public class Preferences {
    private Gson gson;

    public enum Type{
        HISTORY
    }
    public static final int HISTORY_SIZE = 5;

    public Preferences(){
        gson = new Gson();
    }

    public List<CourseData> getHistory(){
        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            return null;
        }
        String data = AppManager.getInstance().getString(AppConst.Preference.HISTORY, "");
        SimpleCoursesResponse simpleCoursesResponse = gson.fromJson(data, SimpleCoursesResponse.class);

        Timber.d("***get History");
        return simpleCoursesResponse.courses;
    }

    public boolean addHistory(CourseData course){
        List<CourseData> courseDataList;
        SimpleCoursesResponse simpleCoursesResponse = new SimpleCoursesResponse();
        simpleCoursesResponse.courses = new ArrayList<>();

        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            courseDataList = new ArrayList<>();
        }else {
            courseDataList  = getHistory();
        }
        int index;
        if((index = CourseDataContains(courseDataList, course)) >= 0) {
            courseDataList.remove(index);
            courseDataList.add(course);
        }else if (courseDataList.size() > HISTORY_SIZE - 1) {
            courseDataList.remove(0);
            courseDataList.add(course);
            while(courseDataList.size() > HISTORY_SIZE - 1){
                courseDataList.remove(0);
            }
        }else{
            courseDataList.add(course);
        }
        simpleCoursesResponse.courses.addAll(courseDataList);

        String json = gson.toJson(simpleCoursesResponse);
        AppManager.getInstance().putString(AppConst.Preference.HISTORY, json);

        return true;
    }

    public int CourseDataContains(List<CourseData> list, CourseData course){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).id.equals(course.id))
                return i;
        }
        return -1;
    }


    public static boolean clear(Type type){
        if(type == Type.HISTORY) {
            if (AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
                AppManager.getInstance().remove(AppConst.Preference.HISTORY);
                return true;
            }
        }
        return false;
    }
}
