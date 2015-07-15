package com.montserrat.utils.view.search;

import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.CoursesData;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by SSS on 2015-06-10.
 */
public class Preferences {
    private Gson gson;

    public static final int HISTORY_SIZE = 10;

    public Preferences(){
        gson = new Gson();
    }

    public List<CourseData> getHistory(){
        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            return null;
        }
        return gson.fromJson(
            AppManager.getInstance().getString(AppConst.Preference.HISTORY, ""),
            CoursesData.class
        ).courses;
    }

    public boolean addHistory(CourseData course){
        List<CourseData> courseDataList;
        CoursesData coursesData = new CoursesData();
        coursesData.courses = new ArrayList<>();

        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            courseDataList = new ArrayList<>();
        }else {
            courseDataList  = getHistory();
        }
        int index;
        if((index = containsCourse(courseDataList, course)) >= 0) {
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
        coursesData.courses.addAll(courseDataList);

        String json = gson.toJson(coursesData);
        AppManager.getInstance().putString(AppConst.Preference.HISTORY, json);

        return true;
    }

    public int containsCourse(List<CourseData> courses, CourseData target) {
        for (CourseData course : courses) {
            if (course.id.equals(target.id)) return courses.indexOf(course);
        }
        return -1;
    }

    public static boolean clear(){
        if (AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
            AppManager.getInstance().remove(AppConst.Preference.HISTORY);
            return true;
        }
        return false;
    }
}
