package com.papyruth.utils.support.materialdialog;

import android.content.Context;
import android.content.res.Resources;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.EvaluationStep1Fragment;
import com.papyruth.android.fragment.main.EvaluationStep2Fragment;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.navigator.Navigator;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class AlertDialog {
    public enum Type{
        EVALUATION_MANDATORY, EVALUATION_POSSIBLE
    }

    public static MaterialDialog build(Context context, Navigator navigator, Type type) {
        return new MaterialDialog.Builder(context)
            .content(Content(context, type))
            .positiveText(PositiveText(context, type))
            .negativeText(R.string.cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    doPositive(navigator, type);
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


    private static String Content(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case EVALUATION_MANDATORY   :   value = context.getResources().getString(R.string.inform_mandatory_evaluation, User.getInstance().getMandatoryEvaluationCount()); break;
            case EVALUATION_POSSIBLE    :   value = res.getString(R.string.inform_wrote_evaluation); break;
        } return value;
    }

    private static String PositiveText(Context context, Type type) {
        final Resources res = context.getResources();
        String value = "";
        switch (type) {
            case EVALUATION_MANDATORY   :   value = res.getString(R.string.goto_write); break;
            case EVALUATION_POSSIBLE    :   value = res.getString(R.string.goto_rewrite); break;
        } return value;
    }

    private static void doPositive(Navigator navigator, Type type) {
        switch (type) {
            case EVALUATION_MANDATORY   :   navigator.navigate(EvaluationStep1Fragment.class, true); break;
            case EVALUATION_POSSIBLE    :
                Observable.combineLatest(
                    Api.papyruth().get_evaluation(User.getInstance().getAccessToken(), EvaluationForm.getInstance().getEvaluationId()),
                    Api.papyruth().get_evaluation_hashtag(User.getInstance().getAccessToken(), EvaluationForm.getInstance().getEvaluationId()),
                    (a,b) -> {
                        EvaluationForm.getInstance().initForEdit(a.evaluation);
                        EvaluationForm.getInstance().setHashtag(b.hashtags);
                        return true;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                        navigator.navigate(EvaluationStep2Fragment.class, true);
                    });
                break;
        }
    }

    private static void doNegative(Type type) {
        switch (type) {
            case EVALUATION_MANDATORY   :  break;
            case EVALUATION_POSSIBLE    : EvaluationForm.getInstance().clear(); break;
        }
    }
}
