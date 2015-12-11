package com.papyruth.support.opensource.materialdialog;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class TermsOfServiceDialog {
    public static MaterialDialog build(Context context, String title, String body) {
        return new MaterialDialog.Builder(context)
            .title(title)
            .content(body)
            .positiveText(R.string.confirm_positive)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    dialog.dismiss();
                }
            })
            .build();
    }

    public static void show(Context context, String title, String body) {
        TermsOfServiceDialog.build(context, title, body).show();
    }
}
