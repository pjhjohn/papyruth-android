package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;

import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class ErrorNetwork {
    public static ErrorHandleResult handle(RetrofitError throwable, Object object) {
        if(throwable.getKind() != RetrofitError.Kind.NETWORK) return new ErrorHandleResult(false);
        Timber.d("Network Error : %s\n%s\n%s", throwable.getMessage(), throwable.getUrl(), throwable.getCause());
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof Error.OnReportToGoogleAnalytics) {
                ((Error.OnReportToGoogleAnalytics) fragment).onReportToGoogleAnalytics(
                    Error.description(throwable.getMessage(), throwable.getUrl()),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (!sentToTracker && activity instanceof Error.OnReportToGoogleAnalytics) {
                    ((Error.OnReportToGoogleAnalytics) activity).onReportToGoogleAnalytics(
                        Error.description(throwable.getMessage(), throwable.getUrl()),
                        object.getClass().getSimpleName(),
                        false
                    );
                } return new ErrorHandleResult(true);
            } else return new ErrorHandleResult(true); // TODO : Handle when fragment doesn't have activity
        } else if (object instanceof Activity) {
            Activity activity = (Activity) object;
            if (activity instanceof Error.OnReportToGoogleAnalytics) {
                ((Error.OnReportToGoogleAnalytics) activity).onReportToGoogleAnalytics(
                    Error.description(throwable.getMessage(), throwable.getUrl()),
                    object.getClass().getSimpleName(),
                    false
                );
            } return new ErrorHandleResult(true);
        } else return new ErrorHandleResult(true); // TODO : Handle when object is neither Activity nor Fragment
    }
}