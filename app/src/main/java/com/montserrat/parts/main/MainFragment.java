package com.montserrat.parts.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainFragment extends ClientFragmentWithRecyclerView<RecyclerAdapter, RecyclerAdapter.Data> {
    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        this.swipeRefreshView.setEnabled(true);

        return view;
    }

    @Override
    protected RecyclerAdapter getAdapter (List<RecyclerAdapter.Data> items) {
        return new RecyclerAdapter(this.items);
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

    @Override
    public void onRefresh() {
        super.onRefresh();
        this.setParameters(new JSONObject()).submit();
    }

    @Override
    public void anotherRequestInProgress () {
        Toast.makeText(this.getActivity(), "Multiple Request Attemption", Toast.LENGTH_SHORT).show();
    }

    public static Fragment newInstance () {
        Fragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "search");
        bundle.putString(AppConst.Request.ACTION, "dummy");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_main);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.main_recyclerview);
        bundle.putInt(AppConst.Resource.FAB, R.id.fab);
        bundle.putInt(AppConst.Resource.TOOLBAR, R.id.toolbar);
        bundle.putInt(AppConst.Resource.SWIPE_REFRESH, R.id.swipe);
        fragment.setArguments(bundle);
        return fragment;
    }
}