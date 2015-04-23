package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends ClientFragmentWithRecyclerView<SchoolRecyclerAdapter, SchoolRecyclerAdapter.Holder.Data>{
    private ViewPagerController pageController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pageController = (ViewPagerController) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.submit(); // TODO : request with api-defined form

        return view;
    }

    @Override
    protected SchoolRecyclerAdapter getAdapter (List<SchoolRecyclerAdapter.Holder.Data> items) {
        return SchoolRecyclerAdapter.newInstance(this.items, this);
    }

    @Override
    public void onPendingRequest () {
        Toast.makeText(this.getActivity(), "Multiple Request Attemption", Toast.LENGTH_SHORT).show(); // TODO : user R.string.~
    }

    @Override
    public void onRequestResponse(JSONObject response) {
        Log.d("DEBUG", "response data : " + response);
        try {
            if(response.getBoolean("success")) {
                JSONArray data = response.getJSONArray("data");
                for(int i = 0; i < data.length(); i ++) {
                    JSONObject row = data.getJSONObject(i);
                    this.items.add(new SchoolRecyclerAdapter.Holder.Data(
                            row.getString("name"),
                            row.getInt("code")
                    ));
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshResponse(JSONObject response) {

    }

    public static Fragment newInstance() {
        Fragment fragment = new SignUpStep1Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.URL, "pjhjohn.appspot.com");
        bundle.putString(AppConst.Request.CONTROLLER, "university");
        bundle.putString(AppConst.Request.ACTION, "all");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_step1);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.signup_univ_recyclerview);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        Toast.makeText(this.getActivity(), "Current Position Has Position of " + position, Toast.LENGTH_SHORT).show();
        UserInfo.getInstance().setSchool(this.items.get(position).schoolCode);
        if(UserInfo.getInstance().isDataReadyOnStep1()) this.pageController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}