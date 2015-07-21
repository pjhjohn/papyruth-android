package com.montserrat.utils.view.navigator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.viewpager.OnBack;

import timber.log.Timber;


/**
 * Created by pjhjohn on 2015-06-10.
 */
public class FragmentNavigator implements Navigator {
    int containerViewId;
    FragmentManager manager;
    MaterialMenuDrawable materialMenuDrawable = null;
    MaterialMenuDrawable.IconState materialMenuDrawableState = null;
    NavigationDrawerFragment navigationDrawer = null;

    public FragmentNavigator(NavigationDrawerFragment drawer, FragmentManager manager, int containerViewId, Class<? extends Fragment> initialFragment, MaterialMenuDrawable materialMenuDrawable, MaterialMenuDrawable.IconState initialMaterialMenuDrawableState) {
        this(manager, containerViewId, initialFragment);
        this.materialMenuDrawable = materialMenuDrawable;
        this.materialMenuDrawableState = initialMaterialMenuDrawableState;
        this.materialMenuDrawable.setIconState(initialMaterialMenuDrawableState);
        this.navigationDrawer = drawer;
    }
    public FragmentNavigator(FragmentManager manager, int containerViewId, Class<? extends Fragment> initialFragment) {
        this.containerViewId = containerViewId;
        this.manager = manager;

        Fragment fragment = this.instantiateFragment(initialFragment);
        this.manager.beginTransaction()
            .add(this.containerViewId, fragment)
            .addToBackStack(fragment.getClass().getSimpleName())
            .commit();
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        final Fragment current = this.manager.findFragmentById(this.containerViewId);
        Fragment next = this.instantiateFragment(target);
        if(bundle != null) next.setArguments(bundle);
        if(current != null && current.getClass().getSimpleName().equals(target.getSimpleName())) return;
        if(clear) {
            this.manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if(materialMenuDrawable != null) materialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            if(navigationDrawer != null) navigationDrawer.setOnNavigationIconClickListener(null);
        } else {
            if(materialMenuDrawable != null) materialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.ARROW);
            if(navigationDrawer != null) navigationDrawer.setOnNavigationIconClickListener(view -> navigationDrawer.getActivity().onBackPressed());
        }
        FragmentTransaction transaction = this.setCustomAnimator(this.manager.beginTransaction(), animatorType);
        if(addToBackStack) transaction.addToBackStack(next.getClass().getSimpleName());
        transaction.replace(this.containerViewId, next, AppConst.Tag.ACTIVE_FRAGMENT).commit();
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        this.navigate(target, null, addToBackStack, animatorType, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        this.navigate(target, bundle, addToBackStack, AnimatorType.SLIDE_TO_RIGHT, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        this.navigate(target, addToBackStack, AnimatorType.SLIDE_TO_RIGHT, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType) {
        this.navigate(target, bundle, addToBackStack, animatorType, false);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType) {
        this.navigate(target, null, addToBackStack, animatorType, false);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        this.navigate(target, bundle, addToBackStack, AnimatorType.SLIDE_TO_RIGHT, false);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        this.navigate(target, null, addToBackStack, AnimatorType.SLIDE_TO_RIGHT, false);
    }

    @Override
    public String getBackStackNameAt(int index) {
        if(this.manager.getBackStackEntryCount() < index + 1) return null;
        return this.manager.getBackStackEntryAt(this.manager.getBackStackEntryCount() - index - 1).getName();
    }

    @Override
    public boolean back() {
        final Fragment current = this.manager.findFragmentById(this.containerViewId);
        if (current != null) {
            boolean backed = false;
            if(FloatingActionControl.getMenu() != null && FloatingActionControl.getMenu().isOpened()) {
                FloatingActionControl.getMenu().close(true);
                backed = true;
            }
            if(!backed && current instanceof OnBack) backed = ((OnBack) current).onBack();
            if(!backed) {
                if(this.manager.getBackStackEntryCount() > 2) {
                    this.manager.popBackStack();
                    backed = true;
                } else if(this.manager.getBackStackEntryCount() == 2) {
                    this.manager.popBackStack();
                    if(materialMenuDrawable != null) materialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.BURGER);
                    if(navigationDrawer!= null) navigationDrawer.setOnNavigationIconClickListener(null);
                    backed = true;
                } if(this.manager.getBackStackEntryCount() == 1){
                    if(materialMenuDrawable != null) materialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.BURGER);
                    if(navigationDrawer!= null) navigationDrawer.setOnNavigationIconClickListener(null);
                    backed = false;
                }
            } return backed;
        } return false;
    }

    private FragmentTransaction setCustomAnimator(FragmentTransaction transaction, AnimatorType animatorType) {
        switch(animatorType) {
            case SLIDE_TO_RIGHT : transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right); break;
            case SLIDE_TO_LEFT  : transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left); break;
            case SLIDE_TO_DOWN  : transaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_up, R.anim.slide_in_up, R.anim.slide_out_down); break;
            case SLIDE_TO_UP    : transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_down, R.anim.slide_out_up); break;
        } return transaction;
    }

    private Fragment instantiateFragment(Class<? extends Fragment> clazz) {
        Fragment instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } return instance;
    }
}
