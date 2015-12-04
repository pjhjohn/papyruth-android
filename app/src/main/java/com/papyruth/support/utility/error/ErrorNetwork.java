package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;

import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class ErrorNetwork {
    public static boolean handle(RetrofitError throwable, Object object) {
        Timber.d("Network Error : %s\n%s\n%s", throwable.getMessage(), throwable.getUrl(), throwable.getCause());
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof Error.OnReportToGoogleAnalytics) {
                ((Error.OnReportToGoogleAnalytics) fragment).onReportToGoogleAnalytics(
                    Error.getDescription(throwable.getMessage(), throwable.getUrl()),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (!sentToTracker && activity instanceof Error.OnReportToGoogleAnalytics) {
                    ((Error.OnReportToGoogleAnalytics) activity).onReportToGoogleAnalytics(
                        Error.getDescription(throwable.getMessage(), throwable.getUrl()),
                        object.getClass().getSimpleName(),
                        false
                    );
                }
                return true;
            } else return false; // TODO : Handle when fragment doesn't have activity
        } else return false; // TODO : Handle when object is Activity
    }
}