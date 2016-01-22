package com.papyruth.android.fragment.main;

import android.view.View;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;

import java.util.concurrent.TimeUnit;

public class HomeFragment extends CommonRecyclerViewFragment<EvaluationItemsDetailAdapter>{

    @Override
    public void onResume() {
        super.onResume();
        if(Evaluation.getInstance().getId() != null){
            if(mEvaluationOpened && mEvaluationContainer.getVisibility() != View.VISIBLE && mEvaluationFragment != null) {
                openEvaluation(null, false);
            }else if(!mEvaluationOpened) {
                if(mEvaluationFragment == null) {
                    mEvaluationFragment = new EvaluationFragment();
                }
                openEvaluation(null, false);
            }
        } else {
            if(mEvaluationFragment != null) getFragmentManager().beginTransaction().remove(mEvaluationFragment).commit();
            mEvaluationOpened = false;
            mEvaluationContainer.setVisibility(View.GONE);
            setFloatingActionControl();
            setStatusBarOptions();
        }
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof EvaluationData) {
            EvaluationData data = (EvaluationData) object;
            if (User.getInstance().emailConfirmationRequired()) {
                AlertDialog.show(mContext, mNavigator, AlertDialog.Type.USER_CONFIRMATION_REQUIRED);
                return;
            }
            if (User.getInstance().mandatoryEvaluationsRequired()) {
                AlertDialog.show(mContext, mNavigator, AlertDialog.Type.MANDATORY_EVALUATION_REQUIRED);
                return;
            }
            if (mEvaluationOpened) return;
            if (mEvaluationIsOccupying) return;
            if (mAnimatorSet != null && mAnimatorSet.isRunning()) return;
            mEvaluationOpened = true;

            Evaluation.getInstance().setId(data.id);
            mEvaluationFragment = new EvaluationFragment();
            this.openEvaluation(view, true);
        }  else if(object instanceof Footer) {
            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
        }
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_red).show(true, 200, TimeUnit.MILLISECONDS);
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
        mToolbar.setTitle(R.string.toolbar_home);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_red).start();
        if(!mEvaluationOpened) setStatusBarOptions();
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, true);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
    }

    @Override
    protected EvaluationItemsDetailAdapter getAdapter() {
        return new EvaluationItemsDetailAdapter(mContext, mSwipeRefresh, mEmptyState, this);
    }

    @Override
    protected void setStatusBarOptions() {
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_red);
    }
}