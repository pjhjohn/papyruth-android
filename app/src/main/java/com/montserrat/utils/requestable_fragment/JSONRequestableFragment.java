package com.montserrat.utils.requestable_fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.utils.request.JSONRequestForm;

public abstract class JSONRequestableFragment extends Fragment implements JSONRequestForm.OnResponse {
    protected JSONRequestForm form = null;

    /**
     * Should call super.onCreateView(~) from the child classes of this.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate View */
        View view = inflater.inflate(this.getFragmentLayoutId(), container, false);

        /* Receive Endpoint by Argument & Register JSONRequestForm for this fragment */
        this.form = new JSONRequestForm(this, this.getEndpoint());

        /* Register Event Handlers within this class if exist */

        /* Return inflated view */
        return view;
    }

    /** Should Implement this to set endpoint of the request for the fragment */
    protected abstract int getFragmentLayoutId();
    protected abstract String getEndpoint();

    /** Should Implement JSONRequestForm.OnResponse methods in order to handle responses */
}