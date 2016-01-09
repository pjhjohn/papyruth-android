package com.papyruth.support.utility.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.papyruth.android.R;

/**
 * Created by pjhjohn on 2015-11-27.
 */
public class AnimatorHelper {
    public static ValueAnimator FADE_IN(View view) {
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

    public static ValueAnimator FADE_OUT(View view) {
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

    public static ValueAnimator FOCUS_EFFECT(View view){
        int currentBackground = view.getResources().getColor(R.color.background_cardview);
        int focusBackground = view.getResources().getColor(R.color.background_focus_cardview);
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), currentBackground, focusBackground, currentBackground);
        animator.setDuration(1500);
        animator.addUpdateListener(animation -> {
            view.setBackgroundColor(((int) animation.getAnimatedValue()));
        });
        return animator;
    }
}
