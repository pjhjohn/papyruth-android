package com.montserrat.parts.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.montserrat.utils.request.ClientFragment;

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class AutoSigninFragment extends ClientFragment {
    public AutoSigninFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        // TODO : Fill in your code here.

        return view;
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
    }
}
