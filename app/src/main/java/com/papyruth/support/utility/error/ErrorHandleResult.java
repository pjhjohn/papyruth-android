package com.papyruth.support.utility.error;

/**
 * Created by pjhjohn on 2015-12-04.
 */
public class ErrorHandleResult {
    public boolean handled;
    /* For EmptyStateView */
    public Integer code;
    public String title;
    public String body;

    public ErrorHandleResult(boolean handled) {
        this.handled = handled;
        this.code = null;
        this.title= null;
        this.body = null;
    }
}
