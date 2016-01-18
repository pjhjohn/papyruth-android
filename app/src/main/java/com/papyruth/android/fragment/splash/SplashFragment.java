package com.papyruth.android.fragment.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
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

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.activity.SplashActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.Circle;
import com.papyruth.support.utility.customview.CircleAngleAnimation;
import com.papyruth.support.utility.error.ErrorDefaultRetrofit;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.fragment.TrackerFragment;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SplashFragment extends TrackerFragment {
    @Bind(R.id.splash_background)           protected ImageView mSplashBackground;
    @Bind(R.id.splash_background_circle)    protected Circle mSplashBackgroundCircle;
    @Bind(R.id.splash_application_logo)     protected ImageView mSplashApplicationLogo;
    @Bind(R.id.splash_version_name)         protected RobotoTextView mVersionName;
    private CompositeSubscription mCompositeSubscription;
    private SplashActivity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (SplashActivity) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mCompositeSubscription != null) mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
        try {
            mVersionName.setText(String.format("Version %s", mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final boolean[] userHasAccessToken = {User.getInstance().getAccessToken() != null};
        Api.papyruth()
            .get_users_me(User.getInstance().getAccessToken())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    // TODO : Try to refresh Access Token
                    if(response == null || response.user == null) {
                        Toast.makeText(mActivity, "유저 정보를 가져오던 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        User.getInstance().update(response.user);
                        userHasAccessToken[0] = User.getInstance().getAccessToken() != null;
                        Api.papyruth()
                            .post_users_refresh_token(
                                    User.getInstance().getAccessToken(),
                                    AppConst.DEVICE_TYPE,
                                    AppManager.getInstance().getAppVersion(getActivity()),
                                    Build.VERSION.RELEASE,
                                    Build.MODEL
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                resp -> {
                                    if( resp != null && resp.access_token != null ) {
                                        User.getInstance().setAccessToken(resp.access_token);
                                        AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, resp.access_token);
                                    } else User.getInstance().clear();
                                    requestPending = false;
                                    startActivity();
                                },
                                err -> {
                                    boolean handled = false;
                                    if (err instanceof RetrofitError) {
                                        switch(((RetrofitError) err).getResponse().getStatus()) {
                                            case 401:
                                            case 419:
                                                handled = true;
                                                if(userHasAccessToken[0]) Toast.makeText(mActivity, "로그인 권한을 갱신하던 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                    if (!handled) {
                                        Timber.e("Unhandled Exception. Throws to ErrorHandler");
                                        err.printStackTrace();
                                        ErrorHandler.handle(err, this);
                                    }
                                    User.getInstance().clear();
                                    requestPending = false;
                                    startActivity();
                                }
                            );
                    }
                },
                error -> {
                    // TODO : if failed by netrowk problem, do not proceed (or tab to retry)
                    // TODO : if failed by authorization problem, proceed to AuthActivity
                    boolean handled = false;
                    if( error instanceof RetrofitError ) {
                        RetrofitError throwable = (RetrofitError) error;
                        switch (throwable.getKind()) {
                            case HTTP :
                                switch(((RetrofitError) error).getResponse().getStatus()) {
                                    case 401:
                                    case 419:
                                        handled = true;
                                        if(userHasAccessToken[0]) Toast.makeText(mActivity, "로그인 권한을 가져오던 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                break;
                            case NETWORK :
                                handled = ErrorNetwork.handle(throwable, this).handled;
                                if( handled ) Toast.makeText(mActivity, R.string.toast_error_retrofit_unstable_network, Toast.LENGTH_SHORT).show();
                                handled = false;
                                break;
                            default :
                                handled = ErrorDefaultRetrofit.handle(throwable, this).handled;
                        }
                    } else {
                        Timber.e("Uncatched Exception. Does Nothing");
                        error.printStackTrace();
                    }

                    // Start AuthActivity
                    User.getInstance().clear();
                    if(handled) {
                        requestPending = false;
                        startActivity();
                    }
                }
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
}