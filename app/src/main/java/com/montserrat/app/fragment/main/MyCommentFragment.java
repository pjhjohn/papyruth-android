package com.montserrat.app.fragment.main;

import android.view.View;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.MyCommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyCommentAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.CommonRecyclerViewFragment;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MyCommentFragment extends CommonRecyclerViewFragment<MyCommentAdapter, MyCommentData> {

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(isOpenSlave) return;
        if(slaveIsOccupying) return;
        if(animators != null && animators.isRunning()) return;
        isOpenSlave = true;
        Api.papyruth()
            .get_evaluation(User.getInstance().getAccessToken(), this.items.get(position).evaluation_id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                Evaluation.getInstance().update(response.evaluation);
                this.slave = new EvaluationFragment();
                setCommentPosition(this.items.get(position).id);
                this.openEvaluation(view);
            });
    }

    private boolean askmore = true;
    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(R.string.nav_item_my_comment);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();

        this.subscriptions.add(
            super.getRefreshObservable(this.swipeRefresh)
                .flatMap(unused -> {
                    this.swipeRefresh.setRefreshing(true);
                    return Api.papyruth().users_me_comments(User.getInstance().getAccessToken(), page = 1);
                })
                .map(mywritten -> mywritten.comments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    this.swipeRefresh.setRefreshing(false);
                    notifyDataChanged(comments);
                }, error -> {
                    this.swipeRefresh.setRefreshing(false);
                    error.printStackTrace();
                }, () -> {
                    this.swipeRefresh.setRefreshing(false);
                    this.progress.setVisibility(View.GONE);
                })
        );

        this.subscriptions.add(
            super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, false)
                .filter(passIfNull -> {
                    Timber.d("has value : %s %s ", passIfNull, progress.getVisibility());
                    return passIfNull == null && this.progress.getVisibility() != View.VISIBLE && askmore;
                })
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    this.swipeRefresh.setRefreshing(false);
                    // TODO : handle the case for max_id == 0 : prefer not to request to server
                    Timber.d("calling");
                    return Api.papyruth().users_me_comments(User.getInstance().getAccessToken(), page);
                })
                .map(mywritten -> mywritten.comments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    this.progress.setVisibility(View.GONE);
                    notifyDataChanged(comments);
                }, error -> {
                    error.printStackTrace();
                }, () ->{
                    this.swipeRefresh.setRefreshing(false);
                    this.progress.setVisibility(View.GONE);
                })
        );
    }

    public void notifyDataChanged(List<MyCommentData> comments){
        if(page < 2) {
            this.items.clear();
        }
        askmore = !comments.isEmpty();
        adapter.setIsEmptyData(comments.isEmpty());
        if (comments.isEmpty())
            this.adapter.notifyDataSetChanged();
        this.items.addAll(comments);
        this.doOnGetMyWritten(items.size() - comments.size(), items.size() - 1);
        page++;
    }

    public void doOnGetMyWritten(int start, int end){
        if (start > -1)
            for(int i = start; i <= end; i++){
                MyCommentData comment = items.get(i);
                Api.papyruth().get_evaluation(User.getInstance().getAccessToken(), comment.evaluation_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        response->{
                            comment.lecture_name = response.evaluation.lecture_name;
                            comment.professor_name = response.evaluation.professor_name;
                            comment.category = getResources().getString(R.string.category_major);

                            if (checkAllDataLoad(start, end))
                                this.adapter.notifyDataSetChanged();
                        },
                        error -> {
                            error.printStackTrace();
                        }
                    );
            }
    }
    private boolean checkAllDataLoad(int start, int end){
        for(int i = start; i <= end; i++){
            if(items.get(i).lecture_name == null)
                return false;
        }
        return true;
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