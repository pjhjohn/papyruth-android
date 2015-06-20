package com.montserrat.app.fragment.main;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.montserrat.app.R;
import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;

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
        search.autoCompleteSetup(queryResult, resultOutside);
        search.courseSetup(courseList);

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
    public void recyclerViewListClicked(View view, int position) {

        if(((RecyclerView)view.getParent()).getId() == queryResult.getId()) {
            this.search.setEvaluationCandidate(position);
            this.search.searchCourse(AutoCompletableSearchView.Type.EVALUATION);
            this.search.showCandidates(false);
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        }else{
            this.search.recyclerViewListClicked(view, position);
            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());

            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);

            this.navigator.navigate(EvaluationStep2Fragment.class, true);
//            if(EvaluationStep2Fragment.class.getName().equals(this.navigator.getBackStackNameAt(1)) ||
//               EvaluationStep3Fragment.class.getName().equals(this.navigator.getBackStackNameAt(2))) {
//                if(!this.navigator.getManager.popBackStack(SOME_FLAG)) this.navigator.navigate(EvaluationStep2Fragment.class, true);
//            } else this.navigator.navigate(EvaluationStep2Fragment.class, true);
        }
    }

    @Override
    protected AutoCompleteAdapter getAdapter(List<Candidate> candidates) {
        return new AutoCompleteAdapter(candidates, this);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().clear();
        this.search.autoComplete(query);
    }
}