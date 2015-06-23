package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.adapter.EvaluationAdapter;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;

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
public class EvaluationFragment extends RecyclerViewFragment<EvaluationAdapter, CommentData> {
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
    @InjectView(R.id.new_comment) protected TextView commentInputWindow;
    private CompositeSubscription subscriptions;
    private Integer page = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(evaluationRecyclerView);
        this.evaluationToolbar.setNavigationIcon(R.drawable.ic_light_clear);
        this.evaluationToolbar.setTitle(Course.getInstance().getName());
        this.evaluationToolbar.inflateMenu(R.menu.evaluation);
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
        this.subscriptions.add(RetrofitApi
            .getInstance()
            .comments(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                0,
                null
            )
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                this.page = 0;
                this.items.clear();
                this.items.addAll(comments);
                this.adapter.notifyDataSetChanged();
            })
        );

        this.subscriptions.add(FloatingActionControl
            .clicks()
            .subscribe(unused -> showCommentInputWindow())
        );
    }

    private void showCommentInputWindow() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_done);
        this.commentInputWindow.setVisibility(View.VISIBLE);
        this.commentInputWindow.requestFocus();
        final InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.commentInputWindow, InputMethodManager.SHOW_FORCED);

        this.subscriptions.add(WidgetObservable
            .text(this.commentInputWindow)
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
            .filter(unused -> !this.commentInputWindow.getText().toString().isEmpty())
            .observeOn(Schedulers.io())
            .flatMap(unused -> RetrofitApi
                .getInstance()
                .comments(
                    User.getInstance().getAccessToken(),
                    Evaluation.getInstance().getId(),
                    this.commentInputWindow.getText().toString()
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(commentResponse -> {
                this.commentInputWindow.setVisibility(View.GONE);
                setEvaluationFloatingActionControl();
            })
        );
    }
}