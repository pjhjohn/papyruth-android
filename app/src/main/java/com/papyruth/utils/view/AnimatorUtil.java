package com.papyruth.utils.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by pjhjohn on 2015-11-27.
 */
public class AnimatorUtil {
    public static Animator FADE_IN(View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(view.getAlpha(), 1);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(anim -> view.setAlpha((float) anim.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                view.setAlpha(0);
                view.setVisibility(View.GONE);
                // may add animation to disappear
            }
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setAlpha(0);
                view.setVisibility(View.VISIBLE);
            }
        });
        return animator;
    }

    public static Animator FADE_OUT(View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(view.getAlpha(), 0);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(anim -> view.setAlpha((float) anim.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                this.onAnimationEnd(animation);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationCancel(animation);
                view.setAlpha(0);
                view.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setAlpha(1);
                view.setVisibility(View.VISIBLE);
            }
        });
        return animator;
    }
}
