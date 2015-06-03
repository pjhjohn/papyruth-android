package com.montserrat.app.fragment.main;

import android.app.Activity;
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
import com.montserrat.app.adapter.PartialEvaluationAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;

import java.util.List;
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
public class HomeFragment extends RecyclerViewFragment<PartialEvaluationAdapter, PartialEvaluation> implements OnPageFocus {
    private ViewPagerContainerController controller;
    private NavFragment.OnCategoryClickListener callback;

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        this.controller = (ViewPagerContainerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @InjectView (R.id.recyclerview) protected RecyclerView recycler;
    @InjectView (R.id.swipe) protected SwipeRefreshLayout refresh;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private Integer since = null, max = null;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.refresh.setEnabled(true);
        this.setupRecyclerView(this.recycler);

        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setupSwipeRefresh(this.refresh);
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        ButterKnife.reset(this);
        if (this.subscriptions != null && !this.subscriptions.isUnsubscribed())
            this.subscriptions.unsubscribe();
    }

    @Override
    protected PartialEvaluationAdapter getAdapter (List<PartialEvaluation> evaluations) {
        return PartialEvaluationAdapter.newInstance(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        // TODO : implement it!
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) this.onPageFocused();
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_new_evaluation)
            .subscribe(unused -> this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP1), true))
        );

        this.subscriptions.add(super.getRefreshObservable(this.refresh)
            .flatMap(unused -> {
                this.refresh.setRefreshing(true);
                return RetrofitApi.getInstance().evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null);
            })
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.refresh.setRefreshing(false);
                this.items.clear();
                if (evaluations != null)
                    this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
            })
        );

        this.subscriptions.add(super.getRecyclerViewScrollObservable(this.recycler, this.toolbar, true)
            .filter(askmoreifnull -> askmoreifnull == null && this.progress.getVisibility() != View.VISIBLE)
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                return RetrofitApi.getInstance().evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null);
            })
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.progress.setVisibility(View.GONE);
                if (evaluations != null) this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
            })
        );
    }
}