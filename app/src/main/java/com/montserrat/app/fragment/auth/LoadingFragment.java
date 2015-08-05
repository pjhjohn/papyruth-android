package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
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

public class LoadingFragment extends Fragment implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

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

    private boolean timerFinished = false, responseActionFinished = false;
    private Boolean validAuthorization = null;

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) onPageFocused();
    }

    @Override
    public void onPageFocused () {
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        this.subscriptions.add(RetrofitApi.getInstance().users_me(User.getInstance().getAccessToken()).subscribe(
            response -> {
                User.getInstance().update(response.user);
                this.subscriptions.add(
                    RetrofitApi.getInstance().universities(User.getInstance().getAccessToken(), User.getInstance().getUniversityId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.actionWithStatistics)
                );
            },
            error -> {
                if (error instanceof RetrofitError) {
                    switch (((RetrofitError) error).getResponse().getStatus()) {
                        case 401:
                        case 419:
                            this.subscriptions.add(
                                RetrofitApi.getInstance().get_info()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this.actionWithStatistics)
                            );
                            break;
                        default:
                            Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                    }
                }
            }
        ));

        subscriptions.add(
            Observable
                .timer(3, TimeUnit.SECONDS)
                .map(unused -> (StatisticsResponse) null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(actionWithStatistics)
        );
    }

    /**
     * if user has valid token, auto sign in.<br/>
     * else invalid token, current page set SigninFragment.
     */

    private Action1<StatisticsResponse> actionWithStatistics = statistics -> {
        if (statistics == null) this.timerFinished = true;
        else {
            if (statistics.university == null) {
                this.vUnivText.setText(String.format("%d\nuniversities has", statistics.university_count));
                this.vUserText.setText(String.format("%d\nstudents with", statistics.user_count));
                this.vEvalText.setText(String.format("%d\nevaluations", statistics.evaluation_count));
                this.validAuthorization = false;
            } else {
                Picasso.with(this.getActivity()).load(statistics.university.image_url).into(this.vUnivIcon);
                this.vUnivText.setText(String.format("%s\nhas", statistics.university.name));
                this.vUserText.setText(String.format("%d\nstudents with", statistics.university.user_count));
                this.vEvalText.setText(String.format("%d\nevaluations", statistics.university.evaluation_count));
                this.validAuthorization = true;
            } this.responseActionFinished = true;
        }
        if (this.timerFinished && this.responseActionFinished) {
            if ( validAuthorization == null ) return;
            if (!validAuthorization) this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.AUTH, false);
            else {
                this.subscriptions.add(
                    RetrofitApi.getInstance().refresh_token(User.getInstance().getAccessToken())
                        .map(user -> user.access_token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            token -> {
                                User.getInstance().setAccessToken(token);
                                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, token);
                                this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                                this.getActivity().finish();
                            },error -> {
                                Timber.d("refresh error : %s", error);
                                error.printStackTrace();
                                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.AUTH, false);
                            }
                        )
                );
            }
        }
    };
}