package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.viewpager.ViewPagerController;

import java.util.ArrayList;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep2Fragment extends ClientFragment {
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
//                SignUpStep2Fragment.this.pageController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP3);
                SignUpStep2Fragment.this.getActivity().startActivity(new Intent(SignUpStep2Fragment.this.getActivity(), MainActivity.class));
            }
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < 50; i ++) list.add(""+(2015-i));
        spinner.setAdapter(new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_item_year, list));
        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpStep2Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "university");
        bundle.putString(AppConst.Request.ACTION, "all");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_step2);
        fragment.setArguments(bundle);
        return fragment;
    }
}
