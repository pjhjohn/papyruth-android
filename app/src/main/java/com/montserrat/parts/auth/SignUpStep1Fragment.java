package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class SignUpStep1Fragment extends ClientFragmentWithRecyclerView<UniversityRecyclerAdapter, UniversityRecyclerAdapter.Holder.Data> {
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
    protected UniversityRecyclerAdapter getAdapter (List<UniversityRecyclerAdapter.Holder.Data> items) {
        return UniversityRecyclerAdapter.newInstance(this.items, this, this);
    }

    @Override
    public void onPendingRequest () {
        Toast.makeText(this.getActivity(), "Multiple Request Attemption", Toast.LENGTH_SHORT).show(); // TODO : user R.string.~
    }

    @Override
    public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        //TODO : Implement it.
    }

    @Override
    public void onRequestResponse(JSONObject response) {
        try {
            JSONArray universities = response.getJSONArray("universities");
            for(int i = 0; i < universities.length(); i++) {
                JSONObject university = universities.getJSONObject(i);
                this.items.add(new UniversityRecyclerAdapter.Holder.Data(
                        university.getString("name"),
                        university.getString("email_domain"),
                        university.getString("image_url"),
                        university.getInt("id")
                ));
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
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "universities");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_signup_step1);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.signup_univ_recyclerview);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        Toast.makeText(this.getActivity(), "Current Position Has Position of " + position, Toast.LENGTH_SHORT).show();
        UserInfo.getInstance().setUniversityId(this.items.get(position).universityId);
        if ( UserInfo.getInstance().getCompletionLevel() >= 1 ) this.pageController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(this.getActivity(), 2);
    }
}