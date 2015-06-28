package com.montserrat.utils.view.search;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.SimpleCourseAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import rx.Subscription;
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
//    private Fragment simpleCourseFragment;

    private RecyclerViewClickListener autoCompleteListener;
//    private RecyclerViewClickListener courseListener;

    private AutoCompleteAdapter autoCompleteAdapter;
    private SimpleCourseAdapter simpleCourseAdapter;

    private Preferences preferences;
    private CompositeSubscription subscription;
    private Context context;

    private List<Candidate> candidates;
    private List<CourseData> courses;

    private Type type;
    private boolean searchMode;

    public enum Type{
        SEARCH, EVALUATION
    }


    public AutoCompletableSearchView(RecyclerViewClickListener listener, Context context, Type type){
        this.courses = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.subscription = new CompositeSubscription();
        this.preferences = new Preferences();

        this.autoCompleteListener = listener;
        this.context = context;
        this.type = type;

        this.searchMode = true;
        this.editText = null;
//        this.simpleCourseFragment = null;
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
        this.simpleCourseAdapter = new SimpleCourseAdapter(this.courses, this.autoCompleteListener);
        this.courseListView.setLayoutManager(new LinearLayoutManager(context));
        this.courseListView.setAdapter(this.simpleCourseAdapter);
//        this.courseListener = this.autoCompleteListener;
    }
    public void initCourse(RecyclerView courseListView, RecyclerViewClickListener listener){
        this.initCourse(courseListView);
//        this.courseListener = listener;
    }

    public void notifyChangedAutocomplete(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
    }

    public void notifyChangedCourse(List<CourseData> courses){
        this.courses.clear();
        this.courses.addAll(courses);
        this.simpleCourseAdapter.notifyDataSetChanged();
    }

    public Subscription autoComplete(TextView textView){
        editText = (EditText) textView;
        return WidgetObservable
            .text(textView)
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .map(listener -> listener.text().toString())
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
                    this.notifyChangedAutocomplete(results);
                    showCandidates(true);
                },
                error -> {
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    } else
                        candidates.clear();
                }
            );
    }

    public void submit(String query){
        Search.getInstance().clear().setQuery(query);
    }
    private Candidate evaluationCandidate;

    public void setEvaluationCandidate(int position) {
        this.evaluationCandidate = candidates.get(position);
    }

//    public void setMenuSearchFragment(Fragment fragment){
//        this.simpleCourseFragment = fragment;
//    }

    public void searchHistory(){
        List<CourseData> courseList = this.preferences.getHistory();
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
                                this.simpleCourseAdapter.notifyDataSetChanged();
                            },
                            error -> Timber.d("serch history error : %s", error)
                        )
                );
            }
        }
    }

    public void searchCourse() {
        Integer lectureId, professorId;
        String query;
        this.setSearchMode(AppManager.getInstance().getBoolean(AppConst.Preference.SEARCH, true));

        if (this.type == Type.EVALUATION){
            lectureId = evaluationCandidate.lecture_id;
            professorId = evaluationCandidate.professor_id;
            query = null;
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
                .subscribe(
                    this::notifyChangedCourse,
                    error -> Timber.d("search course error : %s", error)
                )
        );
    }

//    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        if(autocompleteView != null && ((RecyclerView)view.getParent()).getId() == autocompleteView.getId()) {
            Timber.d("***autocomplete Cilck");
            Search.getInstance().clear();
            Search.getInstance().fromCandidate(candidates.get(position));
            this.showCandidates(false);
//            if(this.simpleCourseFragment != null){
//                ((SimpleCourseFragment)this.simpleCourseFragment).reloadFragment();
//            }
        }else if(courseListView != null && ((RecyclerView)view.getParent()).getId() == courseListView.getId()){
            Timber.d("***course Cilck");
            Course.getInstance().clear().update(courses.get(position));
            preferences.addHistory(courses.get(position));
            Timber.d("***course Cilck2");

        }
    }

    public void setSearchMode(boolean searchMode){
        this.searchMode = searchMode;
        Timber.d("searchMode : %s", this.searchMode);
    }


    public void showCandidates(boolean show){
        ViewGroup.LayoutParams param;
        if(show){
            if(type == Type.EVALUATION) {
                param =  courseListView.getLayoutParams();
                param.height = this.context.getResources().getDisplayMetrics().heightPixels - this.editText.getHeight();
                courseListView.setLayoutParams(param);
            }

            param =  autocompleteView.getLayoutParams();
            param.height = 500;
            param.width = (int)(this.context.getResources().getDisplayMetrics().widthPixels * 0.8);
            autocompleteView.setLayoutParams(param);

            param =  outsideView.getLayoutParams();
            param.height = this.context.getResources().getDisplayMetrics().heightPixels;
            param.width = this.context.getResources().getDisplayMetrics().widthPixels;
            outsideView.setLayoutParams(param);
        } else {
            param =  autocompleteView.getLayoutParams();
            param.height = 0;
            param.width = (int)(this.context.getResources().getDisplayMetrics().widthPixels * 0.8);
            autocompleteView.setLayoutParams(param);

            param =  outsideView.getLayoutParams();
            param.height = 0;
            param.width = this.context.getResources().getDisplayMetrics().widthPixels;

            outsideView.setLayoutParams(param);
        }
    }
}
