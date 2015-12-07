package com.papyruth.android.fragment.main;

import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.MyEvaluationItemsAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;

public class MyEvaluationFragment extends CommonRecyclerViewFragment<MyEvaluationItemsAdapter> {

    @Override
    public void onResume() {
        super.onResume();

        if(Evaluation.getInstance().getId() != null){
            mEvaluationFragment = new EvaluationFragment();
            openEvaluation(null, false);
        }else setFloatingActionControl();
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof EvaluationData){
            EvaluationData data = ((EvaluationData) object);

            if (mEvaluationOpened) return;
            if (mEvaluationIsOccupying) return;
            if (mAnimatorSet != null && mAnimatorSet.isRunning()) return;
            mEvaluationOpened = true;
            Api.papyruth()
                .get_evaluation(User.getInstance().getAccessToken(), data.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Evaluation.getInstance().update(response.evaluation);
                    mEvaluationFragment = new EvaluationFragment();
                    this.openEvaluation(view, true);
                }, error -> ErrorHandler.handle(error, this));
        }else if(object instanceof Footer){
            mRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }
    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_blue).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().subscribe(
            unused -> mNavigator.navigate(EvaluationStep1Fragment.class, true),
            error -> ErrorHandler.handle(error, this)
        );
    }

    @Override
    protected void setToolbarStatus() {
        mToolbar.setTitle(R.string.nav_item_my_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        setStatusBarDefault();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);
    }

    @Override
    protected MyEvaluationItemsAdapter getAdapter() {
        if (this.adapter == null)
            return adapter = new MyEvaluationItemsAdapter(mContext, mSwipeRefresh, mEmptyState, this);
        return adapter;
    }

    @Override
    protected void sendScreen() {
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_my_evaluation));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void setStatusBarDefault() {
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
    }
}