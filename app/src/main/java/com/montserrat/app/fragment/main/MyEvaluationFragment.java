package com.montserrat.app.fragment.main;

import android.view.View;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyEvaluationAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.CommonRecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
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
                this.openEvaluation(view, true);
            });
    }


    private boolean askmore = true;
    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(R.string.nav_item_my_evaluation);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.colorchip_blue).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);

        this.progress.setVisibility(View.VISIBLE);
        this.subscriptions.add(
            Api.papyruth().users_me_evaluations(User.getInstance().getAccessToken(), page = 1)
                .map(response -> response.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations -> {
                    this.notifyDataChanged(evaluations);
                }, error -> error.printStackTrace())
        );
        this.subscriptions.add(
            super.getRefreshObservable(this.swipeRefresh)
                .flatMap(unused -> {
                    this.swipeRefresh.setRefreshing(true);
                    return Api.papyruth().users_me_evaluations(User.getInstance().getAccessToken(), page = 1);
                })
                .map(evaluations -> evaluations.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations -> {
                    this.swipeRefresh.setRefreshing(false);
                    this.notifyDataChanged(evaluations);
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
                .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE && !items.isEmpty() && askmore)
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    this.swipeRefresh.setRefreshing(false);
                    // TODO : handle the case for max_id == 0 : prefer not to request to server
                    return Api.papyruth().users_me_evaluations(User.getInstance().getAccessToken(), page);
                })
                .map(response -> response.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations -> {
                    this.progress.setVisibility(View.GONE);
                    this.notifyDataChanged(evaluations);
                }, error -> {
                    this.progress.setVisibility(View.GONE);
                    error.printStackTrace();
                }, () -> {
                    this.swipeRefresh.setRefreshing(false);
                    this.progress.setVisibility(View.GONE);
                })
        );
    }

    public void notifyDataChanged(List<EvaluationData> evaluations){
        if(page < 2) {
            this.items.clear();
        }

        this.progress.setVisibility(View.GONE);
        askmore = !evaluations.isEmpty();
        this.adapter.setShowPlaceholder(evaluations.isEmpty());
        this.items.addAll(evaluations);
        this.adapter.notifyDataSetChanged();
        page++;
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