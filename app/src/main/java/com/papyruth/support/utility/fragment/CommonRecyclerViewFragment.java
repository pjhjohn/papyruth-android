package com.papyruth.support.utility.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.EvaluationFragment;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.recyclerview.adapter.IAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.customview.EmptyStateView;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.MetricHelper;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.navigator.OnBack;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

/**
 *
 * use this Fragment when need to open EvaluationView.
 *
 */
public abstract class CommonRecyclerViewFragment<ADAPTER extends RecyclerView.Adapter<RecyclerView.ViewHolder>> extends ScrollableFragment implements OnBack, RecyclerViewItemObjectClickListener {
    protected Navigator mNavigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
        mNavigator = (Navigator) activity;
    }

    @Bind(R.id.common_swipe_refresh) protected SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.common_recycler_view) protected RecyclerView mRecyclerView;
    @Bind(R.id.common_empty_state_view)   protected EmptyStateView mEmptyState;
    protected CompositeSubscription mCompositeSubscription;
    protected Toolbar mToolbar;
    protected ADAPTER adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_recyclerview, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();

        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        mSwipeRefresh.setEnabled(true);
        initSwipeRefresh(mSwipeRefresh);

        mRecyclerView.setLayoutManager(this.getRecyclerViewLayoutManager());
        mRecyclerView.setAdapter(this.getAdapter());

        mEvaluationFragment = null;
        mEvaluationIsOccupying = false;
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionControl.getInstance().closeMenuButton(true);
        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEvaluationOpened = false;
        this.setToolbarStatus();
        this.setStatusBarDefault();

        if(adapter instanceof IAdapter) {
            mCompositeSubscription.add(getSwipeRefreshObservable(mSwipeRefresh).subscribe(unused -> ((IAdapter) adapter).refresh()));
            mCompositeSubscription.add(
                getRecyclerViewScrollObservable(mRecyclerView, mToolbar, true)
                    .filter(passIfNull -> passIfNull == null)
                    .subscribe(unused -> ((IAdapter) adapter).loadMore())
            );
        }
    }

    protected abstract void setFloatingActionControl();

    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }
    protected abstract void setToolbarStatus();
    protected abstract ADAPTER getAdapter();
    abstract protected void setStatusBarDefault();

    @Bind(R.id.common_evaluation_container) protected FrameLayout mEvaluationContainer;
    protected EvaluationFragment mEvaluationFragment;
    protected Boolean mEvaluationIsOccupying;
    protected Boolean mEvaluationOpened;

    @Override
    public boolean onBack() {
        if (!mEvaluationIsOccupying && mAnimatorSet == null) return false;
        if (!mEvaluationIsOccupying && !mAnimatorSet.isRunning()) return false;
        if (!mEvaluationIsOccupying) mAnimatorSet.cancel();
        else if(mAnimatorSet.isRunning()) mAnimatorSet.end();
        else if(!mEvaluationFragment.onBack()) this.closeEvaluation();
        return true;
    }

    // Animation
    protected Integer mItemTop, mItemHeight, mScreenHeight;
    protected AnimatorSet mAnimatorSet;
    protected Boolean mAnimationCanceled;
    protected void openEvaluation(View view, boolean animation) {
        try {
            if (view != null && animation)
                openEvaluation(view.getHeight(), (int) view.getY());
            else {
                mAnimationCanceled = false;
                mEvaluationOpened = true;
                mEvaluationIsOccupying = true;
                mAnimatorSet = new AnimatorSet();
                mScreenHeight = getActivity().findViewById(R.id.main_navigator).getBottom();
                mItemHeight = ((int) mContext.getResources().getDimension(R.dimen.cardview_min_height_extended));
                mItemTop = getActivity().findViewById(R.id.toolbar).getBottom();

                mEvaluationContainer.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams lpEvaluationContainer = mEvaluationContainer.getLayoutParams();
                lpEvaluationContainer.height = mScreenHeight;
                mEvaluationContainer.setLayoutParams(lpEvaluationContainer);

                if (mEvaluationFragment != null) {
                    mEvaluationFragment.setShowContentImmediately(true);
                    getFragmentManager().beginTransaction().add(R.id.common_evaluation_container, mEvaluationFragment).commit();
                    mEvaluationFragment.setEvaluationFloatingActionControl();
                }

                mEvaluationContainer.setY(0);
                mToolbar.setY(-mToolbar.getHeight());
                ToolbarHelper.hide(mToolbar);
                StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_evaluation);
            }
        }catch (Exception e){
            ErrorHandler.handle(e, this);
        }
    }

    protected void openEvaluation(int vHeight, int vY) {
        try {
            mEvaluationContainer.setVisibility(View.VISIBLE);
            mItemHeight = vHeight;
            mItemTop = vY;
            mScreenHeight = getActivity().findViewById(R.id.main_navigator).getBottom();
            ViewGroup.LayoutParams lpEvaluationContainer = mEvaluationContainer.getLayoutParams();

            ValueAnimator animHeight = ValueAnimator.ofInt(vHeight, mScreenHeight);
            animHeight.addUpdateListener(animator -> {
                lpEvaluationContainer.height = (int) animator.getAnimatedValue();
                mEvaluationContainer.setLayoutParams(lpEvaluationContainer);
            });

            ValueAnimator animTop = ValueAnimator.ofInt(mItemTop, 0);
            animTop.addUpdateListener(animator -> {
                final int itemTop = (int) animator.getAnimatedValue();
                mEvaluationContainer.setY(itemTop);
                final int toolbarTop = itemTop - MetricHelper.getPixels(mToolbar.getContext(), R.attr.actionBarSize);
                mToolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
            });

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setDuration(AppConst.ANIM_DURATION_MEDIUM);
            mAnimatorSet.playTogether(animHeight, animTop);
            mAnimatorSet.setInterpolator(new DecelerateInterpolator(AppConst.ANIM_DECELERATION));
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mEvaluationFragment != null) {
                        getFragmentManager().beginTransaction().add(R.id.common_evaluation_container, mEvaluationFragment).commit();
                    }
                    mAnimationCanceled = false;
                    mEvaluationOpened = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    if (mEvaluationFragment != null)
                        getFragmentManager().beginTransaction().remove(mEvaluationFragment).commit();
                    mAnimationCanceled = true;
                    mEvaluationOpened = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mAnimationCanceled) {
                        mToolbar.setY(0);
                    } else {
                        mEvaluationIsOccupying = true;
                        mEvaluationFragment.setEvaluationFloatingActionControl();
                        mEvaluationFragment.showContent(true);
                        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_evaluation);
                    }
                }
            });
            mAnimatorSet.start();
        } catch (Exception e) {
            ErrorHandler.handle(e, this);
        }
    }

    protected void closeEvaluation() {
        mEvaluationOpened = false;
        ViewGroup.LayoutParams lpEvaluationContainer = mEvaluationContainer.getLayoutParams();

        ValueAnimator animHeight = ValueAnimator.ofInt(mScreenHeight, mItemHeight);
        animHeight.addUpdateListener(animator -> {
            lpEvaluationContainer.height = (int) animator.getAnimatedValue();
            mEvaluationContainer.setLayoutParams(lpEvaluationContainer);
        });

        ValueAnimator animTop = ValueAnimator.ofInt(0, mItemTop);
        animTop.addUpdateListener(animator -> {
            final int itemTop = (int) animator.getAnimatedValue();
            mEvaluationContainer.setY(itemTop);
            final int toolbarTop = itemTop - MetricHelper.getPixels(mToolbar.getContext(), R.attr.actionBarSize);
            mToolbar.setY(toolbarTop >= 0 ? 0 : toolbarTop);
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setDuration(AppConst.ANIM_DURATION_SHORT);
        mAnimatorSet.playTogether(animHeight, animTop);
        mAnimatorSet.setInterpolator(new AccelerateInterpolator(AppConst.ANIM_ACCELERATION));
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mEvaluationFragment.showContent(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getFragmentManager().beginTransaction().remove(mEvaluationFragment).commit();
                mEvaluationContainer.setVisibility(View.GONE);
                mEvaluationIsOccupying = false;
                setFloatingActionControl();
                Evaluation.getInstance().clear();
                setStatusBarDefault();
            }
        });
        mAnimatorSet.start();
    }

}