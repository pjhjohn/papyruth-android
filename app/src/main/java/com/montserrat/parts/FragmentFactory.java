package com.montserrat.parts;

import android.app.Fragment;
import android.util.Log;

import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.AuthFragment;
import com.montserrat.parts.auth.SignUpInfoFragment;
import com.montserrat.parts.auth.SignUpUnivFragment;
import com.montserrat.parts.main.MainFragment;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}

    /* TODO : static final integer VS enum */
    public enum Type {
        MAIN, AUTH
    }

    public static Fragment create(Type type) {
        switch (type) {
            case MAIN :
                return MainFragment.newInstance();
            case AUTH :
                return AuthFragment.newInstance();
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
                    case AppConst.ViewPager.Auth.SIGNUP_UNIV :
                        return SignUpUnivFragment.newInstance();
                    case AppConst.ViewPager.Auth.SIGNUP_INFO :
                        return SignUpInfoFragment.newInstance();
                    default :
                        throw new RuntimeException("No Fragment found in position " + position);
                }
            default :
                return create(type);
        }
    }
}
