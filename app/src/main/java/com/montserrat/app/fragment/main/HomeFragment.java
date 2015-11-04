package com.montserrat.app.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.materialdialog.AlertMandatoryDialog;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.CommonRecyclerViewFragment;
import com.montserrat.utils.view.navigator.FragmentNavigator;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class HomeFragment extends CommonRecyclerViewFragment<EvaluationItemsDetailAdapter, EvaluationData> {

    private Integer sinceId = null, maxId = null, firstId = null, size = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        sinceId = null;
        toolbar.setTitle(R.string.toolbar_title_home);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_CLARITY).start();
        this.subscriptions.add(
            super.getRefreshObservable(this.swipeRefresh)
                .flatMap(unused -> {
                    this.swipeRefresh.setRefreshing(true);
                    return Api.papyruth().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null);
                })
                .map(evaluations -> evaluations.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations -> {
                        this.items.clear();
                        adapter.setIsEmptyData(evaluations.isEmpty());
                        this.items.addAll(evaluations);
                        this.adapter.notifyDataSetChanged();
                        this.swipeRefresh.setRefreshing(false);
                        size = items.size();
                    }, error -> error.printStackTrace(),
                    () -> {
                        this.swipeRefresh.setRefreshing(false);
                    })
        );
        this.subscriptions.add(
            super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, true)
                .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    // TODO : handle the case for max_id == 0 : prefer not to request to server
                    return Api.papyruth().get_evaluations(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        null,
                        sinceId == null ? null : sinceId - 1,
                        null,
                        null
                    );
                })
                .map(evaluations -> evaluations.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations -> {
                    this.progress.setVisibility(View.GONE);
                    if (evaluations != null) {
                        this.items.addAll(evaluations);

                        // TODO : Compare with below TODO Algorithm.
                        if (evaluations.get(evaluations.size() - 1).id < evaluations.get(0).id) {
                            sinceId = evaluations.get(evaluations.size() - 1).id;
                            maxId = evaluations.get(0).id;
                        } else {
                            sinceId = evaluations.get(0).id;
                            maxId = evaluations.get(evaluations.size() - 1).id;
                        }
                    }
                    this.adapter.notifyDataSetChanged();
                    if (firstId == null)
                        firstId = this.items.get(0).id;
                    size = items.size();
                    // TODO : Implement Better Algorithm.
//                for (EvaluationData evaluation : evaluations) {
//                    final int id = evaluation.id;
//                    if (maxId == null) maxId = id;
//                    else if (maxId < id) maxId = id;
//                    if (sinceId == null) sinceId = id;
//                    else if (sinceId > id) sinceId = id;
//                }
                }, error -> error.printStackTrace())
        );
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        Timber.d("current position : %s", position);
        if(User.getInstance().needMoreEvaluation()) {
            AlertMandatoryDialog.show(getActivity(), this.navigator);
            return;
        }
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
    protected EvaluationItemsDetailAdapter getAdapter () {
        return new EvaluationItemsDetailAdapter(this.items, this);
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks(R.id.fab_new_evaluation).subscribe(unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN));
    }
}