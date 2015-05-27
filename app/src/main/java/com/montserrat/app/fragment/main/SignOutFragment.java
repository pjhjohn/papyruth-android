package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.unique.User;

/**
 * Created by pjhjohn on 2015-05-07.
 */
public class SignOutFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
        User.getInstance().clear();
        activity.startActivity(new Intent(activity, AuthActivity.class));
        activity.finish();
    }
}
