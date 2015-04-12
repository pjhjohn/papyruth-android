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
    protected ListView listview = null;
    protected List<T> items = null;
    protected UniversalAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Register Event Handlers within this class if exist */
        try {
            if (view instanceof ListView) this.listview = (ListView) view;
            else this.listview = (ListView) view.findViewById(this.getListViewId());
        } catch (ClassCastException e) {
            Log.e("DEBUG", "There is no ListView Element within the Fragemnt.");
            e.printStackTrace();
        }
        if (listview != null)
            this.listview.setAdapter(this.adapter = new UniversalAdapter(this.items = new ArrayList<T>(), this.getActivity().getApplicationContext()));

        /* Return inflated view */
        return view;
    }

    /** Should Implement this to set endpoint of the request for the fragment */
    protected abstract int getListViewId();
}
