package com.montserrat.utils.samples;

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

public class SampleClientFragment extends ClientFragment {
    public SampleClientFragment (){}

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
        super.onResponse(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
    }
}