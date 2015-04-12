package com.montserrat.utils.request;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class JSONRequestableFragment extends Fragment implements JSONRequestForm.OnRequest, JSONRequestForm.OnResponse {
    protected JSONRequestForm form = null;
    protected View progress = null;
    protected View content = null;
    private int progressAnimationTime;
    private boolean isContentVisibleAtProgressState = true;

    /**
     * Should call super.onCreateView(~) from the child classes of this.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate View */
        View view = inflater.inflate(this.getFragmentLayoutId(), container, false);

        /* Receive Endpoint by Argument & Register JSONRequestForm for this fragment */
        this.form = new JSONRequestForm(this, this, this.getEndpoint());

        /* Register View Components for Handling Progress */
        this.progress = view.findViewById(this.getProgressViewId());
        this.content = view.findViewById(this.getContentViewId());
        this.progressAnimationTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);

        /* Return inflated view */
        return view;
    }

    /** Should Implement this to set endpoint of the request for the fragment */
    protected abstract int getFragmentLayoutId();
    protected abstract String getEndpoint();

    /* Handling ProgressView */
    /** 0 is not a valid resource id : http://developer.android.com/reference/android/content/res/Resources.html#getIdentifier%28java.lang.String,%20java.lang.String,%20java.lang.String%29 */
    protected int getProgressViewId() {
        return 0;
    }
    protected int getContentViewId() {
        return 0;
    }
    protected void setOnProgressContentsVisibilityFlag(boolean show) {
        this.isContentVisibleAtProgressState = show;
    }

    public void setProgressState(final boolean show) {
        if(this.progress != null) {
            this.progress.setVisibility(show ? View.VISIBLE : View.GONE);
            this.progress
                    .animate()
                    .setDuration(progressAnimationTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd (Animator animation) {
                            JSONRequestableFragment.this.progress.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
        }
        if(this.content != null && (!this.isContentVisibleAtProgressState)) {
            this.content.setVisibility(show? View.GONE : View.VISIBLE);
            this.content
                    .animate()
                    .setDuration(progressAnimationTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd (Animator animation) {
                            JSONRequestableFragment.this.content.setVisibility(show? View.GONE : View.VISIBLE);
                        }
                    });
        }
    }

    /* JSONRequestForm.OnRequest */
    public void onRequest() {
        this.setProgressState(true);
    }

    /* JSONRequestForm.OnResponse */
    @Override
    public void onSuccess(String response) {
        this.setProgressState(false);
    }
    @Override
    public void onTimeout(String errorMsg) {
        this.setProgressState(false);
    }
    @Override
    public void onNoInternetConnection(String errorMsg) {
        this.setProgressState(false);
    }
    // TODO : 백버튼 연동
    @Override
    public void onCanceled() {
        this.setProgressState(false);
    }
}