package com.montserrat.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.fragment.auth.AuthFragment;
import com.montserrat.app.fragment.auth.SplashFragment;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.NavigationCallback;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import timber.log.Timber;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements Navigator {
    private FragmentNavigator mNavigator;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);
        FloatingActionControl.getInstance().setContainer((FloatingActionControlContainer) this.findViewById(R.id.fac));
        mNavigator = new FragmentNavigator(this.getFragmentManager(), R.id.auth_navigator, SplashFragment.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        ViewHolderFactory.getInstance().setContext(this);
//        this.getKeyboard();
    }

    public void getKeyboard(){
        final View activityRoot = findViewById(R.id.auth_activity_root);
        activityRoot.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRoot.getRootView().getHeight() - activityRoot.getHeight();
            if(heightDiff > 100){
                Timber.d("show keyboard!!!");
//                Fragment authFragament;
//                if((authFragament = this.getFragmentManager().findFragmentById(R.id.auth_navigator)) instanceof AuthFragment){
//                    ((AuthFragment)authFragament).logoScaleAnimation(AppConst.ViewPager.Auth.SIGNUP_STEP1, false);
//                }

            }
        });
    }


    @Override
    public void onBackPressed() {
        boolean backed = false;
        if(this.mViewPagerController.controlTargetContains(mViewPagerController.getCurrentPage())) return;
        if(mViewPagerController != null && mViewPagerController.back()) backed = true;
        if(!backed && this.mNavigator.back()) backed = true;
        if(!backed) this.finish();
    }

    /* Map Navigator methods to this.navigator */
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        this.mNavigator.navigate(target, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, Navigator.AnimatorType animatorType) {
        this.mNavigator.navigate(target, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        this.mNavigator.navigate(target, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, Navigator.AnimatorType animatorType, boolean clear) {
        this.mNavigator.navigate(target, addToBackStack, animatorType, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        this.mNavigator.navigate(target, bundle, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, Navigator.AnimatorType animatorType) {
        this.mNavigator.navigate(target, bundle, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        this.mNavigator.navigate(target, bundle, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, Navigator.AnimatorType animatorType, boolean clear) {
        this.mNavigator.navigate(target, bundle, addToBackStack, animatorType, clear);
    }

    @Override
    public String getBackStackNameAt(int index) {
        return this.mNavigator.getBackStackNameAt(index);
    }

    @Override
    public boolean back() {
        return this.mNavigator.back();
    }

    @Override
    public void setOnNavigateListener(NavigationCallback listener) {
        mNavigator.setOnNavigateListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    /* ViewPagerController Mapper */
    private ViewPagerController mViewPagerController;
    public ViewPagerController getViewPagerController() {
        return mViewPagerController;
    }

    public void setViewPagerController(ViewPagerController mViewPagerController) {
        this.mViewPagerController = mViewPagerController;
    }
}