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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.montserrat.controller.AppConst;

import org.json.JSONObject;

import java.util.InvalidPropertiesFormatException;

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
    private int requestMethod;
    private String requestUrl, requestController, requestAction;
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
            this.requestMethod      = args.getInt(AppConst.Request.METHOD, AppConst.Request.Method.GET);
            this.requestUrl         = args.getString(AppConst.Request.URL, AppConst.Request.DEFAULT);
            this.requestController  = args.getString(AppConst.Request.CONTROLLER, AppConst.Request.DEFAULT);
            this.requestAction      = args.getString(AppConst.Request.ACTION, AppConst.Request.DEFAULT);
            this.fragmentId         = args.getInt(AppConst.Resource.FRAGMENT, AppConst.Resource.DEFAULT);
            this.contentId          = args.getInt(AppConst.Resource.FRAGMENT, AppConst.Resource.DEFAULT);
            this.progressId         = args.getInt(AppConst.Resource.PROGRESS, AppConst.Resource.DEFAULT);
        } else {
            this.requestMethod      = AppConst.Request.Method.GET;
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
        if (view == null) Log.d("ClientFragment", "Couldn't inflate Fragment by ID#"+this.fragmentId);

        this.progressView = view.findViewById(this.progressId);
        if (progressView == null) Log.d("ClientFragment", "Couldn't find progressView by ID#"+this.progressId);
        this.contentView = view.findViewById(this.contentId);
        if (contentView == null) Log.d("ClientFragment", "Couldn't find contentView by ID#"+this.contentId);
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

    public ClientFragment setUrl (String url) throws InvalidPropertiesFormatException {
        if (url.split("://").length > 2) throw new InvalidPropertiesFormatException("Invalid URL format. Should in form of (http(s)://)root_url_path:port, where () is optional.");
        this.requestUrl = url;
        return this;
    }

    public ClientFragment setController (String controller) throws InvalidPropertiesFormatException  {
        if (controller.split("/").length != 1) throw new InvalidPropertiesFormatException("Controller field does not accept '/' character.");
        this.requestController = controller;
        return this;
    }

    public ClientFragment setAction (String action) {
        this.requestAction = action;
        return this;
    }

    /* TODO : There will be a more elegant way to implement this. */
    public String getRequestEndpoint ()  {
        if (this.requestUrl.split("://").length == 1) this.requestUrl = "http://" + this.requestUrl; // Default is http, not https.
        return String.format("%s%s%s",
                this.requestUrl,
                this.requestController.isEmpty() ? "" : "/" + this.requestController,
                this.requestAction.isEmpty()? "" : "/" + this.requestAction
        );
    }

    public ClientFragment setParameters (JSONObject param) {
        this.jsonToRequest = param;
        return this;
    }

    public void submit () {
        final JsonObjectRequest request = new JsonObjectRequest(
                this.requestMethod,
                this.getRequestEndpoint(),
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
        Log.d("DEBUG", String.format("%s REQUEST to %s with following data\n%s\n",
                this.requestMethod == AppConst.Request.Method.GET? "GET" : "POST",
                this.getRequestEndpoint(),
                this.jsonToRequest
        ));
        this.queue.addToRequestQueue(request);
    }

    /* TODO : Override if necessary */
    @Override
    public void onResponse (JSONObject response) {
        Log.d("DEBUG", "Response\n" + response.toString());
        this.setProgressState(false);
    }

    @Override
    public void onErrorResponse (VolleyError error) {
        Log.d("DEBUG", "Error\n" + error.toString());
        this.setProgressState(false);
    }
}