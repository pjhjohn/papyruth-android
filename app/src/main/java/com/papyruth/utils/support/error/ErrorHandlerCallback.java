package com.papyruth.utils.support.error;

/**
 * Created by SSS on 2015-11-17.
 */
public interface ErrorHandlerCallback {
    public void sendTracker(String cause, String from, boolean isFatal);
}
