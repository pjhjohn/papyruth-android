package com.papyruth.support.utility.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by pjhjohn on 2015-12-05.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class StatusBarHelper {
    public static void changeColorTo(Activity activity, int toColorResourceId) {
        if(activity == null || activity.getWindow() == null) return;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        StatusBarHelper.getColorTransitionAnimatorInner(activity.getWindow(), activity.getWindow().getStatusBarColor(), activity.getResources().getColor(toColorResourceId)).start();
    }

    private static ValueAnimator getColorTransitionAnimatorInner(Window window, int fromColor, int toColor) {
        ValueAnimator animColor = new ValueAnimator();
        animColor.setIntValues(fromColor, toColor);
        animColor.setEvaluator(new ArgbEvaluator());
        animColor.addUpdateListener(animator -> {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor((int) animator.getAnimatedValue());
        });
        animColor.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(toColor);
            }
        });
        return animColor;
    }
}
