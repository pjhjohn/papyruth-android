package com.montserrat.utils.view;

import android.animation.ValueAnimator;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;

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

    public static ValueAnimator getShowAnimator(Toolbar toolbar) {
        if(toolbar == null) return null;
        ValueAnimator animTop = ValueAnimator.ofInt(-MetricUtil.getPixels(toolbar.getContext(), R.attr.actionBarSize), 0);
        animTop.setInterpolator(new DecelerateInterpolator(2));
        animTop.addUpdateListener(animator -> toolbar.setY((int) animator.getAnimatedValue()));
        return animTop;
    }
    public static ValueAnimator getHideAnimator(Toolbar toolbar) {
        if(toolbar == null) return null;
        ValueAnimator animTop = ValueAnimator.ofInt(0, -MetricUtil.getPixels(toolbar.getContext(), R.attr.actionBarSize));
        animTop.setInterpolator(new AccelerateInterpolator(2));
        animTop.addUpdateListener(animator -> toolbar.setY((int) animator.getAnimatedValue()));
        return animTop;
    }
}
