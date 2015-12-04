package com.papyruth.android.activity;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.fragment.auth.SignInFragment;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.customview.FloatingActionControlContainer;
import com.papyruth.support.utility.error.Error;
import com.papyruth.support.utility.navigator.FragmentNavigator;
import com.papyruth.support.utility.navigator.NavigationCallback;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.softkeyboard.SoftKeyboardActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends SoftKeyboardActivity implements com.papyruth.support.utility.navigator.Navigator, Error.OnReportToGoogleAnalytics {
    @InjectView(R.id.fac)                       protected FloatingActionControlContainer mFloatingActionControlContainer;
    @InjectView(R.id.auth_app_logo)             protected ImageView mApplicationLogo;
    @InjectView(R.id.auth_app_logo_horizontal)  protected ImageView mApplicationLogoHorizontal;
    @InjectView(R.id.auth_signup_progress)      protected ProgressBar mSignUpProgress;
    @InjectView(R.id.auth_signup_label)         protected TextView mSignUpLabel;
    private FragmentNavigator mNavigator;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);
        this.attachSoftKeyboardListeners();
        mTracker = ((PapyruthApplication) getApplication()).getTracker();
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(mFloatingActionControlContainer);
        mNavigator = new FragmentNavigator(this.getFragmentManager(), R.id.auth_navigator, SignInFragment.class);
        mSignUpProgress.setMax(100 * (AppConst.Navigator.Auth.LENGTH - 1));
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        ViewHolderFactory.getInstance().setContext(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public void setCurrentSignUpStep(int step) {
        ValueAnimator animator = ValueAnimator.ofInt(mSignUpProgress.getProgress(), step * 100);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(anim -> mSignUpProgress.setProgress((int) anim.getAnimatedValue()));
        animator.start();
    }

    public void animateApplicationLogo(boolean toSignInFragment) {
        mApplicationLogoHorizontal.setVisibility(View.GONE);

        ViewGroup.LayoutParams lpApplicationLogo = mApplicationLogo.getLayoutParams();
        ViewGroup.LayoutParams lpSignUpLabel = mSignUpLabel.getLayoutParams();
        final int appLogoHeight = lpApplicationLogo.height;
        final int appLogoWidth  = lpApplicationLogo.width;
        final float labelTargetHeight = getResources().getDimension(R.dimen.baseline_x4);

        ValueAnimator animApplicationLogo = toSignInFragment? ValueAnimator.ofFloat(1, 2) : ValueAnimator.ofFloat(1, 0.5f);
        animApplicationLogo.addUpdateListener(
            animation -> {
                lpApplicationLogo.height = (int) (appLogoHeight * (float) animation.getAnimatedValue());
                lpApplicationLogo.width = (int) (appLogoWidth * (float) animation.getAnimatedValue());
                mApplicationLogo.setLayoutParams(lpApplicationLogo);
            });

        ValueAnimator animSignUpLabel = toSignInFragment? ValueAnimator.ofFloat(1, 0) : ValueAnimator.ofFloat(0, 1);
        animSignUpLabel.addUpdateListener(
            animation -> {
                lpSignUpLabel.height = (int) (labelTargetHeight * (float) animation.getAnimatedValue());
                mSignUpLabel.setLayoutParams(lpSignUpLabel);
                mSignUpLabel.setAlpha((float) animation.getAnimatedValue());
            }
        );

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animApplicationLogo, animSignUpLabel);
        animSet.start();
    }

    /* Finishes Activity unless FragmentNavigator handled the event */
    @Override
    public void onBackPressed() {
        if(!mNavigator.back()) this.finish();
    }

    /* Fade-Animated Activity Transition to MainActivity */
    public void startMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        AuthActivity.this.startActivity(intent);
        AuthActivity.this.finish();
    }

    /* Google Analytics */
    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
    @Override
    public void onReportToGoogleAnalytics(String description, String source, boolean fatal) {
        Timber.d("AuthActivity.onReportToGoogleAnalytics from %s\nCause : %s", source, description);
        mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(fatal).build());
    }

    /* Bind FragmentNavigator methods to mNavigator */
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        mNavigator.navigate(target, addToBackStack);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, Navigator.AnimatorType animatorType) {
        mNavigator.navigate(target, addToBackStack, animatorType);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        mNavigator.navigate(target, addToBackStack, clear);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, Navigator.AnimatorType animatorType, boolean clear) {
        mNavigator.navigate(target, addToBackStack, animatorType, clear);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        mNavigator.navigate(target, bundle, addToBackStack);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, Navigator.AnimatorType animatorType) {
        mNavigator.navigate(target, bundle, addToBackStack, animatorType);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        mNavigator.navigate(target, bundle, addToBackStack, clear);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, Navigator.AnimatorType animatorType, boolean clear) {
        mNavigator.navigate(target, bundle, addToBackStack, animatorType, clear);
    }
    @Override
    public String getBackStackNameAt(int index) {
        return mNavigator.getBackStackNameAt(index);
    }
    @Override
    public boolean back() {
        return mNavigator.back();
    }
    @Override
    public void setOnNavigateListener(NavigationCallback listener) {
        mNavigator.setOnNavigateListener(listener);
    }
}