package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.EvaluationForm;

import rx.functions.Func0;
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
                    EvaluationForm.getInstance().removeHashtag(hashtag.getText().toString());
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
                    EvaluationForm.getInstance().removeHashtag(hashtag.getText().toString());
                    action.call(null);
                }
            })
            .show();
    }
    public static void show(Context context, String text, Func0<Void> action) {
        new MaterialDialog.Builder(context)
            .title(R.string.hashtag_delete_title)
            .content(R.string.hashtag_delete_content)
            .positiveText(R.string.confirm_delete)
            .negativeText(R.string.confirm_cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    EvaluationForm.getInstance().removeHashtag(text);
                    action.call();
                }
            })
            .show();
    }
}
