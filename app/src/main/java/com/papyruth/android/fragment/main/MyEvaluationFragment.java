package com.papyruth.android.fragment.main;

import android.view.View;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.recyclerview.adapter.MyEvaluationItemsAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;

import java.util.concurrent.TimeUnit;

public class MyEvaluationFragment extends CommonRecyclerViewFragment<MyEvaluationItemsAdapter> {

    @Override
    public void onResume() {
        super.onResume();

        if(Evaluation.getInstance().getId() != null){
            if(mEvaluationOpened && Evaluation.getInstance().getId() == null) {
                getFragmentManager().beginTransaction().remove(mEvaluationFragment).commit();
                mEvaluationOpened = false;
                mEvaluationContainer.setVisibility(View.GONE);
            }else if(mEvaluationOpened && mEvaluationContainer.getVisibility() != View.VISIBLE && mEvaluationFragment != null) {
                openEvaluation(null, false);
            }else if(!mEvaluationOpened) {
                if(mEvaluationFragment == null) {
                    mEvaluationFragment = new EvaluationFragment();
                }
                openEvaluation(null, false);
            }
        } else setFloatingActionControl();
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof EvaluationData){
            EvaluationData data = ((EvaluationData) object);

            if (mEvaluationOpened) return;
            if (mEvaluationIsOccupying) return;
            if (mAnimatorSet != null && mAnimatorSet.isRunning()) return;
            mEvaluationOpened = true;

            Evaluation.getInstance().setId(data.id);
            mEvaluationFragment = new EvaluationFragment();
            this.openEvaluation(view, true);
        }else if(object instanceof Footer){
            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
        }
    }
    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_blue).show(true, 200, TimeUnit.MILLISECONDS);
        mCompositeSubscription.add(
            FloatingActionControl.clicks().subscribe(
                unused -> {
                    EvaluationForm.getInstance().clear();
                    mNavigator.navigate(EvaluationStep1Fragment.class, true);
                },
                error -> ErrorHandler.handle(error, this)
            )
        );
    }

    @Override
    protected void setToolbarOptions() {
        mToolbar.setTitle(R.string.toolbar_my_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        if(!mEvaluationOpened) setStatusBarOptions();
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, true);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
    }

    @Override
    protected MyEvaluationItemsAdapter getAdapter() {
        return new MyEvaluationItemsAdapter(mContext, mSwipeRefresh, mEmptyState, this);
    }

    @Override
    protected void setStatusBarOptions() {
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
    }
}