package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;

import retrofit.RetrofitError;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class Error403 {
    public static boolean handle(RetrofitError throwable, Object object) {
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof ErrorHandlerCallback) {
                ((ErrorHandlerCallback) fragment).sendErrorTracker(
                    ErrorHandler.setErrorDescription(throwable.getMessage(), throwable.getUrl(), 403),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (!sentToTracker && activity instanceof ErrorHandlerCallback) {
                    ((ErrorHandlerCallback) activity).sendErrorTracker(
                        ErrorHandler.setErrorDescription(throwable.getMessage(), throwable.getUrl(), 403),
                        object.getClass().getSimpleName(),
                        false
                    );
                }
                if(activity instanceof MainActivity) {
                    /* Clear Data */
                    AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
                    Course.getInstance().clear();
                    Evaluation.getInstance().clear();
                    EvaluationForm.getInstance().clear();
                    SignUpForm.getInstance().clear();
                    User.getInstance().clear();

                    /* Back to Launch Activity */
                    Intent intent = new Intent(((Fragment) object).getActivity(), SplashActivity.class);
                    ((Fragment) object).getActivity().startActivity(intent);
                    ((Fragment) object).getActivity().finish();
                } return true;
            } else return false; // TODO : Handle when fragment doesn't have activity
        } else return false; // TODO : Handle when object is Activity
    }
}