package com.montserrat.parts.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.activity.R;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainFragment extends ClientFragmentWithRecyclerView<RecyclerAdapter, RecyclerAdapter.Data> {
    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Request for Data */
        this.submit(new JSONObject());

        return view;
    }

    @Override
    protected int getToolbarId() {
        return R.id.toolbar;
    }

    @Override
    protected int getFloatingActionButtonId() {
        return R.id.fab;
    }

    @Override
    protected RecyclerAdapter getAdapter (List<RecyclerAdapter.Data> items) {
        return new RecyclerAdapter(this.items);
    }

    @Override
    protected int getRecyclerViewId () {
        return R.id.main_recyclerview;
    }



    /* TODO : FILL IT. It's necessary. */
    @Override
    protected int getFragmentLayoutId () {
        return R.layout.fragment_main;
    }

    @Override
    protected String getEndpoint () {
        return "http://pjhjohn.appspot.com/search";
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
                    this.items.add(new RecyclerAdapter.Data(
                            row.getString("subject"),
                            row.getString("professor"),
                            (float)row.getDouble("rating")
                    ));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.adapter.notifyDataSetChanged();
    }

    public static Fragment newInstance (int i) {
        return new MainFragment();
    }
}