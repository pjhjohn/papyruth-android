package com.montserrat.parts.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.montserrat.utils.request.ClientFragment;
import com.montserrat.utils.request.JSONRequestableFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class AutoSigninFragment extends ClientFragment {
    public AutoSigninFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // TODO : Fill in your code here.

        return view;
    }

    /* TODO : FILL IT. It's necessary. */
    @Override
    protected int getFragmentLayoutId () {
        return 0;
    }

    @Override
    protected String getEndpoint () {
        return null;
    }

    /* TODO : Fill if necessary */
    @Override
    protected int getProgressViewId() {
        return 0;
    }

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    public void onResponse(JSONObject response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
