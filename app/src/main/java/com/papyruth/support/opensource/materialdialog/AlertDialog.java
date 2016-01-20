package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.EvaluationStep1Fragment;
import com.papyruth.android.fragment.main.EvaluationStep2Fragment;
import com.papyruth.android.fragment.main.ProfileRegisterUniversityEmailFragment;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.SignUpForm;
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
    public enum Type {
        MANDATORY_EVALUATION_REQUIRED, EVALUATION_ALREADY_REGISTERED, USER_CONFIRMATION_REQUIRED, UNIVERSITY_CONFIRMATION_REQUIRED, LEGACY_USER
    }

    public static MaterialDialog build(Context context, Navigator navigator, Type type) {
        return new MaterialDialog.Builder(context)
            .content(makeContent(context, type))
            .positiveText(makePositiveText(context, type))
            .negativeText(makeNegativeText(context, type))
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    actionOnPositive(context, navigator, type);
                }
                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    actionOnNegative(context, navigator, type);
                }
            })
            .build();
    }

    public static void show(Context context, Navigator navigator, Type type) {
        AlertDialog.build(context, navigator, type).show();
    }

    private static String makeContent(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case MANDATORY_EVALUATION_REQUIRED      : value = res.getString(R.string.dialog_content_alert_mandatory_evaluation_required, User.getInstance().getMandatoryEvaluationCount()); break;
            case EVALUATION_ALREADY_REGISTERED      : value = res.getString(R.string.dialog_content_alert_evaluation_already_registered); break;
            case USER_CONFIRMATION_REQUIRED         : value = res.getString(R.string.dialog_content_alert_user_confirmation_required); break;
            case UNIVERSITY_CONFIRMATION_REQUIRED   : value = String.format(res.getString(R.string.dialog_content_alert_university_confirmation_required), User.getInstance().getUniversityEmail()); break;
            case LEGACY_USER                        : value = String.format(res.getString(R.string.dialog_content_alert_legacy_user), SignUpForm.getInstance().getTempSaveEmail()); break;
        } return value;
    }

    private static String makePositiveText(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case MANDATORY_EVALUATION_REQUIRED      : value = res.getString(R.string.dialog_positive_compose); break;
            case EVALUATION_ALREADY_REGISTERED      : value = res.getString(R.string.dialog_positive_edit); break;
            case USER_CONFIRMATION_REQUIRED         : value = res.getString(R.string.dialog_positive_resend); break;
            case UNIVERSITY_CONFIRMATION_REQUIRED   : value = res.getString(R.string.dialog_positive_resend); break;
            case LEGACY_USER                        : value = res.getString(R.string.dialog_positive_send); break;
        } return value;
    }

    private static String makeNegativeText(Context context, Type type){
        final Resources res = context.getResources();
        String value;
        switch (type) {
            case UNIVERSITY_CONFIRMATION_REQUIRED   : value = res.getString(R.string.dialog_negative_change_email); break;
            default                                 : value = res.getString(R.string.dialog_negative_cancel); break;
        } return value;
    }

    private static void actionOnPositive(Context context, Navigator navigator, Type type) {
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
                Api.papyruth().post_email_confirm(User.getInstance().getAccessToken(), emailType)
                    .map(response -> response.success)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        success -> {
                            if(success) {
                                Toast.makeText(context, R.string.toast_alert_university_confirmation_email_sent, Toast.LENGTH_SHORT).show();
                                navigator.back();
                            } else Toast.makeText(context, R.string.toast_alert_university_confirmation_email_not_sent, Toast.LENGTH_SHORT).show();
                        }, error -> ErrorHandler.handle(error, MaterialDialog.class)
                    );
                break;
            case UNIVERSITY_CONFIRMATION_REQUIRED:
                emailType = Papyruth.EMAIL_CONFIRMATION_UNIVERSITY;
                Api.papyruth().post_email_confirm(User.getInstance().getAccessToken(), emailType)
                    .map(response -> response.success)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        success -> {
                            if(success) Toast.makeText(context, R.string.toast_alert_university_confirmation_email_sent, Toast.LENGTH_SHORT).show();
                            else Toast.makeText(context, R.string.toast_alert_university_confirmation_email_not_sent, Toast.LENGTH_SHORT).show();
                        }, error -> ErrorHandler.handle(error, MaterialDialog.class)
                    );
                break;
            case LEGACY_USER:
                Api.papyruth().post_email_migrate(SignUpForm.getInstance().getTempSaveEmail())
                    .map(response -> response.success)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        success -> {
                            if(success) Toast.makeText(context, R.string.toast_alert_legacy_user_email_sent, Toast.LENGTH_SHORT).show();
                            else Toast.makeText(context, R.string.toast_alert_legacy_user_email_not_sent, Toast.LENGTH_SHORT).show();
                        }, error -> ErrorHandler.handle(error, MaterialDialog.class)
                    );
                break;
        }
    }

    private static void actionOnNegative(Context context, Navigator navigator, Type type) {
        switch (type) {
            case EVALUATION_ALREADY_REGISTERED  : EvaluationForm.getInstance().clear(); break;
            case UNIVERSITY_CONFIRMATION_REQUIRED : navigator.navigate(ProfileRegisterUniversityEmailFragment.class, true); break;
        }
    }
}
