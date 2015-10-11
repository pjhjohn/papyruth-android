package com.montserrat.app.fragment.main;


import android.app.Activity;
import android.content.Context;
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
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.search.AutoCompletableSearchView2;

import java.util.ArrayList;
import java.util.List;

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

    private AutoCompletableSearchView2 search;
    private Toolbar toolbar;

    private List<CourseData> courses;
    private CourseItemsAdapter courseAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.courses = new ArrayList<>();
        this.courseAdapter = new CourseItemsAdapter(this.courses, this, R.layout.cardview_header_height_zero);

        this.search = new AutoCompletableSearchView2(this, this.getActivity().getBaseContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.queryResult);

        this.search.initAutocompleteView(this.queryResult, this.resultOutside, this.queryTextView, getAdapter(), items);
        this.search.initResultView(this.courseList, courseAdapter, courses);

        this.queryTextView.setOnKeyListener((v, keycode, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) {
                    this.search.submitQuery();
                    return true;
            }
            return false;
        });

        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_EASINESS).start();

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
            this.search.searchCourse(this.items.get(position).lecture_id, this.items.get(position).professor_id, null);
        }else{
            EvaluationForm.getInstance().setLectureName(this.courses.get(position).name);
            EvaluationForm.getInstance().setProfessorName(this.courses.get(position).professor_name);
            EvaluationForm.getInstance().setCourseId(this.courses.get(position).id);

            this.navigator.navigate(EvaluationStep2Fragment.class, true);
        }
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
    }

    @Override
    protected AutoCompleteAdapter getAdapter() {
        if(this.adapter != null)
            return this.adapter;
        return new AutoCompleteAdapter(this.items, this);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().clear();
    }
    public void back(){
        this.search.onBack();
    }
}