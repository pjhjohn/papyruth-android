package com.papyruth.support.utility.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
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

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.EvaluationFragment;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.helper.MetricHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.viewpager.OnBack;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 *
 * use this Fragment when need to open EvaluationView.
 *
 */
public abstract class CommonRecyclerViewFragment<ADAPTER extends RecyclerView.Adapter<RecyclerView.ViewHolder>, ITEM> extends RecyclerViewFragment<ADAPTER, ITEM> implements OnBack {
    protected Navigator navigator;

    protected int page = 1;
    private Integer commentPosition = null;

    @InjectView(R.id.recyclerview) protected RecyclerView recyclerView;
    @InjectView (R.id.swipe) protected SwipeRefreshLayout swipeRefresh;
    @InjectView (R.id.material_progress_large) protected View progress;
    protected CompositeSubscription subscriptions;
    protected Toolbar toolbar;


    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        navigator = (Navigator) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.isOpenSlave = false;
        if(Evaluation.getInstance().getId() != null){
            this.slave = new EvaluationFragment();
            this.openEvaluation(null, false);
        }else{
            setFloatingActionControl();
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_recyclerview, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.swipeRefresh.setEnabled(true);
        this.setupRecyclerView(this.recyclerView);
        this.setupSwipeRefresh(this.swipeRefresh);


        this.slave = null;
        this.slaveIsOccupying = false;
        return view;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        FloatingActionControl.getInstance().closeMenuButton(true);
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        ButterKnife.reset(this);
    }


    @InjectView(R.id.evaluation_container) protected FrameLayout slaveContainer;
    protected EvaluationFragment slave;
    protected Boolean  slaveIsOccupying;
    protected Boolean isOpenSlave;

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
    protected Integer itemTop, itemHeight, screenHeight;
    protected AnimatorSet animators;
    protected Boolean isAnimationCanceled;
    protected void openEvaluation(View view, boolean animation) {
        try {
            if (view != null && animation)
                openEvaluation(view.getHeight(), (int) view.getY());
            else {
                isAnimationCanceled = false;
                isOpenSlave = true;
                slaveIsOccupying = true;
                animators = new AnimatorSet();
                this.screenHeight = getActivity().findViewById(R.id.main_navigator).getBottom();
                this.itemHeight = ((int) getActivity().getResources().getDimension(R.dimen.cardview_min_height_extended));
                this.itemTop = getActivity().findViewById(R.id.toolbar).getBottom();

                this.slaveContainer.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams lpEvaluationContainer = slaveContainer.getLayoutParams();
                lpEvaluationContainer.height = screenHeight;
                this.slaveContainer.setLayoutParams(lpEvaluationContainer);

                if (slave != null) {
                    slave.setShowContentImmediately(true);
                    getFragmentManager().beginTransaction().add(R.id.evaluation_container, slave).commit();

                    slave.setEvaluationFloatingActionControl();
                }

                this.slaveContainer.setY(0);
                this.toolbar.setY(-toolbar.getHeight());
                ToolbarHelper.hide(toolbar);
            }
        }catch (Exception e){
            ErrorHandler.handle(e, this);
        }
    }

    protected void openEvaluation(int vHeight, int vY){
        try {
            this.slaveContainer.setVisibility(View.VISIBLE);
            this.itemHeight = vHeight;
            this.itemTop = vY;
            this.screenHeight = getActivity().findViewById(R.id.main_navigator).getBottom();
            ViewGroup.LayoutParams lpEvaluationContainer = slaveContainer.getLayoutParams();

            ValueAnimator animHeight = ValueAnimator.ofInt(vHeight, screenHeight);
            animHeight.addUpdateListener(animator -> {
                lpEvaluationContainer.height = (int) animator.getAnimatedValue();
                this.slaveContainer.setLayoutParams(lpEvaluationContainer);
            });

            ValueAnimator animTop = ValueAnimator.ofInt(this.itemTop, 0);
            animTop.addUpdateListener(animator -> {
                final int itemTop = (int) animator.getAnimatedValue();
                this.slaveContainer.setY(itemTop);
                final int toolbarTop = itemTop - MetricHelper.getPixels(this.toolbar.getContext(), R.attr.actionBarSize);
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
                    if (slave != null) {
                        getFragmentManager().beginTransaction().add(R.id.evaluation_container, slave).commit();
                        if (commentPosition != null)
                            slave.setCommentId(commentPosition);
                    }
                    isAnimationCanceled = false;
                    isOpenSlave = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    if (slave != null)
                        getFragmentManager().beginTransaction().remove(slave).commit();
                    isAnimationCanceled = true;
                    isOpenSlave = false;
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
        }catch (Exception e){
            ErrorHandler.handle(e, this);
        }
    }

    protected void closeEvaluation() {
        this.isOpenSlave = false;
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
            final int toolbarTop = itemTop - MetricHelper.getPixels(toolbar.getContext(), R.attr.actionBarSize);
            this.toolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        animators = new AnimatorSet();
        animators.setDuration(AppConst.ANIM_DURATION_SHORT);
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
                setFloatingActionControl();
                Evaluation.getInstance().clear();
            }
        });
        animators.start();
    }

    /**
     * if commentPosition is not null, comment focus In EvaluationFragment
     */
    protected void setCommentPosition(Integer position){
        this.commentPosition = position;
    }

    protected abstract void setFloatingActionControl();

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

}