package com.montserrat.utils.view.navigator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.montserrat.app.R;

/**
 * Created by pjhjohn on 2015-06-10.
 */
public class FragmentNavigator implements Navigator {
    int containerViewId;
    FragmentManager manager;
    public enum AnimatorType {
        SLIDE_TO_RIGHT, SLIDE_TO_LEFT, SLIDE_TO_UP, SLIDE_TO_DOWN
    }

    public FragmentNavigator(int containerViewId, FragmentManager manager, Class<? extends Fragment> initialFragment) {
        this.containerViewId = containerViewId;
        this.manager = manager;

        Fragment fragment = this.instantiateFragment(initialFragment);
        this.manager.beginTransaction()
            .add(this.containerViewId, fragment)
            .addToBackStack(fragment.getClass().getName())
            .commit();
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        if(clear) this.manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment fragment = this.instantiateFragment(target);
        FragmentTransaction transaction = this.setCustomAnimator(this.manager.beginTransaction(), animatorType);
        if(addToBackStack) transaction.addToBackStack(fragment.getClass().getName());
        transaction.replace(this.containerViewId, fragment);
        transaction.commit();
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        this.navigate(target, addToBackStack, AnimatorType.SLIDE_TO_RIGHT, false);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType) {
        this.navigate(target, addToBackStack, animatorType, false);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        this.navigate(target, addToBackStack, AnimatorType.SLIDE_TO_RIGHT, false);
    }

    @Override
    public String getBackStackNameAt(int index) {
        if(this.manager.getBackStackEntryCount() <= index + 1) return null;
        return this.manager.getBackStackEntryAt(this.manager.getBackStackEntryCount() - index).getName();
    }

    @Override
    public boolean back() {
        if(this.manager.getBackStackEntryCount() <= 1) return false; // 1 for top fragment
        this.manager.popBackStack();
        return true;
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
