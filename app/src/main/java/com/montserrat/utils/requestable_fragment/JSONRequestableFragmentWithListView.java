package com.montserrat.utils.requestable_fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.montserrat.utils.adapter.UniversalAdapter;
import com.montserrat.utils.request.JSONRequestForm;

import java.util.ArrayList;
import java.util.List;

public abstract class JSONRequestableFragmentWithListView<T> extends JSONRequestableFragment {
    protected JSONRequestForm form = null;
    protected ListView listview = null;
    protected List<T> items = null;
    protected UniversalAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate View */
        View view = inflater.inflate(this.getFragmentLayoutId(), container, false);

        /* Receive Endpoint by Argument & Register JSONRequestForm for this fragment */
        this.form = new JSONRequestForm(this, this.getEndpoint());

        /* Register Event Handlers within this class if exist */
        this.listview = (ListView) view.findViewById(this.getListViewId());
        Log.d("DEBUG", "" + this.listview);
        this.items = new ArrayList<T>();

        /* Return inflated view */
        return view;
    }

    /** Should Implement this to set endpoint of the request for the fragment */
    protected abstract int getFragmentLayoutId();
    protected abstract int getListViewId();
    protected abstract String getEndpoint();

    /** Should Implement JSONRequestForm.OnResponse methods in order to handle responses */
}
