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
public class ErrorDefaultHTTP {
    public static boolean handle(RetrofitError throwable, Object object) {
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof ErrorHandlerCallback) {
                ((ErrorHandlerCallback) fragment).sendErrorTracker(
                    ErrorHandler.setErrorDescription(throwable.getMessage(), throwable.getUrl(), throwable.getResponse().getStatus()),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (!sentToTracker && activity instanceof ErrorHandlerCallback) {
                    ((ErrorHandlerCallback) activity).sendErrorTracker(
                        ErrorHandler.setErrorDescription(throwable.getMessage(), throwable.getUrl(), throwable.getResponse().getStatus()),
                        object.getClass().getSimpleName(),
                        false
                    );
                }
                return true;
            } else return false; // TODO : Handle when fragment doesn't have activity
        } else return false; // TODO : Handle when object is Activity
    }
}