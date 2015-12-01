package com.papyruth.android.fragment.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.response.UserDataResponse;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorHandlerCallback;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.Circle;
import com.papyruth.support.utility.customview.CircleAngleAnimation;
import com.papyruth.support.utility.panningview.PanningView;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SplashFragment extends Fragment implements ErrorHandlerCallback{
    @InjectView (R.id.splash_background_panning)    protected PanningView mSplashBackgroundPanning;
    @InjectView (R.id.splash_background_circle)     protected Circle mSplashBackgroundCircle;
    @InjectView (R.id.splash_application_logo)      protected ImageView mSplashApplicationLogo;
    private CompositeSubscription mCompositeSubscription;
    private Tracker mTracker;
    private SplashActivity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTracker = ((papyruth) activity.getApplication()).getTracker();
        mActivity = (SplashActivity) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.inject(this, view);
        ErrorHandler.setApiErrorCallback(this);
        mCompositeSubscription = new CompositeSubscription();
        mSplashBackgroundPanning.startPanning();
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mCompositeSubscription != null) mCompositeSubscription.unsubscribe();
        mActivity.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        mCompositeSubscription.add(Api.papyruth()
            .users_me(User.getInstance().getAccessToken())
            .onErrorResumeNext(throwable -> {
                Timber.d("Error : @GET(\"/users/me\")");
                return Observable.just(UserDataResponse.ERROR());
            })
            .flatMap(response -> { // Handles User-data fetch
                if(response.success) {
                    User.getInstance().update(response.user);
                    return Api.papyruth().users_refresh_token(User.getInstance().getAccessToken());
                } else return Observable.just(UserDataResponse.ERROR());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> { // Handles Access-token refresh
                    if(response.success) {
                        User.getInstance().setAccessToken(response.access_token);
                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, response.access_token);
                    } else User.getInstance().clear();
                    requestPending = false;
                    startActivity();
                },
                error -> { // Collects all exceptions
                    boolean handled = false;
                    if (error instanceof RetrofitError) {
                        switch(((RetrofitError) error).getResponse().getStatus()) {
                            case 401:
                            case 419:
                                handled = true;
                                Toast.makeText(mActivity, "로그인 권한을 가져오던 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                    if (!handled) {
                        Timber.e("Unhandled Exception. Throws to ErrorHandler");
                        error.printStackTrace();
                        ErrorHandler.throwError(error, this);
                    }
                    User.getInstance().clear();
                    requestPending = false;
                    startActivity();
                }
            )
        );

        /* Animation */
        ValueAnimator animAlpha = ValueAnimator.ofFloat(0.0f, 1.0f);
        animAlpha.setDuration(600);
        animAlpha.setInterpolator(new DecelerateInterpolator(2.0f));
        animAlpha.addUpdateListener(anim -> mSplashApplicationLogo.setAlpha((float) animAlpha.getAnimatedValue()));
        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                Observable.timer(400, TimeUnit.MILLISECONDS, Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(unused -> {
                    timerPending = false;
                    startActivity();
                });
            }
        });
        CircleAngleAnimation animCircle = new CircleAngleAnimation(mSplashBackgroundCircle, 360);
        animCircle.setDuration(600);
        animCircle.setInterpolator(new AccelerateDecelerateInterpolator());
        animCircle.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { animAlpha.start(); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        mSplashBackgroundCircle.startAnimation(animCircle);
    }

    private boolean timerPending = true, requestPending = true;
    private void startActivity() {
        if (timerPending || requestPending) return;
        if (User.getInstance().getAccessToken() == null) mActivity.startActivity(AuthActivity.class);
        else mActivity.startActivity(MainActivity.class);
    }

    @Override
    public void sendErrorTracker(String cause, String from, boolean isFatal) {
        Timber.d("cause : %s, from : %s", cause, from);
        mTracker.send(new HitBuilders.ExceptionBuilder()
            .setDescription(cause)
            .setFatal(isFatal)
            .build()
        );
    }
}