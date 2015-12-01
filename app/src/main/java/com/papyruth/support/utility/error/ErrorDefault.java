package com.papyruth.support.utility.error;

import android.app.Activity;
import android.app.Fragment;

import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-12-01.
 */
public class ErrorDefault {
    public static boolean handle(Throwable throwable, Object object) {
        Timber.d("Error : %s\n%s", throwable.getMessage(), throwable.getCause());
        boolean sentToTracker = false;
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            if (fragment instanceof ErrorHandlerCallback) {
                ((ErrorHandlerCallback) fragment).sendErrorTracker(
                    ErrorHandler.setErrorDescription(throwable.getMessage()),
                    object.getClass().getSimpleName(),
                    false
                );
                sentToTracker = true;
            }
            if (fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (!sentToTracker && activity instanceof ErrorHandlerCallback) {
                    ((ErrorHandlerCallback) activity).sendErrorTracker(
                        ErrorHandler.setErrorDescription(throwable.getMessage()),
                        object.getClass().getSimpleName(),
                        false
                    );
                }
                return true;
            } else return false; // TODO : Handle when fragment doesn't have activity
        } else return false; // TODO : Handle when object is Activity
    }
}