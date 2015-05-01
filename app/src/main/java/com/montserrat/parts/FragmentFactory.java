package com.montserrat.parts;

import android.app.Fragment;
import android.os.Bundle;

import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.AuthFragment;
import com.montserrat.parts.auth.LoadingFragment;
import com.montserrat.parts.auth.SignUpStep1Fragment;
import com.montserrat.parts.auth.SignUpStep2Fragment;
<<<<<<< HEAD
import com.montserrat.parts.main.DetailFragment_temp;
import com.montserrat.parts.main.LecturesFragment;
import com.montserrat.parts.main.RatingStep1Fragment;
import com.montserrat.parts.main.RatingStep2Fragment;
import com.montserrat.parts.main.RatingStep3Fragment;
=======
import com.montserrat.parts.detail.DetailFragment;
import com.montserrat.parts.lecture.LecturesFragment;
import com.montserrat.parts.rating.RatingStep1Fragment;
import com.montserrat.parts.rating.RatingStep2Fragment;
import com.montserrat.parts.rating.RatingStep3Fragment;
>>>>>>> change_detail

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class FragmentFactory {
    private FragmentFactory () {}
    /* TODO : static final integer VS enum */
    public enum Type {
        AUTH, HOME, SEARCH, SUGGEST, RATING, RANDOM, PROFILE, SIGNOUT
    }

    public static Fragment create(Type type) {
        return FragmentFactory.create(type, 0);
    }

    public static Fragment create(Type type, int position) {
        Fragment fragment = null;
        switch (type) {
            case AUTH :
                switch(position) {
                    case AppConst.ViewPager.Auth.LOADING        : fragment = LoadingFragment.newInstance();     break;
                    case AppConst.ViewPager.Auth.AUTH           : fragment = AuthFragment.newInstance();        break;
                    case AppConst.ViewPager.Auth.SIGNUP_STEP1   : fragment = SignUpStep1Fragment.newInstance(); break;
                    case AppConst.ViewPager.Auth.SIGNUP_STEP2   : fragment = SignUpStep2Fragment.newInstance(); break;
                } break;
            case HOME :
                switch(position) {
                    case AppConst.ViewPager.Home.HOME           : fragment = LecturesFragment.newInstance();        break;
                } break;
            case SEARCH :
                switch(position) {
                    case AppConst.ViewPager.Search.DUMMY        : fragment = DummyFragment.newInstance();       break;
                } break;
            case SUGGEST:
                switch(position) {
                    case AppConst.ViewPager.Suggest.DUMMY       : fragment = DummyFragment.newInstance();       break;
                } break;
            case RATING:
                switch(position) {
                    case AppConst.ViewPager.Rating.RATING_STEP1: fragment = RatingStep1Fragment.newInstance();       break;
                    case AppConst.ViewPager.Rating.RATING_STEP2: fragment = RatingStep2Fragment.newInstance();       break;
                    case AppConst.ViewPager.Rating.RATING_STEP3: fragment = RatingStep3Fragment.newInstance();       break;
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
                    case AppConst.ViewPager.Signout.DUMMY       : fragment = DetailFragment.newInstance(); break;
                } break;
            default : break;
        }
        if (fragment == null) throw new RuntimeException(String.format("No Fragment found in Fragment.Type.%s with position of %d.", stringify(type), position));
        return fragment;
    }

    public static Fragment create(Type type, int position, JSONObject initializer) {
        Fragment fragment = FragmentFactory.create(type, position);
        if(fragment == null) return null;

        /* Set Data in JSONObject which needs to be requested */
        Bundle bundle = fragment.getArguments();
        bundle.putString(AppConst.Resource.INITIALIZER, initializer.toString()); // TODO : Parse into JSONObject at Fragment-side
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment create(Type type, int position, Bundle bundleToUpdate) {
        Fragment fragment = FragmentFactory.create(type, position);
        if(fragment == null) return null;

        Bundle bundle = fragment.getArguments();
        bundle.putAll(bundleToUpdate);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static String stringify(Type type) {
        switch(type) {
            case AUTH   : return "AUTH";
            case HOME   : return "HOME";
            case SEARCH : return "SEARCH";
            case SUGGEST: return "SUGGEST";
            case RATING: return "RATING";
            case RANDOM : return "RANDOM";
            case PROFILE: return "PROFILE";
            case SIGNOUT: return "SIGNOUT";
            default : return "<UNHANDLED_TYPE>";
        }
    }
}
