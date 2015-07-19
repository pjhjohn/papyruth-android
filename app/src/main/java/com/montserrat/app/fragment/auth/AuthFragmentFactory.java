package com.montserrat.app.fragment.auth;

import android.app.Fragment;

import com.montserrat.app.AppConst;
import com.montserrat.utils.view.viewpager.IFragmentFactory;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class AuthFragmentFactory implements IFragmentFactory{
    private AuthFragmentFactory() {}
    private static IFragmentFactory instance;
    public static synchronized IFragmentFactory getInstance() {
        if(AuthFragmentFactory.instance == null) instance = new AuthFragmentFactory();
        return AuthFragmentFactory.instance;
    }

    @Override
    public Fragment create(int position) {
        Fragment fragment = null;
        switch(position) {
            case AppConst.ViewPager.Auth.LOADING                : fragment = new LoadingFragment();         break;
            case AppConst.ViewPager.Auth.AUTH                   : fragment = new SignInFragment();            break;
            case AppConst.ViewPager.Auth.SIGNUP_UNIV: fragment = new SignUpStepUnivFragment();     break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP1: fragment = new SignUpStep1Fragment();     break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP2: fragment = new SignUpStep2Fragment();     break;
        }
        if (fragment == null) throw new RuntimeException(String.format("No Fragment found having position of %d.", position));
        return fragment;
    }
}
