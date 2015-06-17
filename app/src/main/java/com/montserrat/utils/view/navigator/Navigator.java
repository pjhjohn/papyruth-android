package com.montserrat.utils.view.navigator;

import android.app.Fragment;

/**
 * Created by pjhjohn on 2015-06-12.
 */
public interface Navigator {
    void navigate(Class<? extends Fragment> target, boolean addToBackStack);
    void navigate(Class<? extends Fragment> target, boolean addToBackStack, FragmentNavigator.AnimatorType animatorType);
    void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear);
    void navigate(Class<? extends Fragment> target, boolean addToBackStack, FragmentNavigator.AnimatorType animatorType, boolean clear);
    String getBackStackNameAt(int index);
    boolean back();
}