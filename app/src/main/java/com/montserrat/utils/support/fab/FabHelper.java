package com.montserrat.utils.support.fab;

import android.view.ViewGroup;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppConst.ViewPager.Type;
import com.montserrat.app.fragment.FragmentFactory;

/**
 * Created by pjhjohn on 2015-05-20.
 */
public class FabHelper {
    public static void register(ViewGroup container, Type type, int position) {
        boolean out_of_boundary = false;
        switch (type) {
            case AUTH:
                if (position < AppConst.ViewPager.Auth.LENGTH) FabHelper.registerAuth(container, position);
                else out_of_boundary = true;
                break;
            case HOME:
                if (position < AppConst.ViewPager.Home.LENGTH) FabHelper.registerHome(container, position);
                else out_of_boundary = true;
                break;
            case SEARCH:
                if (position < AppConst.ViewPager.Search.LENGTH) FabHelper.registerSearch(container, position);
                else out_of_boundary = true;
                break;
            case RECOMMENDATION :
                if (position < AppConst.ViewPager.Recommendation.LENGTH) FabHelper.registerRecommendation(container, position);
                else out_of_boundary = true;
                break;
            case EVALUATION :
                if (position < AppConst.ViewPager.Evaluation.LENGTH) FabHelper.registerEvaluation(container, position);
                else out_of_boundary = true;
                break;
            case RANDOM :
                if (position < AppConst.ViewPager.Random.LENGTH) FabHelper.registerRandom(container, position);
                else out_of_boundary = true;
                break;
            case PROFILE :
                if (position < AppConst.ViewPager.Profile.LENGTH) FabHelper.registerProfile(container, position);
                else out_of_boundary = true;
                break;
            case SIGNOUT :
                if (position < AppConst.ViewPager.Signout.LENGTH) FabHelper.registerSignout(container, position);
                else out_of_boundary = true;
                break;
            default:
                throw new RuntimeException(String.format("Invalid ViewPager Type %s", type));
        } if (out_of_boundary) throw new RuntimeException(String.format("Position is out of boundary at ViewPager Type %s", FragmentFactory.stringify(type)));
    }


    private static void registerAuth(ViewGroup container, int position) {
        
    }

    private static void registerHome(ViewGroup container, int position) {

    }

    private static void registerSearch(ViewGroup container, int position) {

    }

    private static void registerRecommendation(ViewGroup container, int position) {

    }

    private static void registerEvaluation(ViewGroup container, int position) {

    }

    private static void registerRandom(ViewGroup container, int position) {

    }

    private static void registerProfile(ViewGroup container, int position) {

    }

    private static void registerSignout(ViewGroup container, int position) {

    }
}
