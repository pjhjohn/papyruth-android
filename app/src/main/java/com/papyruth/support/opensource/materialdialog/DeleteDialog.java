package com.papyruth.support.opensource.materialdialog;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;

import rx.functions.Action0;

/**
 * Created by SSS on 2016-03-11.
 */
public class DeleteDialog {
    public enum Type{
        EVALUATION, COMMENT
    }
    public static void show(Context context, Type type, Action0 action0) {
        new MaterialDialog.Builder(context)
            .title(getTitle(type))
            .content(getContent(type))
            .positiveText(R.string.dialog_positive_delete)
            .negativeText(R.string.dialog_negative_cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    action0.call();
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                }
            })
            .show();
    }
    private static int getTitle(Type type){
        switch (type){
            case EVALUATION : return R.string.dialog_title_delete_evaluation;
            case COMMENT    : return R.string.dialog_title_delete_comment;
            default         : return R.string.empty;
        }
    }
    private static int getContent(Type type){
        switch (type){
            case EVALUATION : return R.string.dialog_content_delete_evaluation;
            case COMMENT    : return R.string.dialog_content_delete_comment;
            default         : return R.string.empty;
        }
    }
}
