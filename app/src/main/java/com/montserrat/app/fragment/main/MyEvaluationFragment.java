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

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyEvaluationAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MyEvaluationFragment extends RecyclerViewFragment<MyEvaluationAdapter, EvaluationData>{
    private Navigator navigator;

    int page = 1;
    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        navigator = (Navigator) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    @InjectView (R.id.recyclerview) protected RecyclerView evaluationsRecyclerView;
    @InjectView (R.id.swipe) protected SwipeRefreshLayout swipeRefresh;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_written, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.swipeRefresh.setEnabled(true);
        this.setupRecyclerView(this.evaluationsRecyclerView);
        this.setupSwipeRefresh(this.swipeRefresh);

        toolbar.setTitle(R.string.my_evaluation);
        toolbar.setTitleTextColor(Color.WHITE);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_EASINESS).start();

        return view;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        ButterKnife.reset(this);
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN));

        this.subscriptions.add(super.getRefreshObservable(this.swipeRefresh)
            .flatMap(unused -> {
                this.swipeRefresh.setRefreshing(true);
                return RetrofitApi.getInstance().users_me_evaluations(User.getInstance().getAccessToken(), page = 1);
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
            },error -> {
                this.swipeRefresh.setRefreshing(false);
                error.printStackTrace();
            })
        );

        this.subscriptions.add(super.getRecyclerViewScrollObservable(this.evaluationsRecyclerView, this.toolbar, true)
            .startWith((Boolean) null)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                this.swipeRefresh.setRefreshing(false);
                // TODO : handle the case for max_id == 0 : prefer not to request to server
                return RetrofitApi.getInstance().users_me_evaluations(User.getInstance().getAccessToken(), page++);
            })
            .map(mywritten -> mywritten.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.progress.setVisibility(View.GONE);
                if (evaluations != null) this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
            },error -> {
                this.progress.setVisibility(View.GONE);
                error.printStackTrace();
            })
        );
    }



    @Override
    protected MyEvaluationAdapter getAdapter () {
        return new MyEvaluationAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }
}