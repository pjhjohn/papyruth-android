package com.montserrat.parts.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.melnykov.fab.FloatingActionButton;
import com.montserrat.activity.R;
import com.montserrat.utils.request.ClientFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends ClientFragment {
    public MainFragment() {}
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private List<RecyclerAdapter.Data> items;
    private RecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Bind Toolbar */
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar); //(Toolbar) view.getRootView().findViewById(R.id.toolbar);
        Log.d("DEBUG", "Currently toolbar in MainFragment is : " + this.toolbar);

        /* Bind fab */
        this.fab = (FloatingActionButton) view.findViewById(R.id.fab);

        /* Initializer for RecyclerView */
        this.items = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.main_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.adapter = new RecyclerAdapter(this.items);
        recyclerView.setAdapter(this.adapter);

        recyclerView.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                MainFragment.this.hideViews();
            }

            @Override
            public void onShow() {
                MainFragment.this.showViews();
            }
        });

        /* Request for Data */
        this.submit(new JSONObject());

        return view;
    }
    private void hideViews() {
        this.toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(
                new AccelerateInterpolator(2)
        );
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.fab.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        this.fab.animate().translationY(this.fab.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();

    }

    private void showViews() {
        this.toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        this.fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }


    /* TODO : FILL IT. It's necessary. */
    @Override
    protected int getFragmentLayoutId () {
        return R.layout.main_fragment;
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