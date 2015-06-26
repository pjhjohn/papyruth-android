package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.adapter.SimpleCourseAdapter;
import com.montserrat.app.model.CourseData;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Simple Course Fragment for listing limited contents for Course
 * TODO : should be able to expand when clicking recyclerview item to show evaluation data in detail
 */

public class SimpleCourseFragment extends RecyclerViewFragment<SimpleCourseAdapter, CourseData> {
    private Navigator navigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView(R.id.recyclerview) protected RecyclerView recycler;
    @InjectView(R.id.swipe) protected SwipeRefreshLayout refresh;
    @InjectView(R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private AutoCompletableSearchView search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_search, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.refresh.setEnabled(true);
        this.search = new AutoCompletableSearchView(this, this.getActivity().getBaseContext(), AutoCompletableSearchView.Type.SEARCH);
        if(this.getArguments() != null && this.getArguments().containsKey(AppConst.Preference.SEARCH))
            this.search.setSearchMode(this.getArguments().getBoolean(AppConst.Preference.SEARCH));

        this.search.initCourse(this.recycler);
        ((MainActivity)this.getActivity()).setFragmentAutoCompletableSearchView(this);
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setupSwipeRefresh(this.refresh);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);

        ((MainActivity)this.getActivity()).setFragmentAutoCompletableSearchView(null);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    protected SimpleCourseAdapter getAdapter () {
        return new SimpleCourseAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        this.search.onRecyclerViewItemClick(view, position);
        this.navigator.navigate(CourseFragment.class, true);
    }

    public void reloadFragment(){
        ((MainActivity)this.getActivity()).reloadFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_new_evaluation)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true))
        );
        this.subscriptions.add(
            this.getRefreshObservable(this.refresh)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    courses -> {
                        this.refresh.setRefreshing(false);
                        this.search.searchCourse();
                    },
                    error -> Timber.d("search error : %s", error)
                )
        );

        this.search.searchCourse();
    }
}