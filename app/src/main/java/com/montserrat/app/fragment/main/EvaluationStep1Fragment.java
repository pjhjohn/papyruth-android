package com.montserrat.app.fragment.main;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

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

    @InjectView(R.id.queryTextView) protected EditText queryTextView;
    @InjectView(R.id.query_result) protected RecyclerView queryResult;
    @InjectView(R.id.course_list) protected RecyclerView courseList;
    @InjectView(R.id.query_result_outside) protected RelativeLayout resultOutside;
    private CompositeSubscription subscriptions;

    private AutoCompletableSearchView search;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        this.search = new AutoCompletableSearchView(this, this.getActivity().getBaseContext(), AutoCompletableSearchView.Type.EVALUATION);
        this.search.initAutoComplete(this.queryResult, this.resultOutside);
        this.search.initCourse(this.courseList);
        this.queryTextView.setOnKeyListener((v, keycode, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                if (keycode == KeyEvent.KEYCODE_ENTER) {
                    this.search.querySubmit();
                    return true;
                }
            }
            return false;
        });

        if(this.search.hasData())
            this.search.searchCourse();

        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title_new_evaluation);
        toolbar.setTitleTextColor(Color.WHITE);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_CLARITY).start();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        this.adapter = null;
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }


    @Override
    public void onRecyclerViewItemClick(View view, int position) {

        if(((RecyclerView)view.getParent()).getId() == this.queryResult.getId()) {
            this.search.setEvaluationCandidate(position);
            this.search.searchCourse();
            this.search.onRecyclerViewItemClick(view, position);
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        }else{
            this.search.onRecyclerViewItemClick(view, position);
            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessorName());
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
        if(toolbar.getY() < 0) ToolbarUtil.show(toolbar);
        FloatingActionControl.getInstance().clear();
        this.search.autoComplete(this.queryTextView);
    }
    public void back(){
        if(toolbar.getY() < 0) ToolbarUtil.show(toolbar);
        this.search.onBack();
    }
}