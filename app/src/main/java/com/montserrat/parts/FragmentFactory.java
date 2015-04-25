package com.montserrat.parts;

import android.app.Fragment;

import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.AuthFragment;
import com.montserrat.parts.auth.SignUpStep1Fragment;
import com.montserrat.parts.auth.SignUpStep2Fragment;
import com.montserrat.parts.detail.DetailFragment;
import com.montserrat.parts.detail.DetailFragment_temp;
import com.montserrat.parts.main.MainFragment;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}

    /* TODO : static final integer VS enum */
    public enum Type {
        MAIN, AUTH, DETAIL
    }

    public static Fragment create(Type type) {
        switch (type) {
            case MAIN :
                return MainFragment.newInstance();
            case AUTH :
                return AuthFragment.newInstance();
            case DETAIL:
                return DetailFragment_temp.newInstance();
            default :
                throw new RuntimeException("No Fragment found in type " + type);
        }
    }

    public static Fragment create(Type type, int position) {
        switch (type) {
            case MAIN :
                return create(type);
            case AUTH :
                switch(position) {
                    case AppConst.ViewPager.Auth.AUTH :
                        return AuthFragment.newInstance();
                    case AppConst.ViewPager.Auth.SIGNUP_STEP1 :
                        return SignUpStep1Fragment.newInstance();
                    case AppConst.ViewPager.Auth.SIGNUP_STEP2 :
                        return SignUpStep2Fragment.newInstance();
                    default :
                        throw new RuntimeException("No Fragment found in position " + position);
                }
            case DETAIL:
                return create(type);
            default :
                return create(type);
        }
    }
}
