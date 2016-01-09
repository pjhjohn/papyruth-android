package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.content.res.Resources;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;

/**
 *Created by pjhjohn on 2015-07-11.
 */

public class FailureDialog {
    public enum Type {
        REGISTER_UNIVERSITY_EMAIL,
        CHANGE_NICKNAME,
        CHANGE_PASSWORD
    }

    public static MaterialDialog build(Context context, Type type) {
        return new MaterialDialog.Builder(context)
            .title(makeTitle(context, type))
            .content(makeContent(context, type))
            .positiveText(R.string.dialog_positive_ok)
            .build();
    }

    public static void show(Context context, Type type) {
        FailureDialog.build(context, type).show();
    }

    private static String makeTitle(Context context, Type type) {
        final Resources res = context.getResources();
        String title = "";
        switch (type) {
            case REGISTER_UNIVERSITY_EMAIL  : title = res.getString(R.string.dialog_title_failure_register_university_email); break;
            case CHANGE_NICKNAME            : title = res.getString(R.string.dialog_title_failure_change_nickname); break;
            case CHANGE_PASSWORD            : title = res.getString(R.string.dialog_title_failure_change_password); break;
        } return title;
    }

    private static String makeContent(Context context, Type type) {
        final Resources res = context.getResources();
        String content = "";
        switch (type) {
            case REGISTER_UNIVERSITY_EMAIL  : content = res.getString(R.string.dialog_content_failure_register_university_email); break;
            case CHANGE_NICKNAME            : content = res.getString(R.string.dialog_content_failure_change_nickname); break;
            case CHANGE_PASSWORD            : content = res.getString(R.string.dialog_content_failure_change_password); break;
        } return content;
    }
}
