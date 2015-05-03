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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

// TODO : TIMER for minimum loading period
public class LoadingFragment extends Fragment {
    private static final String TAG = "LoadingFragment";

    /* Setup ViewPagerController & Locale */
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

    private CompositeSubscription subscriptions = new CompositeSubscription();
    @Override
    public void onResume() {
        super.onResume();
        UserInfo.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        subscriptions.add(
            Observable.combineLatest(
                RxVolley
                    .createObservable("http://mont.izz.kr:3001/api/v1/users/me", UserInfo.getInstance().getAccessToken(), new JSONObject())
                    .flatMap(userData -> {
                        final int status = userData.optInt("status", -1);
                        Observable<JSONObject> chainedRequest = null;
                        switch (status) {
                            case 200:
                                UserInfo.getInstance().setData(userData.optJSONObject("user"));
                                chainedRequest = RxVolley.createObservable("http://mont.izz.kr:3001/api/v1/universities/" + UserInfo.getInstance().getUniversityId(), UserInfo.getInstance().getAccessToken(), new JSONObject());
                                break;
                            case 401:
                                chainedRequest = RxVolley.createObservable("http://mont.izz.kr:3001/api/v1/info", new JSONObject());
                                break;
                            default:
                                Log.e(TAG, "Non-handled status code : " + status);
                        }
                        return chainedRequest;
                    }),
                Observable.timer(4, TimeUnit.SECONDS),
                (chainedResponse, unused) -> chainedResponse
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    JSONObject university = response.optJSONObject("university");
                    if (university != null) {
                        Glide.with(this).load(university.optString("image_url", "")).into(this.vUnivIcon);
                        this.vUnivText.setText(String.format(locale, "%s\nhas", university.optString("name")));
                        this.vUserText.setText(String.format(locale, "%d\nstudents with", university.optInt("user_count")));
                        this.vEvalText.setText(String.format(locale, "%d\nevaluations", university.optInt("evaluation_count")));
                        this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                        this.getActivity().finish();
                    } else {
                        this.vUnivText.setText(String.format(locale, "%d\nuniversities has", response.optInt("university_count")));
                        this.vUserText.setText(String.format(locale, "%d\nstudents with", response.optInt("user_count")));
                        this.vEvalText.setText(String.format(locale, "%d\nevaluations", response.optInt("evaluation_count")));
                        this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.AUTH, false);
                    }
                }
            )
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( this.subscriptions != null ) this.subscriptions.unsubscribe();
    }

    public static Fragment newInstance() {
        return new LoadingFragment();
    }
}


