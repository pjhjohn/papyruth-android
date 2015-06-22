package com.montserrat.utils.view.search;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.SimpleCourseAdapter;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
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
public class AutoCompletableSearchView implements RecyclerViewClickListener {
    private CompositeSubscription subscription;
    private RecyclerView autocompleteView;
    private RecyclerView courseListView;
    private View outsideView;
    private List<Candidate> candidates;
    private List<CourseData> courses;
    private SimpleCourseAdapter simpleCourseAdapter;
    private Fragment simpleCourseFragment;
    private AutoCompleteAdapter autoCompleteAdapter;
    private RecyclerViewClickListener itemListener;
    private Context context;
    private EditText editText;
    private Preferences preferences;

    public enum Type{
        TOOLBAR, SEARCH, COURSE, HISTORY, EVALUATION
    }

    public enum History{

    }
    private Type type;

    public AutoCompletableSearchView(RecyclerViewClickListener listener, Context context, Type type){
        this.courses = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.subscription = new CompositeSubscription();
        this.itemListener = listener;
        this.context = context;
        this.type = type;
        this.editText = null;
        this.simpleCourseFragment = null;
        this.preferences = new Preferences();
    }

    public void autoCompleteSetup(RecyclerView autocompleteView, View outsideView){
        this.autocompleteView = autocompleteView;
        this.autoCompleteAdapter = new AutoCompleteAdapter(this.candidates, this.itemListener);
        this.autocompleteView.setLayoutManager(new LinearLayoutManager(context));
        this.autocompleteView.setAdapter(this.autoCompleteAdapter);
        this.outsideView = outsideView;
        this.outsideView.setOnClickListener(view -> showCandidates(false));
    }

    public void notifyAutocompleteChanged(List <Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
    }

    public void courseSetup(RecyclerView courseListView){
        this.courseListView = courseListView;
        this.simpleCourseAdapter = new SimpleCourseAdapter(this.courses, this.itemListener);
        this.courseListView.setLayoutManager(new LinearLayoutManager(context));
        this.courseListView.setAdapter(this.simpleCourseAdapter);
    }

    public void notifycourseChanged(List<CourseData> courses){
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
                        this.notifyAutocompleteChanged(results);
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

    public void setSimpleCourseFragment(Fragment fragment){
        this.simpleCourseFragment = fragment;
        Timber.d("setfragemnt %s", fragment);
    }

    public void searchCourse(Type type) {
        if(type == Type.HISTORY){
            if (this.preferences.getHistory() != null) {
                List<CourseData> courseList = this.preferences.getHistory();
                this.courses.clear();
                for (int i = 0; i < courseList.size(); i++) {
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
                                        this.courses.addAll(course);
                                        this.simpleCourseAdapter.notifyDataSetChanged();

                                    },
                                    error -> Timber.d("serch history error : %s", error)
                            )
                    );
                }
            }else{
                //TODO : implement it!!
            }

        }else if (type == Type.EVALUATION){
            this.subscription.add(
                RetrofitApi.getInstance().search_search(
                    User.getInstance().getAccessToken(),
                    User.getInstance().getUniversityId(),
                    evaluationCandidate.lecture_id,
                    evaluationCandidate.professor_id,
                    null
                )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::notifycourseChanged,
                        error -> Timber.d("search course error : %s %s", error)
                )
            );
        } else {
            RetrofitApi
                .getInstance()
                .search_search(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        Search.getInstance().getLectureId(),
                        Search.getInstance().getProfessorId(),
                        Search.getInstance().getQuery()
                )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::notifycourseChanged,
                        error -> Timber.d("Search error : %s", error)
                );
        }
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        if(autocompleteView != null && ((RecyclerView)view.getParent()).getId() == autocompleteView.getId()) {
            Search.getInstance().clear();
            Search.getInstance().fromCandidate(candidates.get(position));
            this.showCandidates(false);
            Timber.d("isnull : %s", this.simpleCourseFragment);
            if(this.simpleCourseFragment != null) {
                ((SimpleCourseFragment)this.simpleCourseFragment).refresh();
            }
        }else if(courseListView != null && ((RecyclerView)view.getParent()).getId() == courseListView.getId()){
            Course.getInstance().clear().fromPartailCourse(courses.get(position));
            preferences.addHistory(courses.get(position));
        }
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
