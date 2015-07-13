package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-09.
 * Provides latest evaluations.
 */
public class HomeFragment extends RecyclerViewFragment<EvaluationItemsDetailAdapter, EvaluationData> {
    private Navigator navigator;
    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        navigator = (Navigator) activity;
    }

    @InjectView (R.id.recyclerview) protected RecyclerView evaluationOverview;
    @InjectView (R.id.swipe) protected SwipeRefreshLayout swipeRefresh;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private Integer sinceId = null, maxId = null;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.swipeRefresh.setEnabled(true);
        this.setupRecyclerView(this.evaluationOverview);
        this.setupSwipeRefresh(this.swipeRefresh);

        toolbar.setTitle(R.string.toolbar_title_home);
        toolbar.setTitleTextColor(Color.WHITE);

        return view;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        ButterKnife.reset(this);
        if (subscriptions == null || subscriptions.isUnsubscribed()) return;
        subscriptions.unsubscribe();
    }

    @Override
    protected EvaluationItemsDetailAdapter getAdapter () {
        return new EvaluationItemsDetailAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        // TODO : implement it!
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_new_evaluation)
            .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN))
        );

        this.subscriptions.add(super.getRefreshObservable(this.swipeRefresh)
            .flatMap(unused -> {
                this.swipeRefresh.setRefreshing(true);
                return RetrofitApi.getInstance().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null);
            })
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.swipeRefresh.setRefreshing(false);
                this.items.clear();
                if (evaluations != null)
                    this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
            })
        );

        this.subscriptions.add(super.getRecyclerViewScrollObservable(this.evaluationOverview, this.toolbar, true)
            .startWith((Boolean) null)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                // TODO : handle the case for max_id == 0 : prefer not to request to server
                return RetrofitApi.getInstance().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, sinceId == null ? null : sinceId - 1, null, null);
            })
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.progress.setVisibility(View.GONE);
                if (evaluations != null) this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
                // TODO : Implement Better Algorithm
                for (EvaluationData evaluation : evaluations) {
                    final int id = evaluation.id;
                    if (maxId == null) maxId = id;
                    else if (maxId < id) maxId = id;
                    if (sinceId == null) sinceId = id;
                    else if (sinceId > id) sinceId = id;
                }
            })
        );
    }
}