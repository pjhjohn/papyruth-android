package com.montserrat.parts.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.montserrat.utils.request.ClientFragmentWithListView;

import org.json.JSONObject;

public class DetailFragment extends ClientFragmentWithListView<DetailListItemView> {
    public DetailFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

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