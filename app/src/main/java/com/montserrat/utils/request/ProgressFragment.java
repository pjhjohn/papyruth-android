package com.montserrat.utils.request;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.view.View;

/**
 * Created by pjhjohn on 2015-05-04.
 * Boilerplate fragment that provides some utility functions for child classes.
 */

public class ProgressFragment extends Fragment {
    private int transitionTime;
    private boolean contentVisibleWhenProgress;

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        // TODO : Potential Context Bug when called from two different activities (Not Checked)
        this.transitionTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);
        this.contentVisibleWhenProgress = true;
    }

    protected void setContentVisibilityOnProgress (boolean visibleOnProgress) {
        this.contentVisibleWhenProgress = visibleOnProgress;
    }

    protected void showProgress (final View progress, final boolean show) {
        this.showProgress(progress, null, show);
    }
    protected void showProgress (final View progress, final View contents, final boolean show) {
        if (progress != null) {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(this.transitionTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        if (contents != null && !this.contentVisibleWhenProgress) {
            contents.setVisibility(show ? View.GONE : View.VISIBLE);
            contents.animate().setDuration(this.transitionTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    contents.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
    }
}