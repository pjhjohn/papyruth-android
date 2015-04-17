package com.montserrat.parts.auth;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragment;

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpUnivFragment extends ClientFragment {
    public SignUpUnivFragment (){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpUnivFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_auth);
        fragment.setArguments(bundle);
        return fragment;
    }
}
