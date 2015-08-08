package com.montserrat.app.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.EvaluationAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.CommentInputWindow;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnBack;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.montserrat.utils.support.rx.RxValidator.nonEmpty;
import static com.montserrat.utils.support.rx.RxValidator.toString;

public class EvaluationFragment extends RecyclerViewFragment<EvaluationAdapter, CommentData> implements OnBack {

    @InjectView(R.id.evaluation_recyclerview) protected RecyclerView evaluationRecyclerView;
    @InjectView(R.id.toolbar_evaluation) protected Toolbar evaluationToolbar;
    @InjectView(R.id.comment_window) protected CommentInputWindow commentInputWindow;
    @InjectView(R.id.progress) protected View progress;
    private boolean isCommentInputWindowOpened;
    private CompositeSubscription subscriptions;
    private MaterialMenuDrawable materialNavigationDrawable;
    private Integer since, max;
    private boolean standalone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        this.standalone = false;
        if(this.getArguments()!=null && this.getArguments().getBoolean("STANDALONE")) standalone = true;

        this.setupRecyclerView(this.evaluationRecyclerView);
        EvaluationFragment.TOOLBAR_COLOR_EVALUATION = getResources().getColor(R.color.bg_normal);
        EvaluationFragment.TOOLBAR_COLOR_COMMENT = getResources().getColor(R.color.bg_accent);
        this.materialNavigationDrawable = new MaterialMenuDrawable(this.getActivity(), Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        this.materialNavigationDrawable.setIconState(MaterialMenuDrawable.IconState.X);
        this.evaluationToolbar.setNavigationIcon(materialNavigationDrawable);
        this.setEvaluationToolbar(false);

        this.commentInputWindow.setOnBackListener(this);
        this.isCommentInputWindowOpened = false;

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
        RetrofitApi.getInstance()
            .get_comments(User.getInstance().getAccessToken(), Evaluation.getInstance().getId(), null, null, null)
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                if (comments.size() > 0) {
                    this.since = comments.get(0).id;
                    this.max = comments.get(comments.size() - 1).id;
                    this.items.clear();
                    this.items.addAll(comments);
                    this.adapter.notifyDataSetChanged();
                } else this.since = -1;
                this.registerScrollToLoadMoreListener();
            });

        if(!standalone) return;
        this.setEvaluationFloatingActionControl();
        this.showContent(true);
    }

    private void registerScrollToLoadMoreListener() {
        this.subscriptions.add(this.getRecyclerViewScrollObservable(this.evaluationRecyclerView, this.evaluationToolbar, true)
            .startWith((Boolean) null)
            .filter(unused -> this.since == null || this.since >= 0)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                return RetrofitApi.getInstance().get_comments(
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
                if (comments.size() > 0) {
                    this.since = Math.min(this.since, comments.get(0).id);
                    this.max   = Math.max(this.max, comments.get(comments.size() - 1).id);
                    this.items.addAll(comments);
                    this.adapter.notifyItemRangeInserted(this.adapter.getItemCount(), comments.size());
                } else this.since = -1;
            })
        );
    }

    @Override
    protected EvaluationAdapter getAdapter() {
        return new EvaluationAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {}

    public void setEvaluationFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_comment).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().subscribe(unused -> showCommentInputWindow());
    }

    @Override
    public boolean onBack() {
        if (!isCommentInputWindowOpened) return false;
        this.hideCommentInputWindow();
        return true;
    }

    private void showCommentInputWindow() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_done, false);
        this.commentInputWindow.setVisibility(View.VISIBLE);
        this.commentInputWindow.getCommentInputEditText().requestFocus();
        final InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.commentInputWindow.getCommentInputEditText(), InputMethodManager.SHOW_FORCED);

        this.subscriptions.add(WidgetObservable
            .text(this.commentInputWindow.getCommentInputEditText())
            .map(toString)
            .map(nonEmpty)
            .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(valid -> {
                final boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            })
        );

        this.subscriptions.add(FloatingActionControl
            .clicks()
            .filter(unused -> !this.commentInputWindow.getCommentInputEditText().getText().toString().isEmpty())
            .observeOn(Schedulers.io())
            .flatMap(unused -> RetrofitApi.getInstance().post_comment(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                this.commentInputWindow.getCommentInputEditText().getText().toString()
            ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(commentResponse -> {
                this.commentInputWindow.getCommentInputEditText().setText("");
                hideCommentInputWindow();
            })
        );
        this.isCommentInputWindowOpened = true;
        this.setCommentToolbar(true);
    }

    private void hideCommentInputWindow() {
        final InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.commentInputWindow.getWindowToken(), 0);
        this.commentInputWindow.setVisibility(View.GONE);
        this.setEvaluationFloatingActionControl();
        this.isCommentInputWindowOpened = false;
        this.setEvaluationToolbar(true);
    }

    private static int TOOLBAR_COLOR_EVALUATION;
    private static int TOOLBAR_COLOR_COMMENT;
    private void setEvaluationToolbar(boolean animate) {
        this.materialNavigationDrawable.animateIconState(MaterialMenuDrawable.IconState.X);
        this.evaluationToolbar.setNavigationOnClickListener(unused -> this.getActivity().onBackPressed());
        this.evaluationToolbar.setTitle(R.string.toolbar_title_evaluation);
        this.evaluationToolbar.setTitleTextColor(Color.WHITE);
        this.evaluationToolbar.inflateMenu(R.menu.evaluation);
        if(animate) ToolbarUtil.getColorTransitionAnimator(this.evaluationToolbar, TOOLBAR_COLOR_COMMENT, TOOLBAR_COLOR_EVALUATION).setDuration(AppConst.ANIM_DURATION_SHORT).start();
        else this.evaluationToolbar.setY(0);
    }
    private void setCommentToolbar(boolean animate) {
        this.materialNavigationDrawable.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        this.evaluationToolbar.setNavigationOnClickListener(unused -> this.onBack());
        this.evaluationToolbar.setTitle(R.string.toolbar_title_new_comment);
        this.evaluationToolbar.setTitleTextColor(Color.WHITE);
        this.evaluationToolbar.getMenu().clear();
        if (animate) ToolbarUtil.getColorTransitionAnimator(this.evaluationToolbar, TOOLBAR_COLOR_EVALUATION, TOOLBAR_COLOR_COMMENT).setDuration(AppConst.ANIM_DURATION_SHORT).start();
        else this.evaluationToolbar.setY(0);
    }

    @InjectView(R.id.evaluation_container_cover) protected FrameLayout cover;
    void showContent(boolean show) {
        ValueAnimator animAlpha = ValueAnimator.ofFloat(show ? 1.0f : 0.0f, show ? 0.0f : 1.0f);
        animAlpha.addUpdateListener(animator -> {
            if(this.cover != null) this.cover.setAlpha((float) animator.getAnimatedValue());
        });
        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                EvaluationFragment.this.cover.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(show) EvaluationFragment.this.cover.setVisibility(View.GONE);
            }
        });
        animAlpha.start();
    }
}