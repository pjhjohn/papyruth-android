package com.montserrat.utils.view.search;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.CoursesData;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class ToolbarSearch {
    private AutoCompletableSearchView searchView;
    private Navigator navigator;
    private FragmentManager fragmentManager;
    private CompositeSubscription subscription;
    private Context context;

    private AutoCompleteAdapter autoCompleteAdapter;
    private CourseItemsAdapter courseItemsAdapter;

    private AutoCompletableSearchView.SearchViewListener searchViewListener;
    private RecyclerViewItemClickListener itemClickListener;

    List<Candidate> candidates;
    List<CourseData> courses;

    private RecyclerView autocompleteListView;
    private RecyclerView courseListView;
    private EditText queryView;
    private Candidate selectedCandidate;
    private String searchQuery;

    public ToolbarSearch(RecyclerViewItemClickListener listener, Activity activity){
        searchView = null;
        selectedCandidate = new Candidate();
        this.itemClickListener = listener;
        this.navigator = (Navigator) activity;
        this.fragmentManager = activity.getFragmentManager();
        this.context = activity;
        searchView = new AutoCompletableSearchView(this.itemClickListener, this.context);
    }

    public void initAutoComplete(RecyclerView autocompleteListView, View outsideTochableView, EditText editText, List<Candidate> candidates){

        this.queryView = editText;
        this.candidates = candidates;
        this.autocompleteListView = autocompleteListView;
        this.autoCompleteAdapter = new AutoCompleteAdapter(this.candidates, this.itemClickListener);
        this.autocompleteListView.setLayoutManager(new LinearLayoutManager(context));
        this.autocompleteListView.setAdapter(autoCompleteAdapter);

        this.searchView.initAutocompleteView(autocompleteListView, outsideTochableView, queryView, autoCompleteAdapter, candidates);
    }

    public void initResult(RecyclerView recyclerView, CourseItemsAdapter adapter,List<CourseData> items){
        this.courseListView = recyclerView;
        this.courseItemsAdapter = adapter;
        this.courses = items;

        this.searchView.initResultView(courseListView, courseItemsAdapter, courses);
    }

    public void setSearchViewListener(AutoCompletableSearchView.SearchViewListener listener){
        this.searchViewListener = listener;
        this.searchView.setSearchViewListener(searchViewListener);
    }

    public AutoCompletableSearchView getAutoCompletableSearchView(){
        return searchView;
    }


    public void recyclerViewClicked(View view, int position){
        this.selectedCandidate.lecture_id = candidates.get(position).lecture_id;
        this.selectedCandidate.professor_id = candidates.get(position).professor_id;
        this.searchQuery = null;

        this.search();
        this.searchView.showCandidates(false);
    }

    public void searchCourse(){
        Timber.d("searchCourse : %s", selectedCandidate);
        if (this.selectedCandidate.lecture_id == null && this.selectedCandidate.professor_id == null && searchQuery == null)
            this.searchHistory();
        else
            this.searchView.searchCourse(selectedCandidate.lecture_id, selectedCandidate.professor_id, searchQuery);
    }

    public void submitQuery(){
        this.candidates.clear();
        this.submitQuery(queryView.getText().toString());
    }

    public void submitQuery(String query){
        this.searchQuery = query;
        this.candidates.clear();
    }

    public void search(){
        AppManager.getInstance().putBoolean(AppConst.Preference.SEARCH, true);

        Fragment active = this.fragmentManager.findFragmentByTag(AppConst.Tag.ACTIVE_FRAGMENT);
        if (active != null && active.getClass() == SimpleCourseFragment.class) {
            this.searchView.searchCourse(selectedCandidate.lecture_id, selectedCandidate.professor_id, searchQuery);
        }else {
            this.navigator.navigate(SimpleCourseFragment.class, true);
            this.searchViewListener.onShowChange(true);
        }
    }

    public boolean onBack(){
        return this.searchView.onBack();
    }

    public void searchHistory(){
        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            //Todo : when history is empty, inform empty.
            this.courseItemsAdapter.setResIdNoDataText(R.string.no_data_history);
        }else {
            List<CourseData> courseList = ((CoursesData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                CoursesData.class
            )).courses;
            searchView.notifyChangedCourseAsynchronized(courseList);
        }
    }

    private static final int HISTORY_SIZE = 10;
    public boolean addHistory(CourseData course){
        if(course.id == null)
            return false;
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
