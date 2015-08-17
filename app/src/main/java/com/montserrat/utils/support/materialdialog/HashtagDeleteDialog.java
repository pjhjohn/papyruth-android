package com.montserrat.utils.support.materialdialog;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.montserrat.app.R;

import rx.functions.Func1;

/**
 *Created by pjhjohn on 2015-07-11.
 */

public class HashtagDeleteDialog {
    public static void show(Context context, ViewGroup container, TextView hashtag) {
        new MaterialDialog.Builder(context)
            .title(R.string.hashtag_delete_title)
            .content(R.string.hashtag_delete_content)
            .positiveText(R.string.confirm_delete)
            .negativeText(R.string.confirm_cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    container.removeView(hashtag);
                    dialog.dismiss();
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    dialog.dismiss();
                }
            })
            .show();
    }
    public static void show(Context context, ViewGroup container, Button hashtag, Func1<Void, Void> action) {
        new MaterialDialog.Builder(context)
            .title(R.string.hashtag_delete_title)
            .content(R.string.hashtag_delete_content)
            .positiveText(R.string.confirm_delete)
            .negativeText(R.string.confirm_cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    container.removeView(hashtag);
                    dialog.dismiss();
                    action.call(null);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    dialog.dismiss();
                }
            })
            .show();
    }
}
