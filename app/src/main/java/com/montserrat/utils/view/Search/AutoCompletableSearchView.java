package com.montserrat.utils.view.Search;

import android.content.Context;
import android.hardware.input.InputManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.functions.Action0;
import rx.functions.Func0;
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

    public AutoCompletableSearchView(RecyclerViewClickListener listener, Context context){
        this.courses = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.subscription = new CompositeSubscription();
        this.itemListener = listener;
        this.context = context;
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

    public void courseSetup(List<PartialCourse> courses, RecyclerView courseListView){
        this.courses.addAll(courses);
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
                                    if (query.length() < 1) {
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
                                    candidates.clear();
                                }
                        );
    }

    public Subscription submit(View view, String query){
        return ViewObservable
                        .clicks(view)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .map(click -> query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> Timber.d("submit query : %s", result),
                                error -> Timber.d("submit error : %s", error)
                        );
    }

    @Override
    public void onClick(View v) {
        expandResult(false);
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {

        Candidate item = candidates.get(position);
        Search.getInstance().clear()
            .setCourse(item.course)
            .setLectureId(item.lecture_id)
            .setLectureName(item.lecture_name)
            .setProfessorId(item.professor_id)
            .setProfessorName(item.professor_name);
        this.expandResult(false);
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
