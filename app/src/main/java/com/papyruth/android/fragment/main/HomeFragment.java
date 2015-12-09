package com.papyruth.android.fragment.main;

import android.view.View;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.EvaluationItemsDetailAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.FragmentNavigator;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;

public class HomeFragment extends CommonRecyclerViewFragment<EvaluationItemsDetailAdapter>{

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
        if(object instanceof EvaluationData) {
            EvaluationData data = (EvaluationData) object;
            if (User.getInstance().needEmailConfirmed()) {
                AlertDialog.show(mContext, mNavigator, AlertDialog.Type.NEED_CONFIRMATION);
                return;
            }
            if (User.getInstance().needMoreEvaluation()) {
                AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_MANDATORY);
                return;
            }
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
        }  else if(object instanceof Footer) {
            mRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_red).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().subscribe(
            unused -> mNavigator.navigate(EvaluationStep1Fragment.class, true, FragmentNavigator.AnimatorType.SLIDE_TO_DOWN),
            error -> ErrorHandler.handle(error, this)
        );
    }

    @Override
    protected void setToolbarStatus() {
        mToolbar.setTitle(R.string.toolbar_title_home);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_red).start();
        setStatusBarDefault();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);
    }


    @Override
    protected EvaluationItemsDetailAdapter getAdapter() {
        if (this.adapter == null)
            return adapter = new EvaluationItemsDetailAdapter(mContext, mSwipeRefresh, mEmptyState, this);
        return adapter;
    }


    @Override
    protected void setStatusBarDefault() {
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_red);
    }
}