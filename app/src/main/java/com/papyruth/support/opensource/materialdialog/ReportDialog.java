package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.retrofit.apis.Api;

import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

/**
 *Created by pjhjohn on 2015-07-11.
 */

public class ReportDialog {
    public static void show(Context context, Action1 action1) {
        new MaterialDialog.Builder(context)
            .title(R.string.dialog_title_report)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(R.string.dialog_content_report, R.string.empty, (dialog, input) -> {

            })
            .positiveText(R.string.dialog_positive_send)
            .negativeText(R.string.dialog_negative_cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    Timber.d("input text !! %s", dialog.getInputEditText().getText().toString());
                    action1.call(dialog.getInputEditText().getText().toString());
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                }
            })
            .show();
    }
}
