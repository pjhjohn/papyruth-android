package com.papyruth.android.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.materialdialog.AlertDialog;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.fragment.CommonRecyclerViewFragment;
import com.papyruth.utils.view.navigator.FragmentNavigator;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeFragment extends CommonRecyclerViewFragment<EvaluationItemsDetailAdapter, EvaluationData> {

    private Integer sinceId = null, maxId = null, firstId = null, size = null;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_recent));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        sinceId = null;
        toolbar.setTitle(R.string.toolbar_title_home);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.toolbar_red).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);

        this.subscriptions.add(
            Api.papyruth().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null)
                .map(response -> response.evaluations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluations->{
                    this.items.clear();
                    adapter.setShowPlaceholder(evaluations.isEmpty());
                    this.items.addAll(evaluations);
                    this.adapter.notifyDataSetChanged();
                    size = items.size();
                }, error -> ErrorHandler.throwError(error, this))

        );

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
                        adapter.setShowPlaceholder(evaluations.isEmpty());
                        this.items.addAll(evaluations);
                        this.adapter.notifyDataSetChanged();
                        this.swipeRefresh.setRefreshing(false);
                        size = items.size();
                    }, error -> ErrorHandler.throwError(error, this),
                    () -> {
                        this.swipeRefresh.setRefreshing(false);
                    })
        );
        this.subscriptions.add(
            super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, true)
                .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE && !this.items.isEmpty())
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
                }, error -> ErrorHandler.throwError(error, this))
        );

    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(User.getInstance().needEmailConfirmed()){
            AlertDialog.show(getActivity(), navigator, AlertDialog.Type.NEED_CONFIRMATION);
            return;
        }
        if(User.getInstance().needMoreEvaluation()) {
            AlertDialog.show(getActivity(), this.navigator, AlertDialog.Type.EVALUATION_MANDATORY);
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
                this.openEvaluation(view, true);
            }, error->ErrorHandler.throwError(error, this));
    }

    @Override
    protected EvaluationItemsDetailAdapter getAdapter () {
        return new EvaluationItemsDetailAdapter(this.items, this);
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_red).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().subscribe(
            unused -> navigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN)
            ,error -> ErrorHandler.throwError(error, this)
        );
    }
}