package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.model.User;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class LoadingFragment extends Fragment {
    /* Set PageController */
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    /* Inflate Fragment View */
    @InjectView (R.id.loading_university_text) protected TextView vUnivText;
    @InjectView (R.id.loading_users_text) protected TextView vUserText;
    @InjectView (R.id.loading_evaluations_text) protected TextView vEvalText;
    @InjectView (R.id.loading_university_image) protected ImageView vUnivIcon;
    @InjectView (R.id.loading_users_image) protected ImageView vUserIcon;
    @InjectView (R.id.loading_evaluations_image) protected ImageView vEvalIcon;
    private CompositeSubscription subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }


    private boolean timerDone = false, requestDone = false;
    private Boolean hasAuth = null;
    private Action1<JSONObject> onNextJSONResponse = response -> {
        /* Timer Done */
        if (response == null) this.timerDone = true;

        /* Analyze Response */
        else {
            final int status = response.optInt("status", -1);
            switch(response.optInt("status")) {
            case 200 :
                JSONObject university = response.optJSONObject("university");
                if (university != null) {
                    Glide.with(this).load(university.optString("image_url", "")).into(this.vUnivIcon);
                    this.vUnivText.setText(String.format("%s\nhas", university.optString("name")));
                    this.vUserText.setText(String.format("%d\nstudents with", university.optInt("user_count")));
                    this.vEvalText.setText(String.format("%d\nevaluations", university.optInt("evaluation_count")));
                    this.hasAuth = true;
                } else {
                    this.vUnivText.setText(String.format("%d\nuniversities has", response.optInt("university_count", -100)));
                    this.vUserText.setText(String.format("%d\nstudents with", response.optInt("user_count", -100)));
                    this.vEvalText.setText(String.format("%d\nevaluations", response.optInt("evaluation_count", -100)));
                    this.hasAuth = false;
                } this.requestDone = true;
                break;
            default : Timber.e("Unexpected Status code : %d - Needs to be implemented", status);
            }
        }
        if (this.timerDone&&this.requestDone) {
            if ( hasAuth == null ) return; // TODO : make it to AuthFragment when testing is done
            if (!hasAuth ) this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.AUTH, false);
            else {
                this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                this.getActivity().finish();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        subscriptions.add(
            RxVolley
                .createObservable(Api.url("users/me"), User.getInstance().getAccessToken(), new JSONObject())
                .flatMap(userData -> {
                    final int status = userData.optInt("status", -1);
                    switch (status) {
                        case 200:
                            User.getInstance().setData(userData.optJSONObject("user"));
                            return RxVolley.createObservable(Api.url("universities/%s", User.getInstance().getUniversityId()), User.getInstance().getAccessToken(), new JSONObject());
                        case 401:
                            return RxVolley.createObservable(Api.url("info"), new JSONObject());
                        default:
                            Timber.e("Unexpected Status code : %d - Needs to be implemented", status);
                            return Observable.just(null);
                    }
                })
                .filter(chainedResponse -> chainedResponse != null) // filter nullified ( not desired ) observables.
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextJSONResponse)
        );
        subscriptions.add(
            Observable
                .timer(3, TimeUnit.SECONDS)
                .map(unused -> (JSONObject) null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextJSONResponse)
        );
    }
}