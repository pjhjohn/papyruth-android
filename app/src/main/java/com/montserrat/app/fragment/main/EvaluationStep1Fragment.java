package com.montserrat.app.fragment.main;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.montserrat.app.R;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.search.ToolbarSearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches SimpleCourse for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends RecyclerViewFragment<AutoCompleteAdapter, Candidate> {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView(R.id.query) protected EditText query;
    @InjectView(R.id.query_result) protected RecyclerView queryResult;
    @InjectView(R.id.course_list) protected RecyclerView courseList;
    @InjectView(R.id.query_result_outside) protected RelativeLayout resultOutside;
    private CompositeSubscription subscriptions;

    private AutoCompletableSearchView search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        search = new AutoCompletableSearchView(this, this.getActivity().getBaseContext(), AutoCompletableSearchView.Type.EVALUATION);
        search.initAutoComplete(queryResult, resultOutside);
        search.initCourse(courseList);
        query.setOnKeyListener((v,keycode,e) ->{
            if(e.getAction() == KeyEvent.ACTION_DOWN) {
                Timber.d("***keydown %s", keycode);
                if (keycode == KeyEvent.KEYCODE_ENTER) {
                    Timber.d("***searchBtn");
                    this.search.showCandidates(false);
                    this.onQueryTextSubmit();
                    return true;
                }
            }
            return false;
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        this.adapter = null;
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }

    public void onQueryTextSubmit() {
//        this.query.clearFocus();
//        this.search.submit(query);
//        this.search.setEvaluationCandidate();
        this.search.submit();
    }
    @Override
    public void onRecyclerViewItemClick(View view, int position) {

        if(((RecyclerView)view.getParent()).getId() == queryResult.getId()) {
            this.search.setEvaluationCandidate(position);
            this.search.searchCourse();
            this.search.onRecyclerViewItemClick(view, position);
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        }else{
            this.search.onRecyclerViewItemClick(view, position);
            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());

            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);

            this.navigator.navigate(EvaluationStep2Fragment.class, true);

        }
    }

    @Override
    protected AutoCompleteAdapter getAdapter() {
        return new AutoCompleteAdapter(this.items, this);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().clear();
        this.query.clearFocus();
        this.search.autoComplete(query);
    }
    public void back(){
        this.search.showCandidates(false);
    }
}