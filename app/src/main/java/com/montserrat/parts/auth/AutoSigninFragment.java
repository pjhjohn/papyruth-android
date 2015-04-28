package com.montserrat.parts.auth;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.lecture.LecturesFragment;
import com.montserrat.utils.request.ClientFragment;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class AutoSigninFragment extends ClientFragment {
    public AutoSigninFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new AutoSigninFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "global_infos");
        bundle.putInt(AppConst.Resource.FRAGMENT, 0); // TODO : make a layout and assign id
        bundle.putInt(AppConst.Resource.CONTENT, 0); // TODO : make a layout and assign id
        bundle.putInt(AppConst.Resource.PROGRESS, 0); // TODO : make a layout and assign id
        fragment.setArguments(bundle);
        return fragment;
    }
}
