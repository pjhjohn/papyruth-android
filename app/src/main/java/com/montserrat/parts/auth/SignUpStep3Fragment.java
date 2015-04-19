package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.viewpager.ViewPagerController;

/**
 * Created by pjhjohn on 2015-04-19.
 */

public class SignUpStep3Fragment extends ClientFragment {
    private ViewPagerController pageController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pageController = (ViewPagerController) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        view.findViewById(R.id.btn_signup_submit).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SignUpStep3Fragment.this.getActivity().startActivity(new Intent(SignUpStep3Fragment.this.getActivity(), MainActivity.class));
            }
        });
        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpStep3Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "university");
        bundle.putString(AppConst.Request.ACTION, "all");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_step2);
        fragment.setArguments(bundle);
        return fragment;
    }
}
