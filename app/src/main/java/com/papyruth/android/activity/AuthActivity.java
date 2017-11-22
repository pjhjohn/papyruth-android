package com.papyruth.android.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.papyruth.android.AppConst;
import com.papyruth.android.BuildConfig;
import com.papyruth.android.R;
import com.papyruth.android.fragment.auth.SignInFragment;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.customview.FloatingActionControlContainer;
import com.papyruth.support.utility.error.Error;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.navigator.FragmentNavigator;
import com.papyruth.support.utility.navigator.NavigationCallback;
import com.papyruth.support.utility.navigator.Navigator;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements com.papyruth.support.utility.navigator.Navigator, Error.OnReportToGoogleAnalytics {
    @BindView(R.id.fac)                       protected FloatingActionControlContainer mFloatingActionControlContainer;
    @BindView(R.id.auth_app_logo_horizontal)  protected ImageView mApplicationLogoHorizontal;
    @BindView(R.id.auth_signup_progress)      protected ProgressBar mSignUpProgress;
    @BindView(R.id.auth_signup_label)         protected TextView mSignUpLabel;
    private FragmentNavigator mNavigator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);
        Crashlytics.setBool(getResources().getString(R.string.crashlytics_key_debug_mode), BuildConfig.DEBUG);
        ButterKnife.bind(this);
        FloatingActionControl.getInstance().setContainer(mFloatingActionControlContainer);
        mNavigator = new FragmentNavigator(this.getFragmentManager(), R.id.auth_navigator, SignInFragment.class);
        mSignUpProgress.setMax(100 * (AppConst.Navigator.Auth.LENGTH - 1));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        ViewHolderFactory.getInstance().setContext(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public void setCurrentAuthStep(int step) {
        ValueAnimator animator = ValueAnimator.ofInt(mSignUpProgress.getProgress(), step * 100);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(anim -> mSignUpProgress.setProgress((int) anim.getAnimatedValue()));
        animator.start();
        if(step <= AppConst.Navigator.Auth.SIGNIN) {
            AnimatorHelper.FADE_OUT(mSignUpLabel).start();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            AnimatorHelper.FADE_IN(mSignUpLabel).start();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
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
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    public void onReportToGoogleAnalytics(String description, String source, boolean fatal) {
        Timber.d("AuthActivity.onReportToGoogleAnalytics from %s\nCause : %s", source, description);
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
