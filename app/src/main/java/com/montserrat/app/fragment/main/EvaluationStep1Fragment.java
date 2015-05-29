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

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.model.response.Candidate;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches PartialCourse for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends RecyclerViewFragment<AutoCompleteAdapter, Candidate> implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView(R.id.query) protected EditText query;
    @InjectView(R.id.query_result) protected RecyclerView queryResult;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.queryResult);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        Candidate candidate = items.get(position);
        EvaluationForm.getInstance().setLectureName(candidate.lecture_name);
        EvaluationForm.getInstance().setProfessorName(candidate.professor_name);
        if(candidate.course!=null) EvaluationForm.getInstance().setCourseId(candidate.course.id);
        else EvaluationForm.getInstance().setCourseId(0);

        ((InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (this.pagerController.getPreviousPage() == AppConst.ViewPager.Evaluation.EVALUATION_STEP2 || this.pagerController.getPreviousPage() == AppConst.ViewPager.Evaluation.EVALUATION_STEP3 ) {
            if (this.pagerController.getHistoryCopy().contains(AppConst.ViewPager.Evaluation.EVALUATION_STEP1)) this.pagerController.popCurrentPage();
            else this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true);
        } else this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP2, true);
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
        if(this.getUserVisibleHint()) this.onPageFocused();
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().clear();
        subscriptions.add(WidgetObservable
            .text(query)
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .map(toString)
            .flatMap(queryStr -> RetrofitApi.getInstance().search_autocomplete(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), queryStr))
            .map(response -> response.candidates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                lectures -> {
                    this.items.addAll(lectures);
                    this.adapter.notifyDataSetChanged();
                },
                error -> {
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                }
            )
        );
    }
}