package com.montserrat.parts.main;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.utils.requestable_fragment.JSONRequestableFragmentWithListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainFragment extends JSONRequestableFragmentWithListView<MainListItemView> {
    public MainFragment() {}

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
        try {
            this.form.submit();
        } catch (JSONException| UnsupportedEncodingException e) {
            Log.e (this.getClass().toString(), e.toString());
        }
        return view;
    }

    @Override
    protected int getFragmentLayoutId () {
        return R.layout.main_fragment;
    }
    @Override
    protected int getListViewId () {
        return R.id.main_listview;
    }
    @Override
    protected String getEndpoint () {
        return "http://pjhjohn.appspot.com/search";
    }
    @Override
    public void onSuccess (String responseBody) {
        super.onSuccess(responseBody);
        Toast.makeText(this.getActivity(), responseBody, Toast.LENGTH_LONG).show();
        JSONObject json = null;
        try {
            json = new JSONObject(responseBody);
        } catch(JSONException e) {}
        if(json == null) return;
        try {
            if(json.getBoolean("success")) {
                JSONArray data = json.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = (JSONObject)data.get(i);
                    this.items.add(new MainListItemView(new MainListItemView.Data(
                            row.getString("subject"),
                            row.getString("subject"),
                            (float)row.getDouble("rating")
                    )));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.adapter.notifyDataSetChanged();
    }
    @Override
    public void onTimeout(String error) {
        super.onTimeout(error);
        Toast.makeText(this.getActivity(), error, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onNoInternetConnection(String error) {
        super.onTimeout(error);
        Toast.makeText(this.getActivity(), error, Toast.LENGTH_LONG).show();
    }

    public static Fragment newInstance (int i) {
        return new MainFragment();
    }
}