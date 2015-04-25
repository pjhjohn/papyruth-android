package com.montserrat.parts.detail;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.main.MainRecyclerAdapter;
import com.montserrat.parts.nav.NavListItemView;
import com.montserrat.utils.adapter.UniversalAdapter;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment_temp  extends ClientFragmentWithRecyclerView<MainRecyclerAdapter, MainRecyclerAdapter.Holder.Data> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        this.swipeRefreshView.setEnabled(true);
        this.fabView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DetailFragment_temp.this.submit();
            }
        });

        return view;
    }

    @Override
    protected MainRecyclerAdapter getAdapter (List<MainRecyclerAdapter.Holder.Data> items) {
        return MainRecyclerAdapter.newInstance(this.items, this);
    }

    @Override
    public void onRequestResponse(JSONObject response) {


    }

    @Override
    public void onRefreshResponse(JSONObject response) {

    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        this.setParameters(new JSONObject()).submit();
    }

    @Override
    public void onPendingRequest () {
        Toast.makeText(this.getActivity(), "Another request is pending...", Toast.LENGTH_SHORT).show();
    }

    public static Fragment newInstance () {
        Fragment fragment = new DetailFragment_temp();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "detail");
        bundle.putString(AppConst.Request.ACTION, "dummy");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_detail);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}