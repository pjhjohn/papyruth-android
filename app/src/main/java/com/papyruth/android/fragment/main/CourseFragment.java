package com.papyruth.android.fragment.main;

import android.view.View;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.CourseAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.CommonRecyclerViewFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;

public class CourseFragment extends CommonRecyclerViewFragment<CourseAdapter> {
    @Override
    public void onResume() {
        super.onResume();
        setFloatingActionControl();
    }

    @Override
    protected void setFloatingActionControl() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_green).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().subscribe(unused -> navigateToEvaluationForm(), error -> ErrorHandler.handle(error, this));
    }

    @Override
    protected CourseAdapter getAdapter() {
        return new CourseAdapter(getActivity(), mSwipeRefresh, mEmptyState, mNavigator, this);
    }

    @Override
    protected void setToolbarOptions() {
        this.mToolbar.setTitle(R.string.toolbar_course);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, true);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
    }

    @Override
    protected void setStatusBarOptions() {
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_green);
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if (object instanceof EvaluationData) {
            if (User.getInstance().emailConfirmationRequired()) {
                AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.USER_CONFIRMATION_REQUIRED);
                return;
            }
            if (User.getInstance().mandatoryEvaluationsRequired()) {
                AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.MANDATORY_EVALUATION_REQUIRED);
                return;
            }
            if(!((EvaluationData) object).isValidData()){
                return;
            }
            if (mEvaluationOpened) return;
            if (mEvaluationIsOccupying) return;
            if (mAnimatorSet != null && mAnimatorSet.isRunning()) return;
            mEvaluationOpened = true;

            Evaluation.getInstance().setId(((EvaluationData) object).id);
            this.mEvaluationFragment = new EvaluationFragment();
            this.openEvaluation(view, true);
        } else if (object instanceof Footer) {
            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
        }
    }
    
    private void navigateToEvaluationForm() {
        EvaluationForm.getInstance().clear();
        EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
        EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
        EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessorName());

        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), EvaluationForm.getInstance().getCourseId())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    ((MainActivity) this.getActivity()).navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.build(getActivity(), mNavigator, AlertDialog.Type.EVALUATION_ALREADY_REGISTERED)
                        .show();
                }
            }, error -> ErrorHandler.handle(error, this));
    }
}
