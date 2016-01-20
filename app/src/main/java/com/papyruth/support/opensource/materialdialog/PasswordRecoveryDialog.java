package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.text.InputType;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandleResult;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;

import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by pjhjohn on 2015-07-11.
 */
public class PasswordRecoveryDialog {
    public static void show(Context context) {
        new MaterialDialog.Builder(context)
            .title(R.string.dialog_title_password_recovery)
            .content(R.string.dialog_content_password_recovery)
            .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
            .input(R.string.hint_email, R.string.empty, (dialog, input) -> {
                Api.papyruth()
                    .post_email_password(input.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        response -> {
                            if(response.success)
                                Toast.makeText(context, R.string.toast_password_recovery_email_sent, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(context, R.string.toast_password_recovery_email_not_sent, Toast.LENGTH_SHORT).show();
                        }, error -> ErrorHandler.handle(error, context, true)
                    );
                }
            )
            .positiveText(R.string.dialog_positive_send)
            .negativeText(R.string.dialog_negative_cancel)
            .show();
    }
}
