package com.montserrat.parts.subject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.montserrat.utils.request.ClientFragmentWithListView;

import org.json.JSONObject;

public class SubjectFragment extends ClientFragmentWithListView<SubjectListItemView> {
    public SubjectFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                // TODO : Fill your code here.
            }
        });

        return view;
    }
}