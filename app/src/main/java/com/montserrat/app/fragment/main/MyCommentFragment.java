package com.montserrat.app.fragment.main;

import android.graphics.Color;
import android.view.View;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyCommentAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.CommonRecyclerViewFragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MyCommentFragment extends CommonRecyclerViewFragment<MyCommentAdapter, CommentData> {

    @Override
    public void onRecyclerViewItemClick(View view, int position) {

        if(slaveIsOccupying) return;
        if(animators != null && animators.isRunning()) return;
        RetrofitApi.getInstance()
            .get_evaluation(User.getInstance().getAccessToken(), this.items.get(position).evaluation_id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                Evaluation.getInstance().update(response.evaluation);
                this.slave = new EvaluationFragment();
                setCommentPosition(this.items.get(position).id);
                this.openEvaluation(view);
            });
    }

    private int page = 1;
    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(R.string.my_comment);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();

        this.subscriptions.add(
            super.getRefreshObservable(this.swipeRefresh)
                .flatMap(unused -> {
                    this.swipeRefresh.setRefreshing(true);
                    return RetrofitApi.getInstance().users_me_comments(User.getInstance().getAccessToken(), page = 1);
                })
                .filter(response -> response.success)
                .map(mywritten -> mywritten.comments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    this.swipeRefresh.setRefreshing(false);
                    this.items.clear();
                    if (comments != null) {
                        this.items.addAll(comments);
                    }
                    this.adapter.notifyDataSetChanged();
                }, error -> {
                    this.swipeRefresh.setRefreshing(false);
                    error.printStackTrace();
                }, () ->{
                    this.swipeRefresh.setRefreshing(false);
                    this.progress.setVisibility(View.GONE);
                })
        );

        this.subscriptions.add(
            super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, false)
                .startWith((Boolean) null)
                .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    this.swipeRefresh.setRefreshing(false);
                    // TODO : handle the case for max_id == 0 : prefer not to request to server
                    Timber.d("calling");
                    return RetrofitApi.getInstance().users_me_comments(User.getInstance().getAccessToken(), page++);
                })
                .map(mywritten -> mywritten.comments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    this.progress.setVisibility(View.GONE);
                    if (comments != null) this.items.addAll(comments);
                    this.adapter.notifyDataSetChanged();
                }, error -> {
                    error.printStackTrace();
                }, () ->{
                    this.swipeRefresh.setRefreshing(false);
                    this.progress.setVisibility(View.GONE);
                })
        );
    }


    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().clear();
    }

    @Override
    protected MyCommentAdapter getAdapter () {
        return new MyCommentAdapter(this.items, this);
    }
}