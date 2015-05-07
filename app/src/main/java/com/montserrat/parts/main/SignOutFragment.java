package com.montserrat.parts.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.montserrat.activity.AuthActivity;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.auth.User;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 */
public class SignOutFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Timber.d("before %s", AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
        Timber.d("after %s", AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        Timber.d("Before : %s", User.getInstance().toString());
        User.getInstance().clear();
        Timber.d("After : %s", User.getInstance().toString());
        activity.startActivity(new Intent(activity, AuthActivity.class));
        activity.finish();
    }
}
