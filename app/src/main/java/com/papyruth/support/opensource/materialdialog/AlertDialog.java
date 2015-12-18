package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.EvaluationStep1Fragment;
import com.papyruth.android.fragment.main.EvaluationStep2Fragment;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.retrofit.apis.Papyruth;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.navigator.Navigator;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class AlertDialog {
    public enum Type{
        MANDATORY_EVALUATION_REQUIRED, EVALUATION_ALREADY_REGISTERED, USER_CONFIRMATION_REQUIRED, UNIVERSITY_CONFIRMATION_REQUIRED
    }

    public static MaterialDialog build(Context context, Navigator navigator, Type type) {
        return new MaterialDialog.Builder(context)
            .content(content(context, type))
            .positiveText(positiveText(context, type))
            .negativeText(negativeText(context, type))
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    doPositive(context, navigator, type);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    doNegative(type);
                }
            })
            .show();
    }

    public static void show(Context context, Navigator navigator, Type type) {
        AlertDialog.build(context, navigator, type).show();
    }


    private static String content(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case MANDATORY_EVALUATION_REQUIRED      : value = context.getResources().getString(R.string.inform_mandatory_evaluation, User.getInstance().getMandatoryEvaluationCount()); break;
            case EVALUATION_ALREADY_REGISTERED      : value = res.getString(R.string.inform_wrote_evaluation); break;
            case USER_CONFIRMATION_REQUIRED         : value = res.getString(R.string.inform_email_confirm); break;
            case UNIVERSITY_CONFIRMATION_REQUIRED   : value = String.format(res.getString(R.string.inform_email_university_confirm), User.getInstance().getUniversityEmail()); break;
        } return value;
    }

    private static String positiveText(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case MANDATORY_EVALUATION_REQUIRED      : value = res.getString(R.string.goto_write); break;
            case EVALUATION_ALREADY_REGISTERED      : value = res.getString(R.string.goto_rewrite); break;
            case USER_CONFIRMATION_REQUIRED         : value = res.getString(R.string.confirm_positive); break;
            case UNIVERSITY_CONFIRMATION_REQUIRED   : value = res.getString(R.string.confirm_positive); break;
        } return value;
    }

    private static String negativeText(Context context, Type type){
        final Resources res = context.getResources();
        String value;
        switch (type) {
            case UNIVERSITY_CONFIRMATION_REQUIRED   : value = res.getString(R.string.common_change); break;
            default                                 : value = res.getString(R.string.common_cancel); break;
        } return value;
    }

    private static void doPositive(Context context, Navigator navigator, Type type) {
        Integer emailType = null;
        switch (type) {
            case MANDATORY_EVALUATION_REQUIRED:
                EvaluationForm.getInstance().clear();
                navigator.navigate(EvaluationStep1Fragment.class, true);
                break;
            case EVALUATION_ALREADY_REGISTERED:
                Api.papyruth().get_evaluation(User.getInstance().getAccessToken(), EvaluationForm.getInstance().getEvaluationId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                        EvaluationForm.getInstance().initForEdit(response.evaluation);
                        navigator.navigate(EvaluationStep2Fragment.class, true);
                    }, error -> ErrorHandler.handle(error, MaterialDialog.class));
                break;
            case USER_CONFIRMATION_REQUIRED:
                emailType = Papyruth.EMAIL_CONFIRMATION_USER;
            case UNIVERSITY_CONFIRMATION_REQUIRED:
                if(emailType == null) emailType = Papyruth.EMAIL_CONFIRMATION_UNIVERSITY;
                Api.papyruth().post_email_confirm(User.getInstance().getAccessToken(), emailType)
                    .map(response -> response.success)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        success -> {
                            if (success) {
                                Toast.makeText(context, R.string.success_send_email, Toast.LENGTH_SHORT).show();
                                navigator.back();
                            }else {
                                Toast.makeText(context, R.string.failure_send_email, Toast.LENGTH_SHORT).show();
                            }
                        }, error -> ErrorHandler.handle(error, MaterialDialog.class)
                    );
                break;
            default:
                break;
        }
    }

    private static void doNegative(Type type) {
        switch (type) {
            case MANDATORY_EVALUATION_REQUIRED  : break;
            case EVALUATION_ALREADY_REGISTERED  : EvaluationForm.getInstance().clear(); break;
        }
    }
}
