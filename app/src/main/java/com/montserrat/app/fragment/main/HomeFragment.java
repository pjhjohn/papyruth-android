package com.montserrat.app.fragment.main;

import android.view.View;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.CommonRecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeFragment extends CommonRecyclerViewFragment<EvaluationItemsDetailAdapter, EvaluationData> {

    private Integer sinceId = null, maxId = null;


    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(slaveIsOccupying) return;
        if(animators != null && animators.isRunning()) return;
        RetrofitApi.getInstance()
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
        this.subscriptions.add(super.getRefreshObservable(this.swipeRefresh)
            .flatMap(unused -> {
                this.swipeRefresh.setRefreshing(true);
                return RetrofitApi.getInstance().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null);
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
            })
        );

        this.subscriptions.add(super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, true)
            .startWith((Boolean) null)
            .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(unused -> {
                this.progress.setVisibility(View.VISIBLE);
                // TODO : handle the case for max_id == 0 : prefer not to request to server
                return RetrofitApi.getInstance().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, sinceId == null ? null : sinceId - 1, null, null);
            })
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                this.progress.setVisibility(View.GONE);
                if (evaluations != null) this.items.addAll(evaluations);
                this.adapter.notifyDataSetChanged();
                // TODO : Implement Better Algorithm
                for (EvaluationData evaluation : evaluations) {
                    final int id = evaluation.id;
                    if (maxId == null) maxId = id;
                    else if (maxId < id) maxId = id;
                    if (sinceId == null) sinceId = id;
                    else if (sinceId > id) sinceId = id;
                }
            })
        );
    }


    @Override
    protected EvaluationItemsDetailAdapter getAdapter () {
        return new EvaluationItemsDetailAdapter(this.items, this);
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN));
    }


}