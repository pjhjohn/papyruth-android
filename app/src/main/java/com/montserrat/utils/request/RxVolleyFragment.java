package com.montserrat.utils.request;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.controller.AppConst;

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-05-04.
 * Boilerplate fragment that provides some utility functions for child classes.
 */

public class RxVolleyFragment extends Fragment {
    protected View vProgress, vContent;
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

    protected void showProgress (final boolean show) {
        if (this.vProgress != null) {
            this.vProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            this.vProgress.animate().setDuration(this.transitionTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    RxVolleyFragment.this.vProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        if (this.vContent != null && !this.contentVisibleWhenProgress) {
            this.vContent.setVisibility(show ? View.GONE : View.VISIBLE);
            this.vContent.animate().setDuration(this.transitionTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    RxVolleyFragment.this.vContent.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
    }
}