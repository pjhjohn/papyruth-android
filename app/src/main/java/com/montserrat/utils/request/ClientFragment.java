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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.montserrat.controller.AppConst;

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-04-12.
 * Abstract Fragment which uses Android Volley to make connection to backend.
 */

public abstract class ClientFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener {
    private RequestQueue queue;
    private View progressView;
    private View contentView;
    private int progressAnimationTime;
    private boolean isContentVisibleDuringProgress;
    private CharSequence requestUrl, requestController, requestAction;
    private int fragmentId, contentId, progressId;
    private JSONObject jsonToRequest;
    protected Bundle args;

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        // TODO : Potential Context Bug when called from two different activities (Not Checked)
        this.queue = RequestQueue.getInstance(activity);
    }

    /**
     * Child Classes MUST IMPLEMENT THIS
     */
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Bind Parameters passed via setArguments(Bundle bundle) */
        this.args = this.getArguments();
        if (args != null) {
            this.requestUrl         = args.getString(AppConst.Request.URL, AppConst.Request.DEFAULT);
            this.requestController  = args.getString(AppConst.Request.CONTROLLER, AppConst.Request.DEFAULT);
            this.requestAction      = args.getString(AppConst.Request.ACTION, AppConst.Request.DEFAULT);
            this.fragmentId         = args.getInt(AppConst.Resource.FRAGMENT, AppConst.Resource.DEFAULT);
            this.contentId          = args.getInt(AppConst.Resource.FRAGMENT, AppConst.Resource.DEFAULT);
            this.progressId         = args.getInt(AppConst.Resource.PROGRESS, AppConst.Resource.DEFAULT);
        } else {
            this.requestUrl         = AppConst.Request.DEFAULT;
            this.requestController  = AppConst.Request.DEFAULT;
            this.requestAction      = AppConst.Request.DEFAULT;
            this.fragmentId         = AppConst.Resource.DEFAULT;
            this.contentId          = AppConst.Resource.DEFAULT;
            this.progressId         = AppConst.Resource.DEFAULT;
        }

        /* Initialize other member variables */
        this.isContentVisibleDuringProgress = true;
        this.jsonToRequest = null;

        /* Bind Views */
        View view = inflater.inflate(this.fragmentId, container, false);

        this.progressView = view.findViewById(this.progressId);
        this.contentView = view.findViewById(this.contentId);
        this.progressAnimationTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);
        return view;
    }

    protected void setContentVisibilityOnProgress (boolean visibleOnProgress) {
        this.isContentVisibleDuringProgress = visibleOnProgress;
    }

    public void setProgressState (final boolean show) {
        if (this.progressView != null) {
            this.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            this.progressView.animate().setDuration(this.progressAnimationTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    ClientFragment.this.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        if (this.contentView != null && !this.isContentVisibleDuringProgress) {
            this.contentView.setVisibility(show ? View.GONE : View.VISIBLE);
            this.contentView.animate().setDuration(this.progressAnimationTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd (Animator animation) {
                    ClientFragment.this.contentView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
    }

    public ClientFragment setUrl (CharSequence url) {
        this.requestUrl = url;
        return this;
    }

    public ClientFragment setController (CharSequence controller) {
        this.requestController = controller;
        return this;
    }

    public ClientFragment setAction (CharSequence action) {
        this.requestAction = action;
        return this;
    }

    /* TODO : There will be a more elegant way to implement this. */
    public String buildEndpoint () {
        if(this.requestUrl.subSequence(0, 7).equals("http://")) this.requestUrl = this.requestUrl.subSequence(7, this.requestUrl.length());
        return String.format("http://%s/%s/%s", this.requestUrl, this.requestController, this.requestAction);
    }

    public ClientFragment setParameters (JSONObject param) {
        this.jsonToRequest = param;
        return this;
    }

    public void submit () {
        if (this.jsonToRequest == null) {
            Log.d("DEBUG", "There is no data to send");
            this.jsonToRequest = new JSONObject(); /* TODO : TEST WHAT HAPPENS IF " this.jsonToRequest==null " */
        }
        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                this.buildEndpoint(),
                this.jsonToRequest,
                this,
                this
        );
        this.setProgressState(true);
//        TODO : TIMEOUT HANDLING, RETRY POLICY
//        myRequest.setRetryPolicy(new DefaultRetryPolicy(
//                MY_SOCKET_TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        this.queue.addToRequestQueue(request);
    }

    /* TODO : Override if necessary */
    @Override
    public void onResponse (JSONObject response) {
        this.setProgressState(false);
    }

    @Override
    public void onErrorResponse (VolleyError error) {
        this.setProgressState(false);
    }
}