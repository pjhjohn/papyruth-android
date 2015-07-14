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
import com.montserrat.app.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.MetricUtil;
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

public class HomeFragment extends RecyclerViewFragment<EvaluationItemsDetailAdapter, EvaluationData> implements OnBack{
    private Navigator navigator;
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
    @InjectView(R.id.evaluation_container) protected FrameLayout evaluationContainer;
    @InjectView (R.id.swipe) protected SwipeRefreshLayout swipeRefresh;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private Integer sinceId = null, maxId = null;
    private EvaluationFragment evaluationDetail;
    private Boolean isEvaluationDetailOpened;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.swipeRefresh.setEnabled(true);
        this.setupRecyclerView(this.evaluationsRecyclerView);
        this.setupSwipeRefresh(this.swipeRefresh);

        toolbar.setTitle(R.string.toolbar_title_home);
        toolbar.setTitleTextColor(Color.WHITE);

        this.isEvaluationDetailOpened = false;

        return view;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        if(this.getActivity() != null && this.isEvaluationDetailOpened) this.getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
        ButterKnife.reset(this);
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
        if(isEvaluationDetailOpened) return;
        if(animators != null && animators.isRunning()) return;
        RetrofitApi.getInstance()
            .get_evaluation(User.getInstance().getAccessToken(), this.items.get(position).id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                Evaluation.getInstance().update(response.evaluation);
                this.openEvaluation(view);
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.evaluationDetail = new EvaluationFragment();
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

        this.subscriptions.add(super.getRecyclerViewScrollObservable(this.evaluationsRecyclerView, this.toolbar, true)
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

    @Override
    public boolean onBack() {
        if (!isEvaluationDetailOpened && animators == null) return false;
        if (!isEvaluationDetailOpened && !animators.isRunning()) return false;
        if (!isEvaluationDetailOpened ) animators.cancel();
        else if(animators.isRunning()) animators.end();
        else if(!evaluationDetail.onBack()) this.closeEvaluation();
        return true;
    }

    // Animation
    private Integer itemTop, itemHeight, screenHeight;
    private AnimatorSet animators;
    private Boolean isAnimationCanceled;
    private void openEvaluation(View view) {
        this.evaluationContainer.setVisibility(View.VISIBLE);
        if(this.getView() != null) this.screenHeight = this.getView().getHeight();
        this.itemHeight = view.getHeight();
        this.itemTop = (int) view.getY();

        ViewGroup.LayoutParams lpEvaluationContainer = evaluationContainer.getLayoutParams();

        ValueAnimator animHeight = ValueAnimator.ofInt(view.getHeight(), screenHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.evaluationContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(this.itemTop, 0);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.evaluationContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(this.toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        ValueAnimator animAlpha = ValueAnimator.ofFloat(0f, 1f);
        animAlpha.addUpdateListener(animator -> {
            final float alpha = (float) animator.getAnimatedValue();
            this.evaluationContainer.setAlpha(alpha);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop, animAlpha);
        animators.setInterpolator(new AccelerateInterpolator(AppConst.ANIM_DECELERATION));
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getFragmentManager().beginTransaction().add(R.id.evaluation_container, evaluationDetail).commit();
                isAnimationCanceled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
                isAnimationCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isAnimationCanceled) toolbar.setY(0);
                else {
                    isEvaluationDetailOpened = true;
                    evaluationDetail.setEvaluationFloatingActionControl();
                }
            }

        });
        animators.start();
    }

    private void closeEvaluation() {
        ViewGroup.LayoutParams lpEvaluationContainer = this.evaluationContainer.getLayoutParams();

        ValueAnimator animHeight = ValueAnimator.ofInt(this.screenHeight, this.itemHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            this.evaluationContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(0, this.itemTop);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            this.evaluationContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricUtil.getPixels(toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        ValueAnimator animAlpha = ValueAnimator.ofFloat(1f, 0f);
        animAlpha.addUpdateListener(animator -> {
            final float alpha = (float) animator.getAnimatedValue();
            this.evaluationContainer.setAlpha(alpha);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_MEDIUM);
        animators.playTogether(animHeight, animTop, animAlpha);
        animators.setInterpolator(new DecelerateInterpolator(AppConst.ANIM_ACCELERATION));
        animators.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getFragmentManager().beginTransaction().remove(evaluationDetail).commit();
                evaluationContainer.setVisibility(View.GONE);
                isEvaluationDetailOpened = false;
                FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
                subscriptions.add(FloatingActionControl
                        .clicks(R.id.fab_new_evaluation)
                        .subscribe(unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN))
                );
            }
        });
        animators.start();
    }
}