package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.widget.Toast;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.SignUpForm;
import com.papyruth.android.model.unique.User;

import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class Error401 {
    public static ErrorHandleResult handle(Throwable throwable, Object object) {
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof Error.OnReportToGoogleAnalytics) {
                ((Error.OnReportToGoogleAnalytics) fragment).onReportToGoogleAnalytics(
                    Error.description(throwable.getMessage()),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                Toast.makeText(activity, R.string.toast_not_owner, Toast.LENGTH_SHORT).show();
                if (!sentToTracker && activity instanceof Error.OnReportToGoogleAnalytics) {
                    ((Error.OnReportToGoogleAnalytics) activity).onReportToGoogleAnalytics(
                        Error.description(throwable.getMessage()),
                        object.getClass().getSimpleName(),
                        false
                    );
                } return new ErrorHandleResult(true);
            } else return new ErrorHandleResult(false); // TODO : Handle when fragment doesn't have activity
        } else return new ErrorHandleResult(false); // TODO : Handle when object is Activity
    }
}