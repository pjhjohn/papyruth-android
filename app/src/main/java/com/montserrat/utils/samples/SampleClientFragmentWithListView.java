package com.montserrat.utils.samples;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.montserrat.activity.R;
import com.montserrat.utils.request.ClientFragmentWithListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SampleClientFragmentWithListView extends ClientFragmentWithListView<SampleUniversalItemView> {
    public SampleClientFragmentWithListView () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Set ItemClick Listener for Action */
        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                // Do Something
            }
        });

        /* Request for Data */
        this.submit(new JSONObject());

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

    @Override
    protected int getListViewId () {
        return 0;
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
        try {
            if(response.getBoolean("success")) {
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = (JSONObject) data.get(i);
                    this.items.add(new SampleUniversalItemView(new SampleUniversalItemView.Data(
                            row.getString("subject"),
                            row.getString("professor"),
                            (float)row.getDouble("rating")
                    )));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.adapter.notifyDataSetChanged();
    }

    public static Fragment newInstance (int i) {
        return new SampleClientFragmentWithListView();
    }
}