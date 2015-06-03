package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.UniversityAdapter;
import com.montserrat.app.model.University;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends RecyclerViewFragment<UniversityAdapter, University> implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView (R.id.signup_univ_recyclerview) protected RecyclerView recycler;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.recycler);

        this.subscriptions.add(
            RetrofitApi.getInstance().universities()
            .map(response -> response.universities)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(universities -> {
                this.items.clear();
                this.items.addAll(universities);
                this.adapter.notifyDataSetChanged();
            })
        );

        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().clear();
    }

    @Override
    protected UniversityAdapter getAdapter (List<University> universities) {
        return UniversityAdapter.newInstance(this.items, this, this);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(this.getActivity(), 2);
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        User.getInstance().setUniversityId(this.items.get(position).id);
        User.getInstance().setUniversityName(this.items.get(position).name);
        if (this.pagerController.getPreviousPage() == AppConst.ViewPager.Auth.SIGNUP_STEP2) {
            if (this.pagerController.getHistoryCopy().contains(AppConst.ViewPager.Auth.SIGNUP_STEP1)) this.pagerController.popCurrentPage();
            else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
        } else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
    }
}