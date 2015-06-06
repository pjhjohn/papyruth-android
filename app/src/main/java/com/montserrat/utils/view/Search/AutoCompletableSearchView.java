package com.montserrat.utils.view.Search;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void onClick(View v) {
        Timber.d("click!!");
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {

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
    //NEED
    //layoutmanager
    //setAdapter

}
