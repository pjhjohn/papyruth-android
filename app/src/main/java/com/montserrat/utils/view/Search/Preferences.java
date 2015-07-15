package com.montserrat.utils.view.search;

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

    public static final int HISTORY_SIZE = 10;


    public boolean addHistory(CourseData course){
        List<CourseData> courseDataList;
        CoursesData coursesData = new CoursesData();
        coursesData.courses = new ArrayList<>();

        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            courseDataList = new ArrayList<>();
        }else {
            courseDataList  = ((CoursesData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                CoursesData.class
            )).courses;
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
        AppManager.getInstance().putStringParsed(AppConst.Preference.HISTORY, coursesData);
        return true;
    }


    //go to history.
    public int containsCourse(List<CourseData> courses, CourseData target) {
        Timber.d("hash : %s", target.hashCode());
        for (CourseData course : courses) {
            Timber.d("hash : %s", course.hashCode());
            if (course.id.equals(target.id)) return courses.indexOf(course);
        }
        return -1;
    }
}
