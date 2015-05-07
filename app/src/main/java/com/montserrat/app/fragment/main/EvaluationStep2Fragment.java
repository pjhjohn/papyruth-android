package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.viewpager.ViewPagerController;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep2Fragment extends ClientFragment {
    private ViewPagerController pagerController;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        view.findViewById(R.id.btn_next).setOnClickListener(v -> this.pagerController.setCurrentPage(AppConst.ViewPager.Evaluation.EVALUATION_STEP3, true));

        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new EvaluationStep2Fragment();
        Bundle bundle = new Bundle();
        /* For AutoComplete TextView for lecture title & professor name */
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_evaluation_step2);
        fragment.setArguments(bundle);
        return fragment;
    }
}
