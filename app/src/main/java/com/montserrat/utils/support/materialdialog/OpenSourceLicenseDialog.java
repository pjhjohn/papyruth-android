package com.montserrat.utils.support.materialdialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.afollestad.materialdialogs.MaterialDialog;
import com.montserrat.app.R;
import com.montserrat.app.model.OpenSourceLicenseData;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class OpenSourceLicenseDialog {
    public static MaterialDialog build(Context context, OpenSourceLicenseData data) {
        return new MaterialDialog.Builder(context)
            .title(data.name)
            .content(data.license)
            .positiveText(R.string.confirm_positive)
            .neutralText(R.string.goto_github_repo)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    dialog.dismiss();
                }

                @Override
                public void onNeutral(MaterialDialog dialog) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(data.repoUrl));
                    context.startActivity(intent);
                }
            })
            .build();
    }

    public static void show(Context context, OpenSourceLicenseData data) {
        OpenSourceLicenseDialog.build(context, data).show();
    }
}
