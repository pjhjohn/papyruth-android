package com.montserrat.parts.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.montserrat.activity.MainActivity;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by pjhjohn on 2015-04-12.
 */

// TODO : TIMER for minimum loading period
public class LoadingFragment extends Fragment {
    /* Setup ViewPagerController */
    private ViewPagerController pagerController;
    private Locale locale;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        locale = activity.getResources().getConfiguration().locale;
    }
    /* Inflate Fragment View */
    private TextView vUnivText, vUserText, vEvalText;
    private ImageView vUnivIcon, vUserIcon, vEvalIcon;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        this.vUnivText = (TextView) view.findViewById(R.id.loading_university_text);
        this.vUserText = (TextView) view.findViewById(R.id.loading_users_text);
        this.vEvalText = (TextView) view.findViewById(R.id.loading_evaluations_text);
        (this.vUnivIcon = (ImageView) view.findViewById(R.id.loading_university_image)).setImageResource(R.drawable.snu_logo);
        (this.vUserIcon = (ImageView) view.findViewById(R.id.loading_users_image)).setImageResource(R.drawable.snu_logo);
        (this.vEvalIcon = (ImageView) view.findViewById(R.id.loading_evaluations_image)).setImageResource(R.drawable.snu_logo);
        return view;
    }

    private Subscription requestSubscription;
    private Subscription timerSubscription;
    private Boolean requestReady;
    private boolean timerReady, proceedToMainActivity;
    @Override
    public void onResume() {
        super.onResume();
        requestReady = null;
        timerReady = false;
        proceedToMainActivity = false;
    }
    @Override
    public void onStart() {
        super.onStart();
        /* Step 1 : Get access-token -> Request user information */
        UserInfo userinfo = UserInfo.getInstance();
        if(userinfo.getAccessToken() == null || userinfo.getAccessToken().isEmpty()) userinfo.setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        requestSubscription = RxVolley.createObservable("http://mont.izz.kr:3001/api/v1/users/me", userinfo.getAccessToken(), new JSONObject())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    response -> { /* Success with valid token -> Fill up UserInfo and proceed */
                        requestSubscription.unsubscribe();
                        Log.e("DEBUG", response.toString());
                        UserInfo.getInstance().setData(response.optJSONObject("user"));
                        Log.e("DEBUG", "userinfo : " + UserInfo.getInstance());
                        Log.e("DEBUG", "Locale : " + locale);
                        this.fetchUniversityStatistics();
                    },
                    error -> { /* Token no longer valid -> To the AuthFragment */
                        if (error instanceof VolleyError) {
                            int code = ((VolleyError) error).networkResponse.statusCode;
                            if (code == 401) {
                                this.requestReady = true;
                                this.fetchGlobalStatistics();
                            }
                        } else error.printStackTrace();
                    }
            );
        /* Set Timer for 5 seconds */
        timerSubscription = Observable.just(true)
                .delay(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    Log.d("DEBUG", "Timer Terminated");
                    this.timerReady = true;
                    this.finish();
                });
    }

    /* Unsubscribe */
    @Override
    public void onStop() {
        super.onStop();
        if(requestSubscription!= null && !requestSubscription.isUnsubscribed()) requestSubscription.unsubscribe();
        if(timerSubscription!= null && !timerSubscription.isUnsubscribed()) timerSubscription.unsubscribe();
    }

    private void fetchUniversityStatistics () {
        if(!requestSubscription.isUnsubscribed()) requestSubscription.unsubscribe();
        requestSubscription =
            RxVolley.createObservable("http://mont.izz.kr:3001/api/v1/universities/" + UserInfo.getInstance().getUniversityId(), UserInfo.getInstance().getAccessToken(), new JSONObject())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    response -> { /* Success with valid token -> render view & proceed to MainActivity */
                        Log.d("DEBUG", response.toString());
                        JSONObject univ_data = response.optJSONObject("university");
                        Glide.with(this).load(univ_data.optString("image_url", "")).into(this.vUnivIcon);
                        this.vUnivText.setText(String.format(locale, "%s\nhas", univ_data.optString("name")));
                        this.vUserText.setText(String.format(locale, "%d\nstudents with", univ_data.optInt("user_count")));
                        this.vEvalText.setText(String.format(locale, "%d\nevaluations", univ_data.optInt("evaluation_count")));
                        this.requestSubscription.unsubscribe();
                        this.requestReady = true;
                        this.proceedToMainActivity = true;
                        this.finish();
                    }, Throwable::printStackTrace
            );
    }

    private void fetchGlobalStatistics() {
        if(!requestSubscription.isUnsubscribed()) requestSubscription.unsubscribe();
        requestSubscription =
            RxVolley.createObservable("http://mont.izz.kr:3001/api/v1/info", new JSONObject())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    response -> {
                        Log.d("DEBUG", response.toString());
                        this.vUnivText.setText(String.format(locale, "%d\nuniversities has", response.optInt("university_count")));
                        this.vUserText.setText(String.format(locale, "%d\nstudents with", response.optInt("user_count")));
                        this.vEvalText.setText(String.format(locale, "%d\nevaluations", response.optInt("evaluation_count")));
                        this.requestSubscription.unsubscribe();
                        this.requestReady = true;
                        this.finish();
                    }, Throwable::printStackTrace
            );
    }

    private void finish() {
        Log.d("DEBUG", String.format("<timerReady:%b> <requestReady:%b> <proceedToMainActivity:%b>", timerReady, requestReady, proceedToMainActivity));
        if (requestReady!= null && requestReady && timerReady) {
            if (proceedToMainActivity) {
                this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                this.getActivity().finish();
            } else {
                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.AUTH, false);
            }
        }
    }



    public static Fragment newInstance() {
        return new LoadingFragment();
    }
}


