package com.papyruth.android.fragment.auth;

import android.app.Fragment;

import com.papyruth.android.AppConst;
import com.papyruth.support.utility.viewpager.IFragmentFactory;

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
            case AppConst.ViewPager.Auth.SIGNIN       : fragment = new SignInFragment();        break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP1 : fragment = new SignUpStep1Fragment();   break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP2 : fragment = new SignUpStep2Fragment();   break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP3 : fragment = new SignUpStep3Fragment();   break;
            case AppConst.ViewPager.Auth.SIGNUP_STEP4 : fragment = new SignUpStep4Fragment();   break;
        }
        if (fragment == null) throw new RuntimeException(String.format("No Fragment found having position of %d.", position));
        return fragment;
    }
}
