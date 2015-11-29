package com.papyruth.android.fragment.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.model.response.StatisticsResponse;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.Statistics;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.error.ErrorHandlerCallback;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.Circle;
import com.papyruth.utils.view.CircleAngleAnimation;
import com.papyruth.utils.view.panningview.PanningView;

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

public class SplashFragment extends Fragment implements ErrorHandlerCallback{
    @InjectView (R.id.splash_background) protected PanningView background;
    @InjectView (R.id.splash_circle) protected Circle circle;
    @InjectView (R.id.splash_logo) protected ImageView logo;
    private CompositeSubscription subscriptions;

    private Tracker mTracker;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.inject(this, view);
        ErrorHandler.setApiErrorCallback(this);
        this.subscriptions = new CompositeSubscription();
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        /* Api Call */
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        this.subscriptions.add(Api.papyruth().users_me(User.getInstance().getAccessToken()).subscribe(
            response -> {
                User.getInstance().update(response.user);
                this.subscriptions.add(Api.papyruth()
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
                            this.subscriptions.add(Api.papyruth()
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
                ErrorHandler.throwError(error, this);
            }
        ));
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);

        /* Background Sliding */
        background.startPanning();

        /* Animation */
        ValueAnimator animAlpha = ValueAnimator.ofFloat(0.0f, 1.0f);
        animAlpha.setDuration(600);
        animAlpha.setInterpolator(new DecelerateInterpolator(2.0f));
        animAlpha.addUpdateListener(anim -> {
            float alpha = (float) animAlpha.getAnimatedValue();
            logo.setAlpha(alpha);
        });
        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Observable.just((StatisticsResponse) null).delay(400, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(proceedToLoadingFragment);
            }
        });

        CircleAngleAnimation animCircle = new CircleAngleAnimation(circle, 360);
        animCircle.setDuration(600);
        animCircle.setInterpolator(new AccelerateDecelerateInterpolator());
        animCircle.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { animAlpha.start(); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        circle.startAnimation(animCircle);
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
        if (authFailed) ((SplashActivity) this.getActivity()).startAuthActivity();
        else {
            this.subscriptions.add(Api.papyruth()
                .users_refresh_token(User.getInstance().getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        User.getInstance().setAccessToken(response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                        ((SplashActivity) this.getActivity()).startAuthActivity();
                    },
                    error -> {
                        Timber.d("refresh error : %s", error);
                        error.printStackTrace();
                        ((SplashActivity) this.getActivity()).startAuthActivity();
                    }
                )
            );
        }
    };

    @Override
    public void sendErrorTracker(String cause, String from, boolean isFatal) {
        Timber.d("cause : %s, from : %s", cause, from);
        mTracker.send(
            new HitBuilders.ExceptionBuilder()
                .setDescription(cause)
                .setFatal(isFatal)
                .build());
    }
}