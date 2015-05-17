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
import com.montserrat.app.model.Statistics;
import com.montserrat.app.model.User;
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

    private Action1<Statistics> viewSubscriber = statistics -> {
        /* Timer Done */
        if (statistics == null) this.timerDone = true;
        else {
            if (statistics.university == null) {
                this.vUnivText.setText(String.format("%d\nuniversities has", statistics.university_count));
                this.vUserText.setText(String.format("%d\nstudents with", statistics.user_count));
                this.vEvalText.setText(String.format("%d\nevaluations", statistics.evaluation_count));
                this.hasAuth = false;
            } else {
                Picasso.with(this.getActivity()).load(statistics.university.image_url).into(this.vUnivIcon);
                this.vUnivText.setText(String.format("%s\nhas", statistics.university.name));
                this.vUserText.setText(String.format("%d\nstudents with", statistics.university.user_count));
                this.vEvalText.setText(String.format("%d\nevaluations", statistics.university.evaluation_count));
                this.hasAuth = true;
            } this.requestDone = true;
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
        if(this.getUserVisibleHint()) onPageFocused();
    }

    @Override
    public void onPageFocused () {
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        subscriptions.add(RetrofitApi.getInstance().userinfo(User.getInstance().getAccessToken()).subscribe(
            response -> {
                User.getInstance().update(response.user);
                subscriptions.add(RetrofitApi.getInstance().statistics(User.getInstance().getAccessToken(), User.getInstance().getUniversityId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(viewSubscriber)
                );
            },
            error -> {
                if (error instanceof RetrofitError) {
                    switch (((RetrofitError) error).getResponse().getStatus()) {
                        case 401:
                            subscriptions.add(RetrofitApi.getInstance().statistics()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(viewSubscriber)
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
                .map(unused -> (Statistics) null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(viewSubscriber)
        );
    }
}