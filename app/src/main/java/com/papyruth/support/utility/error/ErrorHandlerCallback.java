package com.papyruth.support.utility.error;

/**
 * Created by SSS on 2015-11-17.
 */
public interface ErrorHandlerCallback {
    void sendErrorTracker(String cause, String from, boolean isFatal);
}