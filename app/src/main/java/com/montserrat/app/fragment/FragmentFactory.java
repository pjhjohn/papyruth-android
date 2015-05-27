package com.montserrat.app.fragment;

import android.app.Fragment;
import android.os.Bundle;

import com.montserrat.app.AppConst;
import com.montserrat.app.fragment.auth.AuthFragment;
import com.montserrat.app.fragment.auth.LoadingFragment;
import com.montserrat.app.fragment.auth.SignUpStep1Fragment;
import com.montserrat.app.fragment.auth.SignUpStep2Fragment;
import com.montserrat.app.fragment.main.CourseFragment;
import com.montserrat.app.fragment.main.ProfileEditFragment;
import com.montserrat.app.fragment.main.ProfileFragment;
import com.montserrat.app.fragment.main.SearchCourseFragment;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.EvaluationStep2Fragment;
import com.montserrat.app.fragment.main.EvaluationStep3Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.SignOutFragment;

import com.montserrat.app.AppConst.ViewPager.Type;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}
    public static Fragment create(Type type, int position) {
        Fragment fragment = null;
        switch (type) {
            case AUTH :
                switch(position) {
                    case AppConst.ViewPager.Auth.LOADING                : fragment = new LoadingFragment();     break;
                    case AppConst.ViewPager.Auth.AUTH                   : fragment = new AuthFragment();        break;
                    case AppConst.ViewPager.Auth.SIGNUP_STEP1           : fragment = new SignUpStep1Fragment(); break;
                    case AppConst.ViewPager.Auth.SIGNUP_STEP2           : fragment = new SignUpStep2Fragment(); break;
                } break;
            case HOME :
                switch(position) {
                    case AppConst.ViewPager.Home.HOME                   : fragment = new HomeFragment();    break;
                } break;
            case SEARCH :
                switch(position) {
                    case AppConst.ViewPager.Search.BRIEF                : fragment = new SearchCourseFragment();       break;
                } break;
            case RECOMMENDATION:
                switch(position) {
                    case AppConst.ViewPager.Recommendation.DUMMY        : fragment = new DummyFragment();       break;
                } break;
            case EVALUATION:
                switch(position) {
                    case AppConst.ViewPager.Evaluation.EVALUATION_STEP1 :
                        fragment = new EvaluationStep1Fragment();
                        break;
                    case AppConst.ViewPager.Evaluation.EVALUATION_STEP2 : fragment = new EvaluationStep2Fragment();       break;
                    case AppConst.ViewPager.Evaluation.EVALUATION_STEP3 : fragment = new EvaluationStep3Fragment();       break;
                } break;
            case RANDOM :
                switch(position) {
                    case AppConst.ViewPager.Random.DUMMY                : fragment = new CourseFragment();       break;
                } break;
            case PROFILE :
                switch(position) {
                    case AppConst.ViewPager.Profile.PROFILE             : fragment = new ProfileFragment();       break;
                    case AppConst.ViewPager.Profile.PROFILE_EDIT        : fragment = new ProfileEditFragment();       break;
                } break;
            case SIGNOUT :
                switch(position) {
                    case AppConst.ViewPager.Signout.DUMMY               : fragment = new SignOutFragment(); break;
                } break;
            default : break;
        }
        if (fragment == null) throw new RuntimeException(String.format("No Fragment found in Fragment.Type.%s with position of %d.", stringify(type), position));
        return fragment;
    }

    public static String stringify(Type type) {
        switch(type) {
            case AUTH   : return "AUTH";
            case HOME   : return "HOME";
            case SEARCH : return "SEARCH";
            case RECOMMENDATION: return "RECOMMENDATION";
            case EVALUATION: return "EVALUATION";
            case RANDOM : return "RANDOM";
            case PROFILE: return "PROFILE";
            case SIGNOUT: return "SIGNOUT";
            default : return "<UNHANDLED_TYPE>";
        }
    }
}
