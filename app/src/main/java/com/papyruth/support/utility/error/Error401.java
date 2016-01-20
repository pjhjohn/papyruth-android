package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class Error401 {
    private static boolean report2GoogleAnalytics(RetrofitError throwable, Object object, boolean toast) {
        if (toast && object instanceof Context) Toast.makeText((Context) object, R.string.toast_error_retrofit_401, Toast.LENGTH_SHORT).show();
        if (object instanceof Error.OnReportToGoogleAnalytics) {
            ((Error.OnReportToGoogleAnalytics) object).onReportToGoogleAnalytics(
                Error.description(throwable.getMessage(), throwable.getUrl(), throwable.getResponse().getStatus()),
                object.getClass().getSimpleName(),
                false
            );
            return true;
        } return false;
    }
    private static void kickOut(Activity activity) {
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
        }
    }
    public static ErrorHandleResult handle(RetrofitError throwable, Object object) {
        return handle(throwable, object, false);
    }
    public static ErrorHandleResult handle(RetrofitError throwable, Object object, boolean toast) {
        if(throwable.getResponse().getStatus() != 401) return new ErrorHandleResult(false);
        boolean reported = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            reported = report2GoogleAnalytics(throwable, fragment, toast);
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if(!reported) reported = report2GoogleAnalytics(throwable, activity, toast);
                kickOut(activity);
                return new ErrorHandleResult(reported);
            } else return new ErrorHandleResult(reported);
        } else if(object instanceof Activity) {
            Activity activity = (Activity) object;
            reported = report2GoogleAnalytics(throwable, activity, toast);
            kickOut(activity);
            return new ErrorHandleResult(reported);
        } else return new ErrorHandleResult(reported);
    }
}