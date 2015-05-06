package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;
import com.montserrat.utils.request.RecyclerViewFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends RecyclerViewFragment<UniversityRecyclerAdapter, UniversityRecyclerAdapter.Holder.Data> {
    private ViewPagerController pageController;
    private CompositeSubscription subscriptions;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pageController = (ViewPagerController) activity;
        this.subscriptions = new CompositeSubscription();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        this.vRecycler = (RecyclerView) view.findViewById(R.id.signup_univ_recyclerview);
        this.setupRecyclerView();

        subscriptions.add(RxVolley
            .createObservable(Api.url("universities"), Request.Method.GET, null, new JSONObject())
            .filter(response -> response.optInt("status") != 0)
            .map(response -> response.optJSONArray("universities"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(universities -> {
                for (int i = 0; i < universities.length(); i++) {
                    JSONObject university = universities.optJSONObject(i);
                    this.items.add(new UniversityRecyclerAdapter.Holder.Data(
                        university.optString("name"),
                        university.optString("email_domain"),
                        university.optString("image_url"),
                        university.optInt("id")
                    ));
                }
                this.adapter.notifyDataSetChanged();
            })
        );

        return view;
    }

    @Override
    protected UniversityRecyclerAdapter getAdapter (List<UniversityRecyclerAdapter.Holder.Data> items) {
        return UniversityRecyclerAdapter.newInstance(this.items, this, this);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(this.getActivity(), 2);
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        User.getInstance().setUniversityId(this.items.get(position).universityId);
        if ( User.getInstance().getCompletionLevel() >= 1 ) this.pageController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

}