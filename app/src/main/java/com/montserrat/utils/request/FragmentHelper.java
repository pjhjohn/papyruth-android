package com.montserrat.utils.request;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.view.View;

/**
 * Created by pjhjohn on 2015-05-08.
 * Provides helper functions for fragment.
 */

@Deprecated
public class FragmentHelper {

    public static void showProgress (final View progress, final boolean show) {
        FragmentHelper.showProgress(progress, null, show, 200/*config_shortAnimTime*/);
    }

    public static void showProgress (final View progress, final View contents, final boolean show, int duration) {
        if (progress != null) {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(duration).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        if (contents != null) {
            contents.setVisibility(show ? View.GONE : View.VISIBLE);
            contents.animate().setDuration(duration).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    contents.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
    }
}