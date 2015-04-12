package com.montserrat.utils.request;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.montserrat.utils.adapter.UniversalAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-12.
 */
public abstract class ClientFragmentWithListView<T> extends ClientFragment {
    protected ListView listview = null;
    protected List<T> items = null;
    protected UniversalAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* TODO : Experiment needed whether findViewById catches itself. */
        try {
            this.listview = (ListView) view.findViewById(this.getListViewId());
        } catch (ClassCastException e) {
            Log.e("DEBUG", "There is no listview element within the fragment.");
            e.printStackTrace();
        }
        if (listview != null) this.listview.setAdapter(
                this.adapter = new UniversalAdapter(
                        this.items = new ArrayList<T>(),
                        this.getActivity()
                )
        );

        return view;
    }

    protected abstract int getListViewId();
}