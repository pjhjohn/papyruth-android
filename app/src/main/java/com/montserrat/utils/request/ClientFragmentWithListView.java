package com.montserrat.utils.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.montserrat.controller.AppConst;
import com.montserrat.utils.adapter.UniversalAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-12.
 */
public abstract class ClientFragmentWithListView<T> extends ClientFragment {
    protected ListView listView;
    protected List<T> items;
    protected UniversalAdapter adapter;
    private int listId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* SuperCall */
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Bind Initialization Data from savedInstanceState */
        if (this.args != null) {
            this.listId = this.args.getInt(AppConst.Resource.LIST, AppConst.Resource.DEFAULT);
        } else {
            this.listId = AppConst.Resource.DEFAULT;
        }

        /* Initialize other member variables */
        this.items = new ArrayList<>();
        this.adapter = new UniversalAdapter(this.items, this.getActivity());

        /* Bind Views */
        this.listView = (ListView) view.findViewById(this.listId);
        if(this.listView != null) this.listView.setAdapter(this.adapter);

        return view;
    }
}