package com.montserrat.app.fragment;

import android.app.Fragment;

import com.montserrat.app.AppConst;
import com.montserrat.app.fragment.auth.AuthFragment;
import com.montserrat.app.fragment.auth.LoadingFragment;
import com.montserrat.app.fragment.auth.SignUpStep1Fragment;
import com.montserrat.app.fragment.auth.SignUpStep2Fragment;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}
    public static Fragment create(int position) {
        Fragment fragment = null;
        switch(position) {
            case AppConst.ViewPager.Auth.LOADING                : fragment = new LoadingFragment();         break;
            case AppConst.ViewPager.Auth.AUTH                   : fragment = new AuthFragment();            break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP1           : fragment = new SignUpStep1Fragment();     break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP2           : fragment = new SignUpStep2Fragment();     break;
        }
        if (fragment == null) throw new RuntimeException(String.format("No Fragment found having position of %d.", position));
        return fragment;
    }
}
