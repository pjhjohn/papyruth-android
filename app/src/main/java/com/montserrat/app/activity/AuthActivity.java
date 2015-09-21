package com.montserrat.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.montserrat.app.R;
import com.montserrat.app.fragment.auth.SplashFragment;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.ViewPagerController;

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
        ViewHolderFactory.getInstance().setContext(this);
    }

    @Override
    public void onBackPressed() {
        boolean backed = false;
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

    /* ViewPagerController Mapper */
    private ViewPagerController mViewPagerController;
    public ViewPagerController getViewPagerController() {
        return mViewPagerController;
    }

    public void setViewPagerController(ViewPagerController mViewPagerController) {
        this.mViewPagerController = mViewPagerController;
    }
}