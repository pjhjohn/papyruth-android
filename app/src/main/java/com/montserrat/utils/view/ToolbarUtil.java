package com.montserrat.utils.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-19.
 */
public class ToolbarUtil {
    public static void show(Toolbar toolbar) {
        Timber.d("toolbar show!!");
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

    private static ValueAnimator getColorTransitionAnimatorInner(Toolbar toolbar, int fromColor, int toColor) {
        ValueAnimator animColor = new ValueAnimator();
        animColor.setIntValues(fromColor, toColor);
        animColor.setEvaluator(new ArgbEvaluator());
        animColor.addUpdateListener(animator -> toolbar.setBackgroundColor((int) animator.getAnimatedValue()));
        animColor.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                toolbar.setBackgroundColor(toColor);
                AppManager.getInstance().setMainToolbarColor(toColor);
            }
        });
        return animColor;
    }

    public static ValueAnimator getColorTransitionAnimator(Toolbar toolbar, int fromColor, int toColor, String name){
        if(toolbar == null) return null;
        toolbar.setTitle(name);
        return getColorTransitionAnimatorInner(toolbar, fromColor, toColor);
    }

    public static ValueAnimator getColorTransitionAnimator(Toolbar toolbar, int toColor, String name){
        if(toolbar == null) return null;
        toolbar.setTitle(name);
        return getColorTransitionAnimatorInner(toolbar, AppManager.getInstance().getMainToolbarColor(), toColor);
    }

    public static ValueAnimator getColorTransitionAnimator(Toolbar toolbar, int fromColor, int toColor) {
        if(toolbar == null) return null;
        return getColorTransitionAnimatorInner(toolbar, fromColor, toColor);
    }

    public static ValueAnimator getColorTransitionAnimator(Toolbar toolbar, int toColor) {
        if(toolbar == null) return null;
        return getColorTransitionAnimatorInner(toolbar, AppManager.getInstance().getMainToolbarColor(), toColor);
    }

    public static void registerMenu(Toolbar toolbar, int menuResourceId, Toolbar.OnMenuItemClickListener listener) {
        toolbar.inflateMenu(menuResourceId);
        toolbar.setOnMenuItemClickListener(listener);
        toolbar.setTitleTextColor(Color.WHITE);

    }
}
