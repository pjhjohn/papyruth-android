package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.widget.Toast;

import com.papyruth.android.R;

import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class ErrorDefault {
    private static boolean report2GoogleAnalytics(Throwable throwable, Object object, boolean toast) {
        if (toast && object instanceof Context) Toast.makeText((Context) object, R.string.toast_error_default, Toast.LENGTH_SHORT).show();
        if (object instanceof Error.OnReportToGoogleAnalytics) {
            ((Error.OnReportToGoogleAnalytics) object).onReportToGoogleAnalytics(
                Error.description(throwable.getMessage()),
                object.getClass().getSimpleName(),
                false
            );
            return true;
        } return false;
    }
    public static ErrorHandleResult handle(Throwable throwable, Object object) {
        return handle(throwable, object, false);
    }
    public static ErrorHandleResult handle(Throwable throwable, Object object, boolean toast) {
        boolean reported = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            reported = report2GoogleAnalytics(throwable, fragment, toast);
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if(!reported) reported = report2GoogleAnalytics(throwable, activity, toast);
                return new ErrorHandleResult(reported);
            } else return new ErrorHandleResult(reported);
        } else if(object instanceof Activity) {
            Activity activity = (Activity) object;
            reported = report2GoogleAnalytics(throwable, activity, toast);
            return new ErrorHandleResult(reported);
        } else return new ErrorHandleResult(reported);
    }
}