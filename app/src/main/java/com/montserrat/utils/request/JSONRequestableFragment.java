package com.montserrat.utils.request;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.montserrat.activity.R;

public abstract class JSONRequestableFragment extends Fragment implements JSONRequestForm.OnResponse{
    protected static final String ARG_API_ENDPOINT = "api_endpoint";
    protected JSONRequestForm form = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate View */
        /* TODO : Dynamic Construction using layout xml id parameter */
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        /* Receive Endpoint by Argument & Register JSONRequestForm for this fragment */
        Toast.makeText(this.getActivity(), this.getEndpoint(), Toast.LENGTH_LONG).show();
        this.form = new JSONRequestForm(this, this.getEndpoint());

        /* Register Event Handlers within this class */

        /* Return generated view */
        return view;
    }

    /** Should Implement this to set endpoint of the request for the fragment */
    protected abstract String getEndpoint();

    /** Should Implement JSONRequestForm.OnResponse methods in order to handle responses */
}