package com.montserrat.utils.view.Search;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by SSS on 2015-06-06.
 */
public class AutoCompletableSearchView implements View.OnClickListener, RecyclerViewClickListener{
    private CompositeSubscription subscription;
    private RecyclerView autocompleteView;
    private RecyclerView courseListView;
    private View outsideView;
    private List<Candidate> candidates;
    private List<PartialCourse> courses;
    private PartialCourseAdapter partialCourseAdapter;
    private AutoCompleteAdapter autoCompleteAdapter;
    private RecyclerViewClickListener itemListener;
    private Context context;

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
    }

    public void autoCompleteSetup(RecyclerView autocompleteView, View outsideView){
        this.autocompleteView = autocompleteView;
        this.autoCompleteAdapter = new AutoCompleteAdapter(this.candidates, this.itemListener);
        this.autocompleteView.setLayoutManager(new LinearLayoutManager(context));
        this.autocompleteView.setAdapter(this.autoCompleteAdapter);
        this.outsideView = outsideView;
        this.outsideView.setOnClickListener(this);
    }

    public void notifyAutocompleteChanged(List <Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
    }

    public void courseSetup(RecyclerView courseListView){
        this.courseListView = courseListView;
        this.partialCourseAdapter = new PartialCourseAdapter(this.courses, this.itemListener);
        this.courseListView.setLayoutManager(new LinearLayoutManager(context));
        this.courseListView.setAdapter(this.partialCourseAdapter);
    }

    public void notifycourseChanged(List<PartialCourse> courses){
        this.courses.clear();
        this.courses.addAll(courses);
        this.partialCourseAdapter.notifyDataSetChanged();
    }

    public Subscription autoComplete(TextView textView){
        return WidgetObservable
                        .text(textView)
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .map(listener -> listener.text().toString())
                        .flatMap(query -> {
                                    if ( !(type == Type.EVALUATION) && query.length() < 1) {
                                        return null;    // history
                                    }
                                    return RetrofitApi.getInstance().search_autocomplete(
                                            User.getInstance().getAccessToken(),
                                            User.getInstance().getUniversityId(),
                                            query
                                    );
                                }
                        )
                        .map(response -> response.candidates)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                results -> {
                                    this.notifyAutocompleteChanged(results);
                                    expandResult(true);
                                },
                                error -> {
                                    if (error instanceof RetrofitError) {
                                        switch (((RetrofitError) error).getResponse().getStatus()) {
                                            default:
                                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                                        }
                                    }else
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

    public void searchCourse(Type type) {
        if (type == Type.EVALUATION){
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
                            this::notifycourseChanged
                            , error ->
                                Timber.d("search course error : %s %s", error)

                            )
            );
        }else {
            RetrofitApi.getInstance().search_search(
                    User.getInstance().getAccessToken(),
                    User.getInstance().getUniversityId(),
                    Search.getInstance().getLectureId(),
                    Search.getInstance().getProfessorId(),
                    Search.getInstance().getQuery())
                    .map(response -> response.courses)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        this::notifycourseChanged
                    , error ->
                        Timber.d("Search error : %s", error)
                    );
        }
    }

    public List<PartialCourse> getHistory(){
        Timber.d("getHistory");
        // TODO : implement it!
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        Gson data = new Gson();
        String json = preferences.getString("object1", "");
        PartialCourse course = new PartialCourse();
        course = data.fromJson(json, PartialCourse.class);

        return null;
    }

    public boolean addHistory(PartialCourse item){
        Timber.d("getHistory");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        List<PartialCourse> list = getHistory();

        SharedPreferences.Editor editor = preferences.edit();
        Gson data = new Gson();
        String json = data.toJson(courses.get(0));
        editor.putString("object1", json);
        editor.apply();
        return false;
    }

    @Override
    public void onClick(View v) {
        expandResult(false);
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        if(autocompleteView != null && ((RecyclerView)view.getParent()).getId() == autocompleteView.getId()) {
            Search.getInstance().fromCandidate(candidates.get(position));
            this.expandResult(false);
        }else if(courseListView != null && ((RecyclerView)view.getParent()).getId() == courseListView.getId()){
            Course.getInstance().clear().fromPartailCourse(courses.get(position));
        }
    }

    public void expandResult(boolean expand){
        if(expand){
            ViewGroup.LayoutParams param =  autocompleteView.getLayoutParams();
            param.height = 250;
            param.width = (int)(this.context.getResources().getDisplayMetrics().widthPixels * 0.8);
            autocompleteView.setLayoutParams(param);

            param =  outsideView.getLayoutParams();
            param.height = this.context.getResources().getDisplayMetrics().heightPixels;
            param.width = this.context.getResources().getDisplayMetrics().widthPixels;

            outsideView.setLayoutParams(param);
        }else{
            ViewGroup.LayoutParams param =  autocompleteView.getLayoutParams();
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
