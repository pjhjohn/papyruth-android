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
import com.montserrat.app.recyclerview.adapter.UniversityAdapter;
import com.montserrat.app.model.UniversityData;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStepUnivFragment extends RecyclerViewFragment<UniversityAdapter, UniversityData> implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.pagerController = null;
    }

    @InjectView (R.id.signup_univ_recyclerview) protected RecyclerView universityList;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_univ, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.universityList);

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
    protected UniversityAdapter getAdapter () {
        return new UniversityAdapter(this.items, this);
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(this.getActivity(), 2);
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        User.getInstance().setUniversityId(this.items.get(position).id);
        User.getInstance().setUniversityName(this.items.get(position).name);
        if (this.pagerController.getPreviousPage() == AppConst.ViewPager.Auth.SIGNUP_STEP1) {
            if (this.pagerController.getHistoryCopy().contains(AppConst.ViewPager.Auth.SIGNUP_UNIV)) this.pagerController.popCurrentPage();
            else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
        } else this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
    }
}