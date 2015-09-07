package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.unique.Statistics;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SplashFragment extends Fragment implements OnPageFocus {
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

    private CompositeSubscription subscriptions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        this.subscriptions = new CompositeSubscription();
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) onPageFocused();
    }

    @Override
    public void onPageFocused () {
        ((AuthActivity)this.getActivity()).signUpStep(5);
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        this.subscriptions.add(RetrofitApi.getInstance().users_me(User.getInstance().getAccessToken()).subscribe(
            response -> {
                User.getInstance().update(response.user);
                this.subscriptions.add(RetrofitApi.getInstance()
                    .universities(User.getInstance().getAccessToken(), User.getInstance().getUniversityId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(proceedToLoadingFragment)
                );
            },
            error -> {
                if (error instanceof RetrofitError) {
                    switch (((RetrofitError) error).getResponse().getStatus()) {
                        case 401:
                        case 419:
                            this.subscriptions.add(RetrofitApi.getInstance()
                                .get_info()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(proceedToLoadingFragment)
                            );
                            break;
                        default:
                            Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                    }
                }
            }
        ));

        subscriptions.add(Observable
            .timer(2, TimeUnit.SECONDS)
            .map(unused -> (StatisticsResponse) null)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(proceedToLoadingFragment)
        );
    }

    private boolean timerPending = true, requestPending = true;
    private Action1<StatisticsResponse> proceedToLoadingFragment = statistics -> {
        boolean authFailed = true;
        Statistics.getInstance().update(statistics);
        if (statistics != null) {
            requestPending = false;
            authFailed = statistics.university == null;
        } else timerPending = false;

        if (timerPending||requestPending) return;
        if (authFailed) this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.LOADING, false);
        else {
            this.subscriptions.add(
                RetrofitApi.getInstance().refresh_token(User.getInstance().getAccessToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        response -> {
                            User.getInstance().setAccessToken(response.access_token);
                            AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                            this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.LOADING, false);
                        },
                        error -> {
                            Timber.d("refresh error : %s", error);
                            error.printStackTrace();
                            this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.LOADING, false);
                        }
                    )
            );
        }
    };
}