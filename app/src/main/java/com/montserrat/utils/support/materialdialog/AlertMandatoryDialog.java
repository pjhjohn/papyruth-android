package com.montserrat.utils.support.materialdialog;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.view.navigator.Navigator;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class AlertMandatoryDialog {

    public static MaterialDialog build(Context context, Navigator navigator) {
        return new MaterialDialog.Builder(context)
            .content(context.getResources().getString(R.string.inform_mandatory_evaluation, User.getInstance().getMandatory_evaluation_count()))
            .positiveText(R.string.goto_write)
            .negativeText(R.string.cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    navigator.navigate(EvaluationStep1Fragment.class, true);
                }
            })
            .show();
    }

    public static void show(Context context, Navigator navigator) {
        AlertMandatoryDialog.build(context, navigator).show();
    }
}
