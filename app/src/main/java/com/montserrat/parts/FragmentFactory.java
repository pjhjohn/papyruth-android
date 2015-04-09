package com.montserrat.parts;

import android.app.Fragment;

import com.montserrat.parts.auth.AuthenticationFragment;
import com.montserrat.parts.main.MainFragment;

import org.apache.http.auth.AUTH;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}
    public static final int _MAIN = 0x01;
    public static final int _AUTH = 0X02;

    private static Fragment create(int fragmentId) {
        Fragment fragment = null;
        switch (fragmentId) {
//            case _MAIN :
//                return MainFragment.newInstance();
//            case _AUTH:
//                return AuthenticationFragment.newInstance();
//            ...
        } return fragment;
    }
}
