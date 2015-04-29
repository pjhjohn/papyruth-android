package com.montserrat.parts.rating;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.viewpager.ViewPagerController;
import com.montserrat.utils.viewpager.ViewPagerManager;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class RatingStep2Fragment extends ClientFragment  implements ViewPagerManager.onPageFocus {
    private ViewPagerController pagerController;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        ((Button)view.findViewById(R.id.btn_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                RatingStep2Fragment.this.pagerController.setCurrentPage(AppConst.ViewPager.Rating.RATING_STEP3, true);
            }
        });

        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new RatingStep2Fragment();
        Bundle bundle = new Bundle();
        /* For AutoComplete TextView for lecture title & professor name */
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_rating_step2);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onPageFocus () {
        if(RatingForm.getInstance().getCompletionLevel() < 1)
            this.pagerController.setCurrentPage(AppConst.ViewPager.Rating.RATING_STEP1, false);
    }
}
