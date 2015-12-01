package com.papyruth.support.utility.navigator;

import android.app.Fragment;

/**
 * Created by SSS on 2015-09-30.
 */
public interface NavigationCallback {
    void onNavigation(Fragment target);
    void onNavigationChanged();
    void onNavigationBack();
}
