package com.papyruth.android.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.github.clans.fab.FloatingActionButton;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.EvaluationAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.EmptyStateView;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.ScrollableFragment;
import com.papyruth.support.utility.helper.MetricHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.navigator.OnBack;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.codetail.animation.SupportAnimator;
import io.codetail.widget.RevealFrameLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EvaluationFragment extends ScrollableFragment implements RecyclerViewItemObjectClickListener, OnBack {
    private Navigator mNavigator;
    private RevealFrameLayout mCommentContainer;
    private RelativeLayout mCommentInput;
    private EditText mCommentText;
    private ImageButton mCommentSubmit;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator          = (Navigator) activity;
        mCommentContainer   = (RevealFrameLayout) activity.findViewById(R.id.comment_container);
        mCommentInput       = (RelativeLayout) activity.findViewById(R.id.comment_input);
        mCommentText        = (EditText) activity.findViewById(R.id.comment_text);
        mCommentSubmit      = (ImageButton) activity.findViewById(R.id.comment_submit);
    }

    @Bind(R.id.evaluation_swipe_refresh)  protected SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.evaluation_recycler_view)  protected RecyclerView mRecyclerView;
    @Bind(R.id.evaluation_step_empty_state_view)    protected EmptyStateView mEmptyState;
    @Bind(R.id.evaluation_toolbar)        protected Toolbar mToolbar;
    private CompositeSubscription mCompositeSubscription;
    private EvaluationAdapter mAdapter;
    private boolean mCommentInputActive;
    private boolean mStandalone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();

        /* Initialize SwipeRefresh & RecyclerView */
        mSwipeRefresh.setEnabled(true);
        initSwipeRefresh(mSwipeRefresh);

        mAdapter = new EvaluationAdapter(mContext, mSwipeRefresh, mEmptyState, mToolbar, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);

        /* Initialize Toolbar */
        MaterialMenuDrawable mMaterialNavigationDrawable = new MaterialMenuDrawable(mContext, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        mMaterialNavigationDrawable.setIconState(MaterialMenuDrawable.IconState.X);
        mToolbar.setNavigationIcon(mMaterialNavigationDrawable);
        mToolbar.setTitle(R.string.toolbar_title_evaluation);
        mToolbar.setNavigationOnClickListener(unused -> {
            if (mCommentInputActive) {
                morph2FloatingActionButton();
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
            }
            getActivity().onBackPressed();
        });
        ToolbarHelper.registerMenu(mToolbar, R.menu.evaluation, item -> {
            if (item.getItemId() != R.id.menu_evaluation_edit) return false;
            if (Evaluation.getInstance().getUserId() != null && Evaluation.getInstance().getUserId().equals(User.getInstance().getId())) {
                if (mCommentInputActive) {
                    morph2FloatingActionButton();
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
                }
                EvaluationForm.getInstance().initForEdit(Evaluation.getInstance());
                mNavigator.navigate(EvaluationStep2Fragment.class, true);
            }
            return true;
        });

        /* Initialize Others */
        mStandalone = getArguments()!=null && getArguments().getBoolean("STANDALONE", false);
        mCommentInputActive = false;
        mCommentInput.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCommentInput.setVisibility(View.GONE);
        Evaluation.getInstance().clear();
        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Bind(R.id.evaluation_container_cover) protected FrameLayout mEvaluationCover;
    public void setShowContentImmediately(boolean show) {
        showContentImmediately(mShowContentImmediately = show);
    }
    public void showContentImmediately(boolean show) {
        if(mEvaluationCover == null) return;
        mEvaluationCover.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /* TODO : Finish below here */
    private SupportAnimator mAnimatorPreL;
    private Animator mAnimatorApiL;
    private float minRadius, maxRadius;
    private int centerX, centerY;
    private boolean mShowContentImmediately = false;

    @Override
    public void onResume() {
        super.onResume();
        mCompositeSubscription.add(getSwipeRefreshObservable(mSwipeRefresh).subscribe(unused -> mAdapter.refresh()));
        mCompositeSubscription.add(getRecyclerViewScrollObservable(mRecyclerView, mToolbar, true)
            .filter(passIfNull -> passIfNull == null)
            .subscribe(unused -> mAdapter.loadMore())
        );

        showContentImmediately(mShowContentImmediately);

        mCompositeSubscription.add(ViewObservable
            .clicks(mCommentSubmit)
            .filter(unused -> !mCommentText.getText().toString().isEmpty())
            .observeOn(AndroidSchedulers.mainThread())
            .map(unused -> {
                morph2FloatingActionButton();
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
                FloatingActionControl.getButton().setIndeterminate(true);
                return null;
            })
            .observeOn(Schedulers.io())
            .flatMap(unused -> Api.papyruth().post_comment(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                mCommentText.getText().toString()
            ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(unused -> {
                FloatingActionControl.getButton().setIndeterminate(false);
                FloatingActionControl.getButton().setProgress(0, true);
                mCommentText.getText().clear();
                mAdapter.refresh();
            })
        );
        if (!mStandalone) return;
        setEvaluationFloatingActionControl();
    }

    void morph2CommentInput() {
        mCommentInput.setVisibility(View.VISIBLE);
        final FloatingActionButton fab = FloatingActionControl.getButton();
        centerX = (fab.getLeft() + fab.getRight()) / 2;
        centerY = (fab.getHeight() + MetricHelper.toPixels(mContext, 16)) / 2;
        minRadius = 0.0f;
        maxRadius = (float) Math.sqrt(Math.pow(mCommentContainer.getWidth(), 2) + Math.pow(mCommentContainer.getHeight(), 2));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(mAnimatorPreL != null) return;
            mAnimatorPreL = io.codetail.animation.ViewAnimationUtils.createCircularReveal(mCommentInput, centerX, centerY, minRadius, maxRadius);
            mAnimatorPreL.addListener(new SupportAnimator.AnimatorListener() {
                @Override public void onAnimationCancel() {}
                @Override public void onAnimationRepeat() {}
                @Override public void onAnimationStart() {}
                @Override public void onAnimationEnd() {
                    mCommentInputActive = true;
                    mCommentText.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mCommentText, InputMethodManager.SHOW_FORCED);
                }
            });
            mAnimatorPreL.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimatorPreL.setDuration(AppConst.ANIM_DURATION_MEDIUM);
            mAnimatorPreL.start();
            FloatingActionControl.getInstance().hide(true);
        } else {
            if(mAnimatorApiL != null) return;
            mAnimatorApiL = android.view.ViewAnimationUtils.createCircularReveal(mCommentInput, centerX, centerY, minRadius, maxRadius);
            mAnimatorApiL.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) {}
                @Override public void onAnimationEnd(Animator animation) {
                    mCommentInputActive = true;
                    mCommentText.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mCommentText, InputMethodManager.SHOW_FORCED);
                }
                @Override public void onAnimationCancel(Animator animation) {}
                @Override public void onAnimationRepeat(Animator animation) {}
            });
            mAnimatorApiL.setDuration(AppConst.ANIM_DURATION_MEDIUM);
            mAnimatorApiL.start();
            FloatingActionControl.getInstance().hide(true);
        }
    }
    void morph2FloatingActionButton() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (mAnimatorPreL == null) return;
            if (mAnimatorPreL.isRunning()) mAnimatorPreL.cancel();
            else {
                mAnimatorPreL = mAnimatorPreL.reverse();
                mAnimatorPreL.addListener(new SupportAnimator.AnimatorListener() {
                    @Override public void onAnimationStart() {}
                    @Override public void onAnimationCancel() {}
                    @Override public void onAnimationRepeat() {}
                    @Override public void onAnimationEnd() {
                        mAnimatorPreL = null;
                        mCommentInput.setVisibility(View.GONE);
                        mCommentInputActive = false;
                        FloatingActionControl.getInstance().show(true);
                    }
                });
                mAnimatorPreL.setInterpolator(new AccelerateDecelerateInterpolator());
                mAnimatorPreL.setDuration(AppConst.ANIM_DURATION_SHORT);
                mAnimatorPreL.start();
            }
        } else {
            if (mAnimatorApiL == null) return;
            if (mAnimatorApiL.isRunning()) mAnimatorApiL.cancel();
            else {
                mAnimatorApiL = android.view.ViewAnimationUtils.createCircularReveal(mCommentInput, centerX, centerY, maxRadius, minRadius);
                mAnimatorApiL.addListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                    @Override public void onAnimationEnd(Animator animation) {
                        mAnimatorApiL = null;
                        mCommentInput.setVisibility(View.GONE);
                        mCommentInputActive = false;
                        FloatingActionControl.getInstance().show(true);
                    }
                });
                mAnimatorApiL.setDuration(AppConst.ANIM_DURATION_SHORT);
                mAnimatorApiL.start();
            }
        }
    }

    public void showContent(boolean show) {
        ValueAnimator animAlpha = ValueAnimator.ofFloat(show ? 1.0f : 0.0f, show ? 0.0f : 1.0f);
        animAlpha.setDuration(AppConst.ANIM_DURATION_SHORT);
        animAlpha.addUpdateListener(animator -> mEvaluationCover.setAlpha((float) animator.getAnimatedValue()));
        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mEvaluationCover.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (show) mEvaluationCover.setVisibility(View.GONE);
            }
        });
        animAlpha.start();
    }

    public void setEvaluationFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_comment).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        FloatingActionControl.clicks().subscribe(unused -> morph2CommentInput(), error->ErrorHandler.handle(error, this));
    }

    @Override
    public boolean onBack() {
        if (!mCommentInputActive) return false;
        morph2FloatingActionButton();
        return true;
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof Evaluation && view.getId() == R.id.evaluation_header) {
            if (User.getInstance().needEmailConfirmed()) {
                AlertDialog.show(mContext, mNavigator, AlertDialog.Type.USER_CONFIRMATION_REQUIRED);
                return;
            }
            if (User.getInstance().needMoreEvaluation()) {
                AlertDialog.show(mContext, mNavigator, AlertDialog.Type.MANDATORY_EVALUATION_REQUIRED);
                return;
            }
            if (mCommentInputActive) {
                morph2FloatingActionButton();
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
            }
            mCompositeSubscription.add(
                Api.papyruth().get_course(User.getInstance().getAccessToken(), ((Evaluation) object).getCourseId())
                    .map(response -> response.course)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(course -> {
                        Course.getInstance().update(course);
                        mNavigator.navigate(CourseFragment.class, true);
                    }, error -> ErrorHandler.handle(error, this))
            );
        } else if(object instanceof Footer) {
            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
        }
    }
}