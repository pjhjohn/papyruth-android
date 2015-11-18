package com.papyruth.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.R;
import com.papyruth.android.fragment.auth.LoadingFragment;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.support.error.ErrorHandlerCallback;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.view.FloatingActionControlContainer;
import com.papyruth.utils.view.navigator.FragmentNavigator;
import com.papyruth.utils.view.navigator.NavigationCallback;
import com.papyruth.utils.view.navigator.Navigator;
import com.papyruth.utils.view.softkeyboard.SoftKeyboardActivity;
import com.papyruth.utils.view.viewpager.ViewPagerController;

import timber.log.Timber;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends SoftKeyboardActivity implements Navigator, ErrorHandlerCallback {
    private FragmentNavigator mNavigator;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = ((papyruth) getApplication()).getTracker();

        this.setContentView(R.layout.activity_auth);
        this.attachSoftKeyboardListeners();
        FloatingActionControl.getInstance().setContainer((FloatingActionControlContainer) this.findViewById(R.id.fac));
        mNavigator = new FragmentNavigator(this.getFragmentManager(), R.id.auth_navigator, LoadingFragment.class);
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

    @Override
    public void onBackPressed() {
        boolean backed = false;
        if(mViewPagerController.controlTargetContains(mViewPagerController.getCurrentPage())) return;
        if(mViewPagerController != null && mViewPagerController.back()) backed = true;
        if(!backed && mNavigator.back()) backed = true;
        if(!backed) this.finish();
    }

    /* Map Navigator methods to mNavigator */
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

    /* ViewPagerController Mapper */
    private ViewPagerController mViewPagerController;
    public ViewPagerController getViewPagerController() {
        return mViewPagerController;
    }

    public void setViewPagerController(ViewPagerController viewPagerController) {
        mViewPagerController = viewPagerController;
    }

    /* Fade-Animated Activity Transition to MainActivity */
    public void startMainActivity() {
        Intent mainIntent = new Intent(AuthActivity.this, MainActivity.class);
        AuthActivity.this.startActivity(mainIntent);
        AuthActivity.this.finish();
        AuthActivity.this.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

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