package com.montserrat.utils.view.navigator;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by pjhjohn on 2015-06-12.
 */
public interface Navigator {
    enum AnimatorType {
        SLIDE_TO_RIGHT, SLIDE_TO_LEFT, SLIDE_TO_UP, SLIDE_TO_DOWN
    }
    void navigate(Class<? extends Fragment> target, boolean addToBackStack);
    void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType);
    void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear);
    void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType, boolean clear);
    void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack);
    void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType);
    void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear);
    void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType, boolean clear);
    String getBackStackNameAt(int index);
    boolean back();
    void setOnNavigateListener(NavigationCallback listener);
}