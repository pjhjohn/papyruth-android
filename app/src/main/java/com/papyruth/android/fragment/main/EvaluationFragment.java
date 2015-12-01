package com.papyruth.android.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.CommentData;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.EvaluationAdapter;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.helper.MetricHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.fragment.RecyclerViewFragment;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.viewpager.OnBack;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.codetail.animation.SupportAnimator;
import io.codetail.widget.RevealFrameLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EvaluationFragment extends RecyclerViewFragment<EvaluationAdapter, CommentData> implements OnBack, View.OnClickListener {
    @InjectView(R.id.evaluation_recyclerview) protected RecyclerView evaluationRecyclerView;
    @InjectView(R.id.toolbar_evaluation) protected Toolbar evaluationToolbar;
    @InjectView(R.id.progress) protected View progress;
    private boolean mCommentInputActive;
    private CompositeSubscription subscriptions;
    private MaterialMenuDrawable materialNavigationDrawable;
    private Integer since, max;
    private boolean standalone;

    private SupportAnimator mAnimatorPreL;
    private Animator mAnimatorApiL;
    private RevealFrameLayout mRevealContainer;
    private RelativeLayout mRevealTarget;
    private EditText mCommentText;
    private ImageButton mCommentSubmit;

    private float minRadius, maxRadius;
    private int centerX, centerY;
    private boolean showContentImmediately = false;

    private Integer commentId;

    private Navigator navigator;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mRevealContainer = (RevealFrameLayout) activity.findViewById(R.id.comment_reveal_frame);
        mRevealTarget = (RelativeLayout) activity.findViewById(R.id.comment_input);
        mCommentText = (EditText) activity.findViewById(R.id.comment_text);
        mCommentSubmit = (ImageButton) activity.findViewById(R.id.comment_submit);
        mRevealTarget.setVisibility(View.GONE);
        navigator = (Navigator) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRevealTarget.setVisibility(View.GONE);
        this.navigator = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        this.standalone = this.getArguments()!=null && this.getArguments().getBoolean("STANDALONE", false);

        this.setupRecyclerView(this.evaluationRecyclerView);
        this.materialNavigationDrawable = new MaterialMenuDrawable(this.getActivity(), Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        this.materialNavigationDrawable.setIconState(MaterialMenuDrawable.IconState.X);
        this.evaluationToolbar.setNavigationIcon(materialNavigationDrawable);
        this.evaluationToolbar.setNavigationOnClickListener(unused -> {
            if(mCommentInputActive) {
                this.morph2FAB();
                ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
                closeCommentWindow = true;
            }
            this.getActivity().onBackPressed();
        });
        this.evaluationToolbar.setTitle(R.string.toolbar_title_evaluation);
        ToolbarHelper.registerMenu(this.evaluationToolbar, R.menu.evaluation, item -> {
            if (item.getItemId() != R.id.menu_evaluation_edit) return false;
            if (Evaluation.getInstance().getUserId().equals(User.getInstance().getId())) {
                if (mCommentInputActive) {
                    this.morph2FAB();
                    ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
                }
                EvaluationForm.getInstance().initForEdit(Evaluation.getInstance());
                ((MainActivity) this.getActivity()).navigate(EvaluationStep2Fragment.class, true);
            }
            return true;
        });
        this.mCommentInputActive = false;
        this.since = null;
        this.max = null;
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        ButterKnife.reset(this);
        Evaluation.getInstance().clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_evaluation));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        this.evaluationToolbar.getMenu().findItem(R.id.menu_evaluation_edit).setVisible(Evaluation.getInstance().getUserId() != null && Evaluation.getInstance().getUserId().equals(User.getInstance().getId()));
        this.showContentImmediately(this.showContentImmediately);
        this.subscriptions.add(Api.papyruth()
            .get_comments(User.getInstance().getAccessToken(), Evaluation.getInstance().getId(), null, null, null)
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                setComments(comments, true);
                registerScrollToLoadMoreListener();
            }, error -> ErrorHandler.handle(error, this))
        );
        this.subscriptions.add(ViewObservable
            .clicks(this.mCommentSubmit)
            .filter(unused -> !this.mCommentText.getText().toString().isEmpty())
            .observeOn(AndroidSchedulers.mainThread())
            .map(unused -> {
                this.morph2FAB();
                ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
                FloatingActionControl.getButton().setIndeterminate(true);
                return null;
            })
            .observeOn(Schedulers.io())
            .flatMap(unused -> Api.papyruth().post_comment(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                this.mCommentText.getText().toString()
            ))
            .flatMap(unused -> Api.papyruth().get_comments(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                this.since = null,
                this.max = null,
                null
            ))
            .map(response -> response.comments)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                setComments(comments, true);
                this.mCommentText.setText("");
                FloatingActionControl.getButton().setIndeterminate(false);
                FloatingActionControl.getButton().setProgress(0, true);
            }, error->ErrorHandler.handle(error, this))
        );
        if (!standalone) return;
        this.setEvaluationFloatingActionControl();
    }

    private void setComments(List<CommentData> comments) { setComments(comments, false); }
    private void setComments(List<CommentData> comments, boolean reset) {
        if (reset) this.items.clear();
        if (comments == null || comments.size() <= 0) {
            this.since = -1;
            return;
        }
        this.since = this.since == null ? comments.get(comments.size() - 1).id : Math.min(this.since, comments.get(comments.size() - 1).id);
        this.max = this.max == null ? comments.get(0).id : Math.max(this.max, comments.get(0).id);
        this.items.addAll(comments);
        this.adapter.setShowPlaceholder(comments.isEmpty());
        if (reset) this.adapter.notifyDataSetChanged();
        else this.adapter.notifyItemRangeInserted(this.adapter.getItemCount(), comments.size());

        if(commentId != null) focusComment();
    }

    private void registerScrollToLoadMoreListener() {
        this.subscriptions.add(this.getRecyclerViewScrollObservable(this.evaluationRecyclerView, this.evaluationToolbar, true)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .filter(unused -> this.since != null && this.since >= 0)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                return Api.papyruth().get_comments(
                    User.getInstance().getAccessToken(),
                    Evaluation.getInstance().getId(),
                    null,
                    this.since - 1,
                    null
                );
            })
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                this.progress.setVisibility(View.GONE);
                this.setComments(comments);
            }, error->ErrorHandler.handle(error, this))
        );
    }

    void morph2CommentInput() {
        mRevealTarget.setVisibility(View.VISIBLE);
        final FloatingActionButton fab = FloatingActionControl.getButton();
        centerX = (fab.getLeft() + fab.getRight()) / 2;
        centerY = (fab.getHeight() + MetricHelper.toPixels(getActivity(), 16)) / 2;
        minRadius = 0.0f;
        maxRadius = (float) Math.sqrt(Math.pow(mRevealContainer.getWidth(), 2) + Math.pow(mRevealContainer.getHeight(), 2));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(mAnimatorPreL != null) return;
            mAnimatorPreL = io.codetail.animation.ViewAnimationUtils.createCircularReveal(mRevealTarget, centerX, centerY, minRadius, maxRadius);
            mAnimatorPreL.addListener(new SupportAnimator.AnimatorListener() {
                @Override public void onAnimationCancel() {}
                @Override public void onAnimationRepeat() {}
                @Override public void onAnimationStart() {}
                @Override public void onAnimationEnd() {
                    mCommentInputActive = true;
                    mCommentText.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mCommentText, InputMethodManager.SHOW_FORCED);
                }
            });
            mAnimatorPreL.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimatorPreL.setDuration(AppConst.ANIM_DURATION_MEDIUM);
            mAnimatorPreL.start();
            FloatingActionControl.getInstance().hide(true);
        } else {
            if(mAnimatorApiL != null) return;
            mAnimatorApiL = android.view.ViewAnimationUtils.createCircularReveal(mRevealTarget, centerX, centerY, minRadius, maxRadius);
            mAnimatorApiL.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) {}
                @Override public void onAnimationEnd(Animator animation) {
                    mCommentInputActive = true;
                    mCommentText.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
    void morph2FAB() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (mAnimatorPreL == null) return;
            if (mAnimatorPreL.isRunning()) mAnimatorPreL.cancel();
            else {
                mAnimatorPreL = mAnimatorPreL.reverse();
                mAnimatorPreL.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {
                    }

                    @Override
                    public void onAnimationEnd() {
                        mAnimatorPreL = null;
                        mRevealTarget.setVisibility(View.GONE);
                        mCommentInputActive = false;
                        FloatingActionControl.getInstance().show(true);
                    }

                    @Override
                    public void onAnimationCancel() {
                    }

                    @Override
                    public void onAnimationRepeat() {
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
                mAnimatorApiL = android.view.ViewAnimationUtils.createCircularReveal(mRevealTarget, centerX, centerY, maxRadius, minRadius);
                mAnimatorApiL.addListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimatorApiL = null;
                        mRevealTarget.setVisibility(View.GONE);
                        mCommentInputActive = false;
                        FloatingActionControl.getInstance().show(true);
                    }
                });
                mAnimatorApiL.setDuration(AppConst.ANIM_DURATION_SHORT);
                mAnimatorApiL.start();
            }
        }
    }

    public void setShowContentImmediately(boolean show){
        this.showContentImmediately = show;
    }

    @InjectView(R.id.evaluation_container_cover) protected FrameLayout cover;
    public void showContentImmediately(boolean show){
        EvaluationFragment.this.cover.setVisibility( show ? View.GONE : View.VISIBLE);
    }
    public void showContent(boolean show) {
        ValueAnimator animAlpha = ValueAnimator.ofFloat(show ? 1.0f : 0.0f, show ? 0.0f : 1.0f);
        animAlpha.addUpdateListener(animator -> {
            if (this.cover != null) this.cover.setAlpha((float) animator.getAnimatedValue());
        });
        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                EvaluationFragment.this.cover.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (show) {
                    EvaluationFragment.this.cover.setVisibility(View.GONE);
                }
            }
        });
        animAlpha.start();
    }
    public void setCommentId(int commentId){
        this.commentId = commentId;
    }

    public void focusComment(){
        final int offset = this.adapter.getItemOffset();
        RecyclerView.State state = new RecyclerView.State();

        Integer index = null;
        for(int i = 0; i < items.size(); i++) {
            if (this.commentId.equals(items.get(i).id)) {
                index = i + offset;
                break;
            }
        }
        if(index == null)
            return;
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this.getActivity()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return new PointF(0, 0);
            }
        };
        smoothScroller.setTargetPosition(index);
        evaluationRecyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
        this.commentId = null;
    }

    @Override
    protected EvaluationAdapter getAdapter() {
        return new EvaluationAdapter(this.items, this, this);
    }
    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
    @Override
    public void onRecyclerViewItemClick(View view, int position) {

    }
    public void setEvaluationFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_comment).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        FloatingActionControl.clicks().subscribe(unused -> morph2CommentInput(), error->ErrorHandler.handle(error, this));
    }


    private boolean closeCommentWindow = false;
    @Override
    public boolean onBack() {
        if (!mCommentInputActive) {
            return false;
        }
        if(closeCommentWindow){
            return false;
        }else {
            closeCommentWindow = true;
            this.morph2FAB();
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.evaluation_lecture) {
            if(User.getInstance().needEmailConfirmed()){
                AlertDialog.show(getActivity(), navigator, AlertDialog.Type.NEED_CONFIRMATION);
                return;
            }
            if(User.getInstance().needMoreEvaluation()) {
                AlertDialog.show(getActivity(), navigator, AlertDialog.Type.EVALUATION_MANDATORY);
                return;
            }
            if(mCommentInputActive) {
                this.morph2FAB();
                ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mCommentText.getWindowToken(), 0);
            }
            this.subscriptions.add(
                Api.papyruth().get_course(User.getInstance().getAccessToken(), Evaluation.getInstance().getCourseId())
                    .map(response -> response.course)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(course -> {
                        Course.getInstance().update(course);
                        ((MainActivity)this.getActivity()).navigate(CourseFragment.class, true);
                    }, error -> ErrorHandler.handle(error, this))
            );
        }
    }
}