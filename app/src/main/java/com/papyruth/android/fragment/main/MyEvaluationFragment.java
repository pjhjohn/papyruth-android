package com.papyruth.android.fragment.main;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.MyEvaluationItemsAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.FragmentNavigator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MyEvaluationFragment extends CommonRecyclerViewFragment<MyEvaluationItemsAdapter, EvaluationData> {

    private Tracker mTracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
    }
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
            }, error-> ErrorHandler.handle(error, this));
    }


    private boolean askmore = true;
    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_my_evaluation));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toolbar.setTitle(R.string.nav_item_my_evaluation);
        ToolbarHelper.getColorTransitionAnimator(toolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
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
                }, error -> ErrorHandler.handle(error, this))
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
                    ErrorHandler.handle(error, this);
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
                    ErrorHandler.handle(error, this);
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
    protected MyEvaluationItemsAdapter getAdapter () {
        return new MyEvaluationItemsAdapter(this.items, this);
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_blue).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().subscribe(
            unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN),
            error -> ErrorHandler.handle(error, this)
        );
    }
}