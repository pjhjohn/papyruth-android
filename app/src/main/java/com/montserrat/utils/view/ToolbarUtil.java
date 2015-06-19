package com.montserrat.utils.view;

import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by pjhjohn on 2015-06-19.
 */
public class ToolbarUtil {
    public static void show(Toolbar toolbar) {
        if(toolbar == null) return;
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }
    public static void hide(Toolbar toolbar) {
        if(toolbar == null) return;
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
}
