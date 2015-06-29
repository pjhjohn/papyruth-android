package com.montserrat.app.fragment.main;

import android.app.Activity;
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

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.recyclerview.adapter.EvaluationAdapter;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.CommentInputWindow;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnBack;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.montserrat.utils.support.rx.RxValidator.toString;
import static com.montserrat.utils.support.rx.RxValidator.nonEmpty;

/**
 * Evaluation Fragment
 * - Evaluation contents as a HEADER of recycler view
 * - List of items containing each Comment
 * - Has ability to receive comment input
 */
public class EvaluationFragment extends RecyclerViewFragment<EvaluationAdapter, CommentData> implements OnBack {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
    }

    @InjectView(R.id.evaluation_recyclerview) protected RecyclerView evaluationRecyclerView;
    @InjectView(R.id.toolbar_evaluation) protected Toolbar evaluationToolbar;
    @InjectView(R.id.comment_window) protected CommentInputWindow commentInputWindow;
    @InjectView(R.id.progress) protected View progress;
    private boolean isCommentInputWindowOpened;
    private CompositeSubscription subscriptions;
    private Integer page;
    private boolean moreCommentsAvailiable;
    private MaterialMenuDrawable materialNavigationDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        EvaluationFragment.TOOLBAR_COLOR_EVALUATION = getResources().getColor(R.color.bg_normal);
        EvaluationFragment.TOOLBAR_COLOR_COMMENT = getResources().getColor(R.color.bg_accent);

        this.setupRecyclerView(evaluationRecyclerView);
        this.materialNavigationDrawable = new MaterialMenuDrawable(this.getActivity(), Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        this.evaluationToolbar.setNavigationIcon(materialNavigationDrawable);
        this.setEvaluationToolbar(false);
        this.commentInputWindow.setOnBackListener(this);
        this.isCommentInputWindowOpened = false;
        this.page = null;
        this.moreCommentsAvailiable = true;
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
    protected EvaluationAdapter getAdapter() {
        return new EvaluationAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        // Comment has been clicked
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setEvaluationFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_comment).show(true, 200, TimeUnit.MILLISECONDS);
        this.subscriptions.add(FloatingActionControl
            .clicks()
            .subscribe(unused -> showCommentInputWindow())
        );

        this.subscriptions.add(this.getRecyclerViewScrollObservable(this.evaluationRecyclerView, this.evaluationToolbar, true)
            .startWith((Boolean) null)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .observeOn(AndroidSchedulers.mainThread())
            .filter(unused -> this.moreCommentsAvailiable)
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                return RetrofitApi
                    .getInstance()
                    .comments(
                        User.getInstance().getAccessToken(),
                        Evaluation.getInstance().getId(),
                        this.page == null ? 0 : this.page + 1,
                        null
                    );
            })
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                this.progress.setVisibility(View.GONE);
                if (comments.size() == 0) {
                    this.moreCommentsAvailiable = false;
                    return;
                }
                this.page = this.page == null ? 0 : this.page + 1;
                this.items.addAll(comments);
                this.adapter.notifyItemRangeInserted(this.adapter.getItemCount(), comments.size());
            })
        );
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
            .flatMap(unused -> RetrofitApi
                .getInstance()
                .comments(
                    User.getInstance().getAccessToken(),
                    Evaluation.getInstance().getId(),
                    this.commentInputWindow.getCommentInputEditText().getText().toString()
                )
            )
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
        if(animate) ToolbarUtil.getColorTransitionAnimator(this.evaluationToolbar, TOOLBAR_COLOR_EVALUATION, TOOLBAR_COLOR_COMMENT).setDuration(AppConst.ANIM_DURATION_SHORT).start();
        else this.evaluationToolbar.setY(0);
    }
}