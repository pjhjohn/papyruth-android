package com.montserrat.parts.rating;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class RatingStep2Fragment extends ClientFragment {
    public RatingStep2Fragment (){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new RatingStep2Fragment();
        Bundle bundle = new Bundle();
        /* For AutoComplete TextView for lecture title & professor name */
        bundle.putString(AppConst.Request.URL, "mont.izz.kr:3000");
        bundle.putString(AppConst.Request.CONTROLLER, "lectures");
        bundle.putString(AppConst.Request.ACTION, "autocomplete");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_rating_step1);
        fragment.setArguments(bundle);
        return fragment;
    }

}
