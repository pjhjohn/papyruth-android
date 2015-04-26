package com.montserrat.parts.lecture;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.navigation_drawer.NavFragment;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LecturesFragment extends ClientFragmentWithRecyclerView<LecturesRecyclerAdapter, LecturesRecyclerAdapter.Holder.Data> {
    private NavFragment.OnCategoryClickListener callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        this.swipeRefreshView.setEnabled(true);
        this.fabView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LecturesFragment.this.callback.onCategorySelected(NavFragment.Category.RATING);
            }
        });

        /* Request with Initializer Data. ENPOINT SHOULD BE DEFINED WITH */
        if(this.args!=null) try {
            final JSONObject initialDataToSubmit = new JSONObject(this.args.getString(AppConst.Resource.INITIALIZER, AppConst.Request.DEFAULT));
            this.setParameters(initialDataToSubmit).submit();
        } catch (JSONException e) {}

        return view;
    }

    @Override
    protected LecturesRecyclerAdapter getAdapter (List<LecturesRecyclerAdapter.Holder.Data> items) {
        return LecturesRecyclerAdapter.newInstance(this.items, this);
    }

    @Override
    public void onRequestResponse(JSONObject response) {
        try {
            if(response.getBoolean("success")) {
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.getJSONObject(i);
                    this.items.add(new LecturesRecyclerAdapter.Holder.Data(
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
    public void onRefreshResponse(JSONObject response) {
        try {
            if(response.getBoolean("success")) {
                JSONArray data = response.getJSONArray("data");
                this.items.clear();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.getJSONObject(i);
                    this.items.add(new LecturesRecyclerAdapter.Holder.Data(
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
        this.setAction("refresh").submit();
    }

    @Override
    public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        this.setAction("more").submit();
    }

    @Override
    public void onPendingRequest () {
        Toast.makeText(this.getActivity(), "Another request is pending...", Toast.LENGTH_SHORT).show();
    }

    public static Fragment newInstance () {
        Fragment fragment = new LecturesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "search");
        bundle.putString(AppConst.Request.ACTION, "refresh");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_home);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.main_recyclerview);
        bundle.putInt(AppConst.Resource.FAB, R.id.fab);
        bundle.putInt(AppConst.Resource.TOOLBAR, R.id.toolbar);
        bundle.putInt(AppConst.Resource.SWIPE_REFRESH, R.id.swipe);
        bundle.putInt(AppConst.Resource.CONTENT, R.id.content_main);
        bundle.putInt(AppConst.Resource.PROGRESS, R.id.progress_main);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        // To Lecture Page
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}