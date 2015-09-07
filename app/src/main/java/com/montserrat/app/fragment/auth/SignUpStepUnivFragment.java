package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.montserrat.app.model.unique.Signup;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.UniversityAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

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
        ((AuthActivity)this.getActivity()).signUpStep(0);
        FloatingActionControl.getInstance().hide(true);
        ((InputMethodManager)this.getActivity().getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.universityList.getWindowToken(), 2);
        if(Signup.getInstance().getUniversity_id() != null){
            this.universityList.getChildAt(getUniversityPosition()).setBackgroundColor(getResources().getColor(R.color.selected_gray));
//            this.universityList.getChildAt(getUniversityPosition()).setAlpha((float) 0.4);
        }
    }


    public int getUniversityPosition(){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).id == Signup.getInstance().getUniversity_id())
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
        if(Signup.getInstance().getUniversity_id() != null) {
            this.universityList.getChildAt(getUniversityPosition()).setBackgroundColor(getResources().getColor(R.color.transparent));
//            this.universityList.getChildAt(getUniversityPosition()).setAlpha((float)1.0);
        }
        Signup.getInstance().setUniversity_id(this.items.get(position).id);
        Signup.getInstance().setImage_url(this.items.get(position).image_url);
        this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP1, true);
    }

}