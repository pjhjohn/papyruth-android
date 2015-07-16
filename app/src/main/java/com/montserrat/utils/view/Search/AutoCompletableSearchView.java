package com.montserrat.utils.view.search;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.CoursesData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by SSS on 2015-06-06.
 */
public class AutoCompletableSearchView {
    private EditText editText;
    private RecyclerView autocompleteView;
    private RecyclerView courseListView;
    private View outsideView;
    private RecyclerViewItemClickListener autoCompleteListener;

    private AutoCompleteAdapter autoCompleteAdapter;
    private CourseItemsAdapter courseItemsAdapter;

    private CompositeSubscription subscription;
    private Context context;

    private List<Candidate> candidates;
    private List<CourseData> courses;

    private Type type;
    private boolean searchMode;
    private boolean setOpen;
    private boolean isOpen;
    private boolean isAutocompleteViewOpen;
    private boolean reserveCandidateListOnClose;

    private SearchViewListener searchViewListener;

    public enum Type{
        SEARCH, EVALUATION
    }

    public AutoCompletableSearchView(RecyclerViewItemClickListener listener, Context context, Type type){
        this.courses = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.subscription = new CompositeSubscription();

        this.autoCompleteListener = listener;
        this.context = context;
        this.type = type;

        this.searchMode = true;
        this.editText = null;
        this.setOpen = false;
        this.isAutocompleteViewOpen = false;
        this.isOpen = false;
        this.reserveCandidateListOnClose = false;
    }

    public interface SearchViewListener{
        public void onTextChange(String query);
        public void onShowChange(boolean show);
    }
    public void setSearchViewListener(SearchViewListener listener){
        this.searchViewListener = listener;
    }

    public void initAutoComplete(RecyclerView autocompleteView, View outsideView){
        this.autocompleteView = autocompleteView;
        this.autoCompleteAdapter = new AutoCompleteAdapter(this.candidates, this.autoCompleteListener);
        this.autocompleteView.setLayoutManager(new LinearLayoutManager(context));
        this.autocompleteView.setAdapter(this.autoCompleteAdapter);
        this.outsideView = outsideView;
        this.outsideView.setOnClickListener(view -> showCandidates(false));
    }

    public void initCourse(RecyclerView courseListView){
        this.courseListView = courseListView;
        if (type == Type.EVALUATION) {
            this.courseItemsAdapter = new CourseItemsAdapter(this.courses, this.autoCompleteListener, R.layout.cardview_header_height_zero);
            this.evaluationCandidate = new Candidate();
        } else this.courseItemsAdapter = new CourseItemsAdapter(this.courses, this.autoCompleteListener);

        this.courseListView.setLayoutManager(new LinearLayoutManager(context));
        this.courseListView.setAdapter(this.courseItemsAdapter);
    }

    public void notifyChangedAutocomplete(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
        this.updateViewHeight();
    }

    public void notifyChangedCourse(List<CourseData> courses){
        this.courses.clear();
        this.courses.addAll(courses);
        this.courseItemsAdapter.notifyDataSetChanged();
    }

    public void setAutoCompleteViewOpen(boolean setOpen){
        this.setOpen = setOpen;
    }

    public void autoComplete(TextView textView){
        this.editText = (EditText) textView;

        this.editText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                this.showCandidates(true);
            }
        });

        this.subscription.add(
            WidgetObservable
                .text(this.editText)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> this.setAutoCompleteViewOpen(true))

        );

        this.subscription.add(
            WidgetObservable
                .text(this.editText)
                .map(listener -> {
                    if (searchViewListener != null) {
                        searchViewListener.onTextChange(listener.text().toString());
                    }
                    return listener.text().toString();
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(query -> {
                    if (type != Type.EVALUATION && query.isEmpty()) return null; // history
                    return RetrofitApi.getInstance().search_autocomplete(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        query
                    );
                })
                .map(response -> response.candidates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    results -> {
                        if (isOpen && setOpen)
                            this.showCandidates(true);
                        this.notifyChangedAutocomplete(results);
                    },
                    error -> {
//                        this.showCandidates(false);
                        this.showCandidates(setOpen);
                        Timber.d("get Candidates error : %s", error);
                        if (error instanceof RetrofitError) {
                            switch (((RetrofitError) error).getResponse().getStatus()) {
                                default:
                                    Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                            }
                        }
                    }
                )
        );
    }

    public void querySubmit(){
        this.querySubmit(this.editText.getText().toString());
    }

    public void querySubmit(String query){
        if(this.type == Type.EVALUATION){
            this.setEvaluationCandidate(query);
            this.searchCourse();
        }else if(this.type == Type.SEARCH) {
            Search.getInstance().clear().setQuery(query);
        }
        this.showCandidates(false);
        this.editText.clearFocus();
    }



    private Candidate evaluationCandidate;
    private String evaluationQuery;

    public void setEvaluationCandidate(int position) {
        this.evaluationCandidate.clear();

        this.evaluationQuery = null;
        this.evaluationCandidate = candidates.get(position);
    }
    public void setEvaluationCandidate(String query){
        this.evaluationCandidate.clear();
        this.evaluationQuery = query;
    }

    public void setReserveCandidateListOnClose(boolean reserve){
        this.reserveCandidateListOnClose = reserve;
    }

    public boolean getReserveCandidateListOnClose(){
        return this.reserveCandidateListOnClose;
    }

    public boolean hasData(){
        if(this.type == Type.SEARCH){
            return true;
        }else if(this.type == Type.EVALUATION){
            if (this.evaluationCandidate.lecture_id != null || this.evaluationCandidate.professor_id != null || this.evaluationQuery != null)
                return true;
        }
        return false;
    }

    public void searchHistory(){
        List<CourseData> courseList = ((CoursesData)AppManager.getInstance().getStringParsed(
            AppConst.Preference.HISTORY,
            CoursesData.class
        )).courses;
        Timber.d("courses : %s", courseList.size());
        this.courses.clear();
        if(courseList == null){
            //Todo : when history is empty, inform history is empty.
        }else {
            for (int i = 0; i < courseList.size(); i++) {
                final int j = courseList.size() - i - 1;
                this.courses.add(CourseData.Sample());
                this.subscription.add(
                    RetrofitApi.getInstance().search_search(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        courseList.get(i).lecture_id,
                        courseList.get(i).professor_id,
                        null
                    )
                        .map(response -> response.courses)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            course -> {
                                this.courses.set(j, course.get(0));
                                this.courseItemsAdapter.notifyDataSetChanged();
                            },
                            error -> Timber.d("serch history error : %s", error)
                        )
                );
            }
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

    public void setSearchMode(boolean searchMode){
        this.searchMode = searchMode;
    }

    public void searchCourse() {
        Integer lectureId, professorId;
        String query;
        this.setSearchMode(AppManager.getInstance().getBoolean(AppConst.Preference.SEARCH, true));
        this.setAutoCompleteViewOpen(false);

        if (this.type == Type.EVALUATION){
            lectureId = this.evaluationCandidate.lecture_id;
            professorId = this.evaluationCandidate.professor_id;
            query = this.evaluationQuery;
        } else if(searchMode) {
            lectureId = Search.getInstance().getLectureId();
            professorId = Search.getInstance().getProfessorId();
            query = Search.getInstance().getQuery();
        }else{
            searchHistory();
            return;
        }

        this.subscription.add(
            RetrofitApi.getInstance().search_search(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                lectureId,
                professorId,
                query
            )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    this.showCandidates(false);
                })
                .subscribe(
                    this::notifyChangedCourse,
                    error -> Timber.d("search course error : %s", error)
                )
        );
    }

    public void onRecyclerViewItemClick(View view, int position) {
//        this.showCandidates(false);
        if(autocompleteView != null && ((RecyclerView)view.getParent()).getId() == autocompleteView.getId()) {
            Search.getInstance().clear();
            Search.getInstance().fromCandidate(candidates.get(position));
        }else if(courseListView != null && ((RecyclerView)view.getParent()).getId() == courseListView.getId()){
            Course.getInstance().clear().update(courses.get(position));
            this.addHistory(courses.get(position));
        }
    }


    public void updateViewHeight(){
        ViewGroup.LayoutParams param;
        param =  autocompleteView.getLayoutParams();
        if(isOpen) {
            if (this.candidates.size() < 5) {
                param.height = (int) (48 * this.candidates.size() * this.context.getResources().getDisplayMetrics().density);
            } else {
                param.height = (int) (240 * this.context.getResources().getDisplayMetrics().density);
            }

//            param.width = (int)(this.context.getResources().getDisplayMetrics().widthPixels * 0.8);
        }else{
            param.height = 0;
        }
        this.autocompleteView.setLayoutParams(param);
    }

    public void showCandidates(boolean show){
        ViewGroup.LayoutParams param;
        if(show){
            if(type == Type.EVALUATION) {
                param =  courseListView.getLayoutParams();
                param.height = this.context.getResources().getDisplayMetrics().heightPixels - this.editText.getHeight();
                courseListView.setLayoutParams(param);
            }
            if(this.editText.getText().toString().length() == 0)
                this.candidates.clear();
            Timber.d("&&& size : %s", this.candidates.size());

            this.isOpen = true;
            this.updateViewHeight();

            this.outsideView.setAlpha((float) 0.7);
            this.outsideView.setBackgroundColor(Color.GRAY);

            param =  this.outsideView.getLayoutParams();
            param.height = this.context.getResources().getDisplayMetrics().heightPixels;
            param.width = this.context.getResources().getDisplayMetrics().widthPixels;
            this.outsideView.setLayoutParams(param);
            this.isAutocompleteViewOpen = true;

            this.onShowChange(true);
        } else {
            param =  this.autocompleteView.getLayoutParams();
            param.height = 0;
            this.autocompleteView.setLayoutParams(param);

            param =  outsideView.getLayoutParams();
            param.height = 0;
            param.width = this.context.getResources().getDisplayMetrics().widthPixels;

            this.outsideView.setLayoutParams(param);

            this.editText.clearFocus();
            ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.editText.getWindowToken(), 2);
            this.isAutocompleteViewOpen = false;

            this.onShowChange(false);

            this.setOpen = false;
            this.isOpen = false;
            if(!reserveCandidateListOnClose)
                this.candidates.clear();
        }
    }

    private void onShowChange(boolean show){
        if(this.searchViewListener != null)
            this.searchViewListener.onShowChange(show);
    }

    public boolean onBack(){
        if(isAutocompleteViewOpen){
            this.showCandidates(false);
            return true;
        }

        return false;
    }
}
