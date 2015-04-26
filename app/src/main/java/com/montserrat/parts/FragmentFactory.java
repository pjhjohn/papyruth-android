package com.montserrat.parts;

import android.app.Fragment;

import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.AuthFragment;
import com.montserrat.parts.auth.SignUpStep1Fragment;
import com.montserrat.parts.auth.SignUpStep2Fragment;
import com.montserrat.parts.detail.DetailFragment_temp;
import com.montserrat.parts.main.MainFragment;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}
    /* TODO : static final integer VS enum */
    public enum Type {
        AUTH, DETAIL, HOME, SEARCH, SUGGEST, WRITE, RANDOM, PROFILE, SIGNOUT
    }

    public static Fragment create(Type type) {
        return FragmentFactory.create(type, 0);
    }

    public static Fragment create(Type type, int position) {
        Fragment fragment = null;
        switch (type) {
            case AUTH :
                switch(position) {
                    case AppConst.ViewPager.Auth.AUTH           : fragment = AuthFragment.newInstance();        break;
                    case AppConst.ViewPager.Auth.SIGNUP_STEP1   : fragment = SignUpStep1Fragment.newInstance(); break;
                    case AppConst.ViewPager.Auth.SIGNUP_STEP2   : fragment = SignUpStep2Fragment.newInstance(); break;
                } break;
            case DETAIL:
                switch(position) {
                    case AppConst.ViewPager.Detail.TEMP         : fragment = DetailFragment_temp.newInstance(); break;
                } break;
            case HOME :
                switch(position) {
                    case AppConst.ViewPager.Home.HOME           : fragment = MainFragment.newInstance();        break;
                } break;
            case SEARCH :
                switch(position) {
                    case AppConst.ViewPager.Search.DUMMY        : fragment = DummyFragment.newInstance();       break;
                } break;
            case SUGGEST:
                switch(position) {
                    case AppConst.ViewPager.Suggestion.DUMMY    : fragment = DummyFragment.newInstance();       break;
                } break;
            case WRITE :
                switch(position) {
                    case AppConst.ViewPager.Write.WRITE_STEP1   : fragment = DummyFragment.newInstance();       break;
                    case AppConst.ViewPager.Write.WRITE_STEP2   : fragment = DummyFragment.newInstance();       break;
                    case AppConst.ViewPager.Write.WRITE_STEP3   : fragment = DummyFragment.newInstance();       break;
                } break;
            case RANDOM :
                switch(position) {
                    case AppConst.ViewPager.Random.DUMMY        : fragment = DummyFragment.newInstance();       break;
                } break;
            case PROFILE :
                switch(position) {
                    case AppConst.ViewPager.Profile.DUMMY       : fragment = DummyFragment.newInstance();       break;
                } break;
            case SIGNOUT :
                switch(position) {
                    case AppConst.ViewPager.Signout.DUMMY       : fragment = DummyFragment.newInstance();       break;
                } break;
            default : break;
        }
        if (fragment == null) throw new RuntimeException(String.format("No Fragment found in Fragment.Type.%s with position of %d.", stringify(type), position));
        return fragment;
    }
    private static String stringify(Type type) {
        switch(type) {
            case AUTH   : return "AUTH";
            case DETAIL : return "DETAIL";
            case HOME   : return "HOME";
            case SEARCH : return "SEARCH";
            case SUGGEST: return "SUGGEST";
            case WRITE  : return "WRITE";
            case RANDOM : return "RANDOM";
            case PROFILE: return "PROFILE";
            case SIGNOUT: return "SIGNOUT";
            default : return "<UNHANDLED_TYPE>";
        }
    }
}
