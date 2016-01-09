package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;
import com.papyruth.android.model.OpenSourceLicenseData;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class OpenSourceLicenseDialog {
    public static MaterialDialog build(Context context, OpenSourceLicenseData data) {
        return new MaterialDialog.Builder(context)
            .title(data.name)
            .content(data.license)
            .positiveText(R.string.dialog_positive_ok)
            .neutralText(R.string.dialog_neutral_repository)
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
