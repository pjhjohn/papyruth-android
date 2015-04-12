package com.montserrat.utils.request;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-12.
 */
public abstract class ClientFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener {
    private RequestQueue queue;
    private View progress = null;
    private View content = null;
    private int progressAnimationTime;
    private boolean isContentVisibleAtProgressState = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.queue = RequestQueue.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(this.getFragmentLayoutId(), container, false);

        this.progress = view.findViewById(this.getProgressViewId());
        this.content  = view.findViewById(this.getContentViewId());
        this.progressAnimationTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);

        return view;
    }

    /** Should Implement this to set endpoint of the request for the fragment */
    protected abstract int getFragmentLayoutId();
    protected abstract String getEndpoint();

    protected int getProgressViewId(){ return 0; }
    protected int getContentViewId (){ return 0; }
    protected void setOnProgressContentsVisibilityFlag(boolean show) { this.isContentVisibleAtProgressState = show; }
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
                            ClientFragment.this.progress.setVisibility(show ? View.VISIBLE : View.GONE);
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
                            ClientFragment.this.content.setVisibility(show? View.GONE : View.VISIBLE);
                        }
                    });
        }
    }

    public void submit(JSONObject jsonToSend) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, this.getEndpoint(), jsonToSend, this, this);
        this.setProgressState(true);
//        TODO : TIMEOUT HANDLING
//        myRequest.setRetryPolicy(new DefaultRetryPolicy(
//                MY_SOCKET_TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        this.queue.addToRequestQueue(request);
    }

    @Override
    public void onResponse (JSONObject response) {
        this.setProgressState(false);
    }

    @Override
    public void onErrorResponse (VolleyError error) {
        this.setProgressState(false);
    }
}