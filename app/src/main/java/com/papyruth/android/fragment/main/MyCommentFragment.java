package com.papyruth.android.fragment.main;

import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.MyCommentData;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.MyCommentItemsAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;

public class MyCommentFragment extends CommonRecyclerViewFragment<MyCommentItemsAdapter>{

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof MyCommentData){
            MyCommentData data = ((MyCommentData) object);

            if (mEvaluationOpened) return;
            if (mEvaluationIsOccupying) return;
            if (mAnimatorSet != null && mAnimatorSet.isRunning()) return;
            mEvaluationOpened = true;
            Api.papyruth()
                .get_evaluation(User.getInstance().getAccessToken(), data.evaluation_id)
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
        mToolbar.setTitle(R.string.nav_item_my_comment);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        setStatusBarDefault();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);
    }

    @Override
    protected MyCommentItemsAdapter getAdapter() {
        if (this.adapter == null)
            return adapter = new MyCommentItemsAdapter(mContext, mSwipeRefresh, mEmptyState, this);
        return adapter;
    }

    @Override
    protected void sendScreen() {
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_my_comment));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void setStatusBarDefault() {
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
    }
}