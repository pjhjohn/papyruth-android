package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyEvaluationAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.MetricUtil;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnBack;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MyEvaluationFragment extends RecyclerViewFragment<MyEvaluationAdapter, EvaluationData> implements OnBack {
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

        this.slave = null;
        this.slaveIsOccupying = false;
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

        if(slaveIsOccupying) return;
        if(animators != null && animators.isRunning()) return;
        RetrofitApi.getInstance()
            .get_evaluation(User.getInstance().getAccessToken(), this.items.get(position).id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                Evaluation.getInstance().update(response.evaluation);
                this.slave = new EvaluationFragment();
                this.openEvaluation(view);
            });
    }

    @Override
    public void onResume() {
        super.onResume();
//        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
//        FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN));

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
            }, () ->{
                this.swipeRefresh.setRefreshing(false);
                this.progress.setVisibility(View.GONE);
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
            }, () ->{
                this.swipeRefresh.setRefreshing(false);
                this.progress.setVisibility(View.GONE);
            })
        );
    }

    @InjectView(R.id.evaluation_container) protected FrameLayout slaveContainer;
    private EvaluationFragment slave;
    private Boolean  slaveIsOccupying;

    @Override
    public boolean onBack() {
        if (!slaveIsOccupying && animators == null) return false;
        if (!slaveIsOccupying && !animators.isRunning()) return false;
        if (!slaveIsOccupying) animators.cancel();
        else if(animators.isRunning()) animators.end();
        else if(!slave.onBack()) this.closeEvaluation();
        return true;
    }

    // Animation
    private Integer itemTop, itemHeight, screenHeight;
    private AnimatorSet animators;
    private Boolean isAnimationCanceled;
    private void openEvaluation(View view) {
        this.slaveContainer.setVisibility(View.VISIBLE);
        if(this.getView() != null) this.screenHeight = this.getView().getHeight();
        this.itemHeight = view.getHeight();
        this.itemTop = (int) view.getY();

        ViewGroup.LayoutParams lpEvaluationContainer = slaveContainer.getLayoutParams();

        ValueAnimator animHeight = ValueAnimator.ofInt(view.getHeight(), screenHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.slaveContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(this.itemTop, 0);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.slaveContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(this.toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop);
        animators.setInterpolator(new DecelerateInterpolator(AppConst.ANIM_DECELERATION));
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if(slave != null)getFragmentManager().beginTransaction().add(R.id.evaluation_container, slave).commit();
                isAnimationCanceled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if(slave != null) getFragmentManager().beginTransaction().remove(slave).commit();
                isAnimationCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isAnimationCanceled) {
                    toolbar.setY(0);
                } else {
                    slaveIsOccupying = true;
                    slave.setEvaluationFloatingActionControl();
                    slave.showContent(true);
                }
            }

        });
        animators.start();
    }

    private void closeEvaluation() {
        ViewGroup.LayoutParams lpEvaluationContainer = this.slaveContainer.getLayoutParams();

        ValueAnimator animHeight = ValueAnimator.ofInt(this.screenHeight, this.itemHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.slaveContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(0, this.itemTop);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.slaveContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop);
        animators.setInterpolator(new AccelerateInterpolator(AppConst.ANIM_ACCELERATION));
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                slave.showContent(false);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                getFragmentManager().beginTransaction().remove(slave).commit();
                slaveContainer.setVisibility(View.GONE);
                slaveIsOccupying = false;
                FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
                FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN));
            }
        });
        animators.start();
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