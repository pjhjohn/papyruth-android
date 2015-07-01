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
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;import rx.schedulers.Schedulers;
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

    private Preferences preferences;
    private CompositeSubscription subscription;
    private Context context;

    private List<Candidate> candidates;
    private List<CourseData> courses;

    private Type type;
    private boolean searchMode;
    private boolean isAutoCompleteViewOpen;

    public enum Type{
        SEARCH, EVALUATION
    }


    public AutoCompletableSearchView(RecyclerViewItemClickListener listener, Context context, Type type){
        this.courses = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.subscription = new CompositeSubscription();
        this.preferences = new Preferences();

        this.autoCompleteListener = listener;
        this.context = context;
        this.type = type;

        this.searchMode = true;
        this.editText = null;
        this.isAutoCompleteViewOpen = false;
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
        this.courseItemsAdapter = new CourseItemsAdapter(this.courses, this.autoCompleteListener);

        if(type == Type.EVALUATION){
            this.courseItemsAdapter.setHead(false);
        }else{
            this.courseItemsAdapter.setHead(true);
        }

        this.courseListView.setLayoutManager(new LinearLayoutManager(context));
        this.courseListView.setAdapter(this.courseItemsAdapter);
    }

    public void notifyChangedAutocomplete(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
    }

    public void notifyChangedCourse(List<CourseData> courses){
        this.courses.clear();
        this.courses.addAll(courses);
        this.courseItemsAdapter.notifyDataSetChanged();
    }
    public void setAutoCompleteViewOpen(boolean isOpen){
        this.isAutoCompleteViewOpen = isOpen;
    }

    public void autoComplete(TextView textView){
        this.editText = (EditText) textView;
        this.editText.clearFocus();
        this.editText.setOnFocusChangeListener((v, hasFocus) -> {
            Timber.d("***hasFocus, %s", hasFocus);
            if(hasFocus)
                this.showCandidates(true);
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
                        this.showCandidates(isAutoCompleteViewOpen);
                        this.notifyChangedAutocomplete(results);
                    },
                    error -> {
                        this.showCandidates(false);
                        if (error instanceof RetrofitError) {
                            switch (((RetrofitError) error).getResponse().getStatus()) {
                                default:
                                    Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                            }
                        } else
                            candidates.clear();
                    }
                )
        );
    }

    public void submit(){
        this.submit(this.editText.getText().toString());
        this.showCandidates(false);
    }

    public void submit(String query){
        Timber.d("***submit");
        if(this.type == Type.EVALUATION){
            this.setEvaluationCandidate(query);
            this.searchCourse();
        }else if(this.type == Type.SEARCH) {
            Timber.d("submit click");
            Search.getInstance().clear().setQuery(query);
        }
        this.editText.clearFocus();
    }
    private Candidate evaluationCandidate;
    private String evaluationQuery;

    public void setEvaluationCandidate(int position) {
        if(this.evaluationCandidate != null)
            this.evaluationCandidate.clear();
        else
            this.evaluationCandidate = new Candidate();
        this.evaluationQuery = null;
        this.evaluationCandidate = candidates.get(position);
    }
    public void setEvaluationCandidate(String query){
        if(this.evaluationCandidate != null)
            this.evaluationCandidate.clear();
        else
            this.evaluationCandidate = new Candidate();
        this.evaluationQuery = query;
    }

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
                                this.courseItemsAdapter.notifyDataSetChanged();
                            },
                            error -> Timber.d("serch history error : %s", error)
                        )
                );
            }
        }
    }

    public void searchCourse() {
        Timber.d("***searchCourse");
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
            preferences.addHistory(courses.get(position));
        }
    }

    public void setSearchMode(boolean searchMode){
        this.searchMode = searchMode;
        Timber.d("***searchMode : %s", this.searchMode);
    }

    public void showCandidates(boolean show){
        Timber.d("***autocomplete %s", show);
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

            ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.editText.getWindowToken(), 2);
        }
    }
}
