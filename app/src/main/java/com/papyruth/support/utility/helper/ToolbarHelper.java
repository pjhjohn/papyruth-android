package com.papyruth.support.utility.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.papyruth.android.R;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-19.
 */
public class ToolbarHelper {
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
        ValueAnimator animTop = ValueAnimator.ofInt(-MetricHelper.getPixels(toolbar.getContext(), R.attr.actionBarSize), 0);
        animTop.setInterpolator(new DecelerateInterpolator(2));
        animTop.addUpdateListener(animator -> toolbar.setY((int) animator.getAnimatedValue()));
        return animTop;
    }
    public static ValueAnimator getHideAnimator(Toolbar toolbar) {
        if(toolbar == null) return null;
        ValueAnimator animTop = ValueAnimator.ofInt(0, -MetricHelper.getPixels(toolbar.getContext(), R.attr.actionBarSize));
        animTop.setInterpolator(new AccelerateInterpolator(2));
        animTop.addUpdateListener(animator -> toolbar.setY((int) animator.getAnimatedValue()));
        return animTop;
    }

    public static void registerMenu(Toolbar toolbar, int menuResourceId, Toolbar.OnMenuItemClickListener listener) {
        toolbar.inflateMenu(menuResourceId);
        toolbar.setTitleTextColor(Color.WHITE);
        if(listener != null) toolbar.setOnMenuItemClickListener(listener);
    }

    public static MenuItem menuItemVisibility(Toolbar toolbar, int menuItemResourseId, boolean visibility){
        MenuItem menuItem = toolbar.getMenu().findItem(menuItemResourseId);
        if(menuItem != null)
            menuItem.setVisible(visibility);
        else
            Timber.d("This toolbar(%s) has not this item %s", toolbar.toString(), menuItemResourseId);
        return menuItem;
    }

    public static void menuItemColor(Toolbar toolbar, int menuItemResourseId, int color){
        MenuItem menuItem = toolbar.getMenu().findItem(menuItemResourseId);
        if(menuItem != null) {
            Drawable iconDrawable = menuItem.getIcon();
            iconDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }else{
            Timber.d("This toolbar(%s) has not this item %s", toolbar.toString(), menuItemResourseId);
        }
    }

    public static ValueAnimator getColorTransitionAnimator(Toolbar toolbar, int toColorResourceId) {
        if(toolbar == null) return null;
        return getColorTransitionAnimatorInner(toolbar, ((ColorDrawable) toolbar.getBackground()).getColor(), toolbar.getContext().getResources().getColor(toColorResourceId));
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
            }
        });
        return animColor;
    }
}
