package com.montserrat.utils.view.search;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.view.View;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.fragment.main.CourseFragment;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.CoursesData;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class ToolbarSearch {
    private AutoCompletableSearchView searchView;
    private Navigator navigator;
    private FragmentManager fragmentManager;
    private AutoCompletableSearchView.SearchViewListener searchViewListener;
    private CompositeSubscription subscription;

    private static ToolbarSearch instance;
    private ToolbarSearch(){
        searchView = null;
    }
    public static synchronized ToolbarSearch getInstance(){
        if(ToolbarSearch.instance == null) ToolbarSearch.instance = new ToolbarSearch();
        return ToolbarSearch.instance;
    }

    public AutoCompletableSearchView newSearchView(RecyclerViewItemClickListener listener, Context context, AutoCompletableSearchView.Type type){
        searchView = new AutoCompletableSearchView(listener, context, type);
        return searchView;
    }

    public void setSearchViewListener(AutoCompletableSearchView.SearchViewListener listener){
        this.searchViewListener = listener;
        this.searchView.setSearchViewListener(searchViewListener);
    }

    public AutoCompletableSearchView getAutoCompletableSearchView(){
        return searchView;
    }

    public void setActivityComponent(Activity activity){
        this.navigator = (Navigator) activity;
        this.fragmentManager = activity.getFragmentManager();
    }

    public void recyclerViewClicked(View view, int position, boolean isAutoComplete){
        this.searchView.onRecyclerViewItemClick(view, position);
        this.search(isAutoComplete);
    }


    public void search(boolean isAutoComplete){
        if(isAutoComplete) {
            AppManager.getInstance().putBoolean(AppConst.Preference.SEARCH, true);

            Fragment active = this.fragmentManager.findFragmentByTag(AppConst.Tag.ACTIVE_FRAGMENT);
            if (active != null && active.getClass() == SimpleCourseFragment.class) {
                this.searchView.searchCourse();
            }else {
                this.navigator.navigate(SimpleCourseFragment.class, true);
                this.searchViewListener.onShowChange(true);
            }
        }else{
            this.navigator.navigate(CourseFragment.class, true);
        }
    }

    public boolean onBack(){
        return this.searchView.onBack();
    }



    public void searchHistory(){
        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            //Todo : when history is empty, inform empty.
        }else {
            List<CourseData> courseList = ((CoursesData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                CoursesData.class
            )).courses;
            // soon changed
            //using ACSV2's
            // notifyChangedCourseAsynchronized(courseList);
        }
    }

    private static final int HISTORY_SIZE = 10;
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
        coursesData.courses.clear();
        coursesData.courses.addAll(courseDataList);
        AppManager.getInstance().putStringParsed(AppConst.Preference.HISTORY, coursesData);
        return true;
    }
    public int containsCourse(List<CourseData> courses, CourseData target) {
        Timber.d("hash : %s", target.hashCode());
        for (CourseData course : courses) {
            Timber.d("hash : %s", course.hashCode());
            if (course.id.equals(target.id)) return courses.indexOf(course);
        }
        return -1;
    }
}
