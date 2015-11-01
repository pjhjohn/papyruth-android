package com.montserrat.app.fragment.main;

import android.view.View;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyEvaluationAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.CommonRecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MyEvaluationFragment extends CommonRecyclerViewFragment<MyEvaluationAdapter, EvaluationData> {


    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(isOpenSlave) return;
        if(slaveIsOccupying) return;
        if(animators != null && animators.isRunning()) return;
        isOpenSlave = true;
        Api.papyruth()
            .get_evaluation(User.getInstance().getAccessToken(), this.items.get(position).id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                Evaluation.getInstance().update(response.evaluation);
                this.slave = new EvaluationFragment();
                this.openEvaluation(view);
            });
    }

    @Override
    public void onResume() {
        super.onResume();

        toolbar.setTitle(R.string.nav_item_my_evaluation);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();

        this.subscriptions.add(super.getRefreshObservable(this.swipeRefresh)
            .flatMap(unused -> {
                this.swipeRefresh.setRefreshing(true);
                return Api.papyruth().users_me_evaluations(User.getInstance().getAccessToken(), page = 1);
            })
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.swipeRefresh.setRefreshing(false);
                this.items.clear();
                if (evaluations != null)
                    this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
            },error -> {
                this.swipeRefresh.setRefreshing(false);
                error.printStackTrace();
            }, () ->{
                this.swipeRefresh.setRefreshing(false);
                this.progress.setVisibility(View.GONE);
            })
        );

        this.subscriptions.add(super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, true)
            .startWith((Boolean) null)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                this.swipeRefresh.setRefreshing(false);
                // TODO : handle the case for max_id == 0 : prefer not to request to server
                return Api.papyruth().users_me_evaluations(User.getInstance().getAccessToken(), page++);
            })
            .map(mywritten -> mywritten.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.progress.setVisibility(View.GONE);
                if (evaluations != null) this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
            },error -> {
                this.progress.setVisibility(View.GONE);
                error.printStackTrace();
            }, () ->{
                this.swipeRefresh.setRefreshing(false);
                this.progress.setVisibility(View.GONE);
            })
        );
    }

    @Override
    protected MyEvaluationAdapter getAdapter () {
        return new MyEvaluationAdapter(this.items, this);
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN));
    }
}