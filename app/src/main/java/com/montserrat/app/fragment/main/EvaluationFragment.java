package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.EvaluationAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.materialdialog.AlertMandatoryDialog;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.MetricUtil;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnBack;

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

    private Navigator navigator;

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
        this.evaluationToolbar.setNavigationOnClickListener(unused -> this.getActivity().onBackPressed());
        this.evaluationToolbar.setTitle(R.string.toolbar_title_evaluation);
        ToolbarUtil.registerMenu(this.evaluationToolbar, R.menu.evaluation, null);

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
        this.subscriptions.add(Api.papyruth()
            .get_comments(User.getInstance().getAccessToken(), Evaluation.getInstance().getId(), null, null, null)
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                setComments(comments, true);
                registerScrollToLoadMoreListener();
            })
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
            })
        );
        if (!standalone) return;
        this.setEvaluationFloatingActionControl();
        this.showContent(true);
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
        this.adapter.setIsEmptyData(comments.isEmpty());
        if (reset) this.adapter.notifyDataSetChanged();
        else this.adapter.notifyItemRangeInserted(this.adapter.getItemCount(), comments.size());
    }

    private void registerScrollToLoadMoreListener() {
        this.subscriptions.add(this.getRecyclerViewScrollObservable(this.evaluationRecyclerView, this.evaluationToolbar, true)
            .startWith((Boolean) null)
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
            })
        );
    }

    void morph2CommentInput() {
        mRevealTarget.setVisibility(View.VISIBLE);
        final FloatingActionButton fab = FloatingActionControl.getButton();
        centerX = (fab.getLeft() + fab.getRight()) / 2;
        centerY = (fab.getHeight() + MetricUtil.toPixels(getActivity(), 16)) / 2;
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

    @InjectView(R.id.evaluation_container_cover) protected FrameLayout cover;
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
                if (show) EvaluationFragment.this.cover.setVisibility(View.GONE);
            }
        });
        animAlpha.start();
    }
    public void focusComment(int commentId){
        final int offset = this.adapter.getItemOffset()+3;
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).id == commentId){
                this.evaluationRecyclerView.scrollToPosition((i+offset >= items.size()) ? items.size()+1 : i+offset);
                break;
            }
        }
    }
//    public void focusCommentByPosition(int position){
//        final int offset = this.adapter.getItemOffset()+2;
//        this.evaluationRecyclerView.scrollToPosition(position+offset);
//    }
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
        FloatingActionControl.getInstance().setControl(R.layout.fab_comment).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        FloatingActionControl.clicks().subscribe(unused -> morph2CommentInput());
    }
    @Override
    public boolean onBack() {
        if (!mCommentInputActive) return false;
        this.morph2FAB();
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.evaluation_modify && Evaluation.getInstance().getUserId().equals(User.getInstance().getId())){
            EvaluationForm.getInstance().initForEdit(Evaluation.getInstance());
            ((MainActivity) this.getActivity()).navigate(EvaluationStep2Fragment.class, true);
        }else if(v.getId() == R.id.evaluation_header){
            if(User.getInstance().needMoreEvaluation())
                AlertMandatoryDialog.show(getActivity(), navigator);
            this.subscriptions.add(
                Api.papyruth().get_course(User.getInstance().getAccessToken(), Evaluation.getInstance().getCourseId())
                    .map(response -> response.course)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(course -> {
                        Course.getInstance().update(course);
                        ((MainActivity)this.getActivity()).navigate(CourseFragment.class, true);
                    }, error -> error.printStackTrace())
            );
        }
    }
}