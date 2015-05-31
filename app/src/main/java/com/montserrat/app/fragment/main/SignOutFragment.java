package com.montserrat.app.fragment.main;

import android.app.Fragment;
import android.content.Intent;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.view.viewpager.OnPageFocus;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-05-07.
 */
public class SignOutFragment extends Fragment implements OnPageFocus {
    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) this.onPageFocused();
    }

    @Override
    public void onPageFocused() {
        Timber.d("Signing out");
        AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
        User.getInstance().clear();
        this.getActivity().startActivity(new Intent(this.getActivity(), AuthActivity.class));
        this.getActivity().finish();
    }
}
