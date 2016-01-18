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
    public static ErrorHandleResult handle(RetrofitError throwable, Object object) {
        if(throwable.getResponse().getStatus() != 401){
            return new ErrorHandleResult(false);
        }
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof Error.OnReportToGoogleAnalytics) {
                ((Error.OnReportToGoogleAnalytics) fragment).onReportToGoogleAnalytics(
                    Error.description(throwable.getMessage(), throwable.getUrl(), 401),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (!sentToTracker && activity instanceof Error.OnReportToGoogleAnalytics) {
                    ((Error.OnReportToGoogleAnalytics) activity).onReportToGoogleAnalytics(
                        Error.description(throwable.getMessage(), throwable.getUrl(), 401),
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
                } return new ErrorHandleResult(true);
            } else return new ErrorHandleResult(false); // TODO : Handle when fragment doesn't have activity
        } else if(object instanceof Activity) {
            Activity activity = (Activity) object;
            if (activity instanceof Error.OnReportToGoogleAnalytics) {
                ((Error.OnReportToGoogleAnalytics) activity).onReportToGoogleAnalytics(
                    Error.description(throwable.getMessage(), throwable.getUrl(), 401),
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
                Intent intent = new Intent(activity, SplashActivity.class);
                activity.startActivity(intent);
                activity.finish();
            } return new ErrorHandleResult(true);
        } else return new ErrorHandleResult(false); // TODO : Handle when object is neither Activity nor Fragment
    }
}