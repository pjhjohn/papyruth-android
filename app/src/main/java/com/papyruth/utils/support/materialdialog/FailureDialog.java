package com.papyruth.utils.support.materialdialog;

import android.content.Context;
import android.content.res.Resources;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;

/**
 *Created by pjhjohn on 2015-07-11.
 */

public class FailureDialog {
    public enum Type {
        CHANGE_EMAIL,
        CHANGE_NICKNAME,
        CHANGE_PASSWORD
    }

    public static MaterialDialog build(Context context, Type type) {
        return new MaterialDialog.Builder(context)
            .title(Title(context, type))
            .content(Content(context, type))
            .positiveText(R.string.confirm_positive)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    dialog.dismiss();
                }
            })
            .build();
    }

    public static void show(Context context, Type type) {
        FailureDialog.build(context, type).show();
    }

    private static String Title(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case CHANGE_EMAIL   : value = res.getString(R.string.change_email_title); break;
            case CHANGE_NICKNAME: value = res.getString(R.string.change_nickname_title); break;
            case CHANGE_PASSWORD: value = res.getString(R.string.change_password_title); break;
        } return String.format("%s %s %s", res.getString(R.string.failure_title_prefix), value, res.getString(R.string.failure_title_postfix));
    }

    private static String Content(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case CHANGE_EMAIL   : value = res.getString(R.string.change_email_content); break;
            case CHANGE_NICKNAME: value = res.getString(R.string.change_nickname_content); break;
            case CHANGE_PASSWORD: value = res.getString(R.string.change_password_content); break;
        } return String.format("%s %s %s", res.getString(R.string.failure_content_prefix), value, res.getString(R.string.failure_content_postfix));
    }
}
