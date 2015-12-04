package com.papyruth.support.utility.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by mrl on 2015-12-03.
 */
public class PermissionHelper {
    public static final int PERMISSION_CONTACTS = 0x1;
    public static final int PERMISSION_READ_CONTACTS = 0x2;

    public static boolean checkAndRequestPermission(Activity activity, int permissionRequestCode, String... permissions) {
        String[] requiredPermissions = getRequiredPermissions(activity, permissions);
        if (requiredPermissions.length > 0 /*&& !activity.isDestroyedCompat()*/) {
            ActivityCompat.requestPermissions(activity, requiredPermissions, permissionRequestCode);
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAndRequestPermission(Fragment fragment, int permissionRequestCode, String... permissions) {
        String[] requiredPermissions = getRequiredPermissions(fragment.getContext() != null ?
            fragment.getContext() : fragment.getActivity(), permissions);

        if (requiredPermissions.length > 0 && fragment.isAdded()) {
            fragment.requestPermissions(requiredPermissions, permissionRequestCode);
            return false;
        } else {
            return true;
        }
    }

    public static String[] getRequiredPermissions(Context context, String... permissions) {
        List<String> requiredPermissions = new ArrayList<>();

        // Context가 null이면 무조건 권한을 요청하도록 requiredPermissions가 존재한다고 reutrn 한다
        if (context == null) return requiredPermissions.toArray(new String[1]);

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(permission);
            }
        }
        return requiredPermissions.toArray(new String[requiredPermissions.size()]);
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) return false;

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    public static String getRationalMessage(Context context, int code) {
        switch (code) {
            case PERMISSION_CONTACTS:
                return getRationalMessage(context, context.getString(R.string.permission_get_accounts_rational), context.getString(R.string.permission_get_accounts));
            case PERMISSION_READ_CONTACTS :
                return getRationalMessage(context, context.getString(R.string.permission_read_contacts_rational), context.getString(R.string.permission_read_contacts));
        } return "";
    }

    public static String getRationalMessage(Context context, String rational, String permission) {
        return String.format(context.getString(R.string.permission_request), rational, permission);
    }

    public static void showRationalDialog(Context context, int resid) {
        showRationalDialog(context, context.getString(resid));
    }

    public static void showRationalDialog(Context context, String message) {
        new MaterialDialog.Builder(context)
            .title(R.string.permission_title)
            .content(message)
            .positiveText(R.string.dialog_btn_settings)
            .negativeText(R.string.dialog_btn_close)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        context.startActivity(intent);
                    }
                }
            }).show();
    }
}
