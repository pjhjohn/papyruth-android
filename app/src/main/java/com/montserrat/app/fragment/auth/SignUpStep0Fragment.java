package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.UniversityData;
import com.montserrat.app.model.unique.SignUpForm;
import com.montserrat.app.recyclerview.adapter.UniversityAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep0Fragment extends RecyclerViewFragment<UniversityAdapter, UniversityData> implements OnPageFocus, OnPageUnfocus {
    private ViewPagerController pagerController;

    @InjectView (R.id.signup_univ_recyclerview) protected RecyclerView universityList;
    private CompositeSubscription subscriptions;


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step0, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.universityList);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.subscriptions.add(
            RetrofitApi.getInstance().universities()
                .map(response -> response.universities)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(universities -> {
                    this.items.clear();
                    this.items.addAll(universities);
                    this.adapter.notifyDataSetChanged();
                    this.universityList.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            if(SignUpForm.getInstance().getUniversityId() != null)
                                universityList.getChildAt(getUniversityPosition()).setBackgroundColor(getResources().getColor(R.color.bg_accent));
                        }
                        @Override public void onChildViewRemoved(View parent, View child) { }
                    });
                }, error -> {
                    Timber.d("get university list error : %s", error);
                    error.printStackTrace();
                })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onPageFocused() {
        ((AuthActivity)this.getActivity()).signUpStep(0);
        FloatingActionControl.getInstance().setControl(R.layout.fab_next);
        if(this.subscriptions.isUnsubscribed())
            this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(
            ViewObservable
                .clicks(FloatingActionControl.getButton())
                .subscribe(unused -> {
                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
                }, error -> Timber.d("page change error %s", error))
        );

        if(SignUpForm.getInstance().getUniversityId() != null){
            FloatingActionControl.getInstance().show(true);
        }

        ((InputMethodManager)this.getActivity().getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.universityList.getWindowToken(), 2);
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscriptions !=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    public int getUniversityPosition(){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).id == SignUpForm.getInstance().getUniversityId())
                return i;
        }
        return -1;
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
        if(SignUpForm.getInstance().getUniversityId() != null) {
            this.universityList.getChildAt(getUniversityPosition()).setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        SignUpForm.getInstance().setUniversityId(this.items.get(position).id);
        SignUpForm.getInstance().setImageUrl(this.items.get(position).image_url);
        this.universityList.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.bg_accent));

        this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
    }

}