package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;
import android.widget.Toast;

import com.papyruth.android.R;

import retrofit.RetrofitError;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class Error403 {
    public static ErrorHandleResult handle(Throwable throwable, Object object) {
        if(((RetrofitError) throwable).getResponse().getStatus() != 403){
            return new ErrorHandleResult(false);
        }
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
                Toast.makeText(activity, R.string.toast_error_retrofit_403, Toast.LENGTH_SHORT).show();
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