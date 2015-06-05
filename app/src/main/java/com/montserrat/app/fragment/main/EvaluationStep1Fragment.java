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

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
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
    private ViewPagerContainerController controller;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.controller = (ViewPagerContainerController) activity;
    }

    @InjectView(R.id.query) protected EditText query;
    @InjectView(R.id.query_result) protected RecyclerView queryResult;
    @InjectView(R.id.course_list) protected RecyclerView courseList;
    @InjectView(R.id.query_result_outside) protected RelativeLayout resultOutside;
    private CompositeSubscription subscriptions;
    private PartialCourseAdapter partialCourseAdapter;
    private List<PartialCourse> itemList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.queryResult);
        this.itemList = new ArrayList<>();
        this.partialCourseAdapter = new PartialCourseAdapter(itemList, this);
        this.courseList.setLayoutManager(new LinearLayoutManager(this.getActivity().getBaseContext()));
        this.courseList.setAdapter(this.partialCourseAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        this.adapter = null;
        items.clear();
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        Timber.d("view : %s %s", ((RecyclerView)view.getParent()).getId(), queryResult.getId());

        if(((RecyclerView)view.getParent()).getId() == queryResult.getId()) {
            Candidate candidate = items.get(position);
            this.subscriptions.add(
                    RetrofitApi.getInstance().search_search(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        candidate.lecture_id,
                        candidate.professor_id,
                        null
                    )
                    .map(response -> response.courses)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(courses ->{
                                this.itemList.clear();
                                this.itemList.addAll(courses);
                                Timber.d("running well");
                                this.partialCourseAdapter.notifyDataSetChanged();
                            }, error -> {
                                Timber.d("search course error : %s %s", error);
                            }
                            )
            );
            expandResult(false);
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }else{
            PartialCourse course = itemList.get(position);
            EvaluationForm.getInstance().setLectureName(course.name);
            EvaluationForm.getInstance().setProfessorName(course.professor_name);
            EvaluationForm.getInstance().setCourseId(course.id);

            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);

            if (Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP2).equals(this.controller.getPreviousPage()) ||
                    Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP3).equals(this.controller.getPreviousPage())) {
                if (this.controller.getHistoryCopy().contains(Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP1)))
                    this.controller.popCurrentPage();
                else
                    this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP2), true);
            } else
                this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP2), true);
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
        if(this.getUserVisibleHint()) this.onPageFocused();
    }

    private void expandResult(boolean expand){
        Timber.d("%s", expand);
        if(expand){
            ViewGroup.LayoutParams param =  queryResult.getLayoutParams();
            param.height = 700;
            param.width = (int)(this.getResources().getDisplayMetrics().widthPixels * 0.8);
            queryResult.setLayoutParams(param);
            queryResult.setY(query.getY() + query.getLayoutParams().height);


            param =  resultOutside.getLayoutParams();
            param.height = this.getResources().getDisplayMetrics().heightPixels;
            param.width = this.getResources().getDisplayMetrics().widthPixels;
            resultOutside.setY(query.getY()+query.getLayoutParams().height);
            Timber.d("height %s %s %s %s", queryResult.getLayoutParams().height, queryResult.getY(), resultOutside.getY(), query.getY());

            resultOutside.setLayoutParams(param);
        }else{
            ViewGroup.LayoutParams param =  queryResult.getLayoutParams();
            param.height = 0;
            param.width = (int)(this.getResources().getDisplayMetrics().widthPixels * 0.8);
            queryResult.setLayoutParams(param);

            param =  resultOutside.getLayoutParams();
            param.height = 0;
            param.width = this.getResources().getDisplayMetrics().widthPixels;
            resultOutside.setY(query.getY()+query.getLayoutParams().height);

            resultOutside.setLayoutParams(param);
        }
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
                        .filter(candidates -> candidates.size() > 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                lectures -> {
                                    this.items.clear();
                                    this.items.addAll(lectures);
                                    this.adapter.notifyDataSetChanged();
                                    expandResult(true);
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
        courseList.setY(query.getY()+query.getLayoutParams().height);
        this.subscriptions.add(ViewObservable
                .clicks(resultOutside)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused ->
                                expandResult(false)
                )

        );
    }
}