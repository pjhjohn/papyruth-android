package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.papyruth.android.R;
import com.papyruth.android.model.UserData;
import com.papyruth.android.model.VotersData;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by pjhjohn on 2015-07-11.
 */
public class InputDialog {

    public static void show(Context context){
        new MaterialDialog.Builder(context)
            .title(R.string.password_recovery_title)
            .content(R.string.enter_your_email)
            .input(R.string.hint_email, R.string.empty, (dialog, input) -> {

            })
            .positiveText(R.string.submit)
            .negativeText(R.string.confirm_cancel)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    //TODO : ADD fotgot password api
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                }
            })
            .build()
            .show();
    }
}
