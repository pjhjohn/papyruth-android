package com.papyruth.support.utility.error;

/**
 * Created by pjhjohn on 2015-12-04.
 */
public class Error {
    public static String description(String errorMsg, String requestUrl, int statusCode) {
        return String.format("Status Code : <%d>\nMessage : <%s>\nRequest url : <%s>", statusCode, errorMsg, requestUrl);
    }
    public static String description(String errorMsg, String requestUrl) {
        return String.format("Message : <%s>\nRequest url : <%s>", errorMsg, requestUrl);
    }
    public static String description(String errorMsg) {
        return String.format("Message : <%s>", errorMsg);
    }
    public interface OnReportToGoogleAnalytics {
        void onReportToGoogleAnalytics(String cause, String from, boolean isFatal);
    }
}
