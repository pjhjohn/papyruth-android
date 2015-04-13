package com.montserrat.utils.request;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ClientFragmentWithRecyclerView<T extends RecyclerView.Adapter<RecyclerView.ViewHolder>, E> extends ClientFragment{
    private static final String TAG = "ClientFragmentWithRecyclerView";

    protected Toolbar toolbar = null;
    protected FloatingActionButton fab = null;
    protected T adapter = null;
    protected List<E> items = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.toolbar = (Toolbar) this.getActivity().findViewById(this.getToolbarId());
        if (this.toolbar == null) Log.d(TAG, "Couldn't find ID of toolbar");

        this.fab = (FloatingActionButton) this.getActivity().findViewById(this.getFloatingActionButtonId());
        if (this.fab == null) Log.d(TAG, "Couldn't find ID of FAB");

        this.items = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(this.getRecyclerViewId());
        if (recyclerView == null) throw new RuntimeException("Couldn't find RecyclerView");

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity())); // TODO : has issue of dependency on LinearLayout
        this.adapter = this.getAdapter(this.items);
        recyclerView.setAdapter(this.adapter);

        return view;
    }

    protected abstract T getAdapter (List<E> items);

    protected int getToolbarId() {
        return 0;
    }

    protected int getFloatingActionButtonId() {
        return 0;
    }

    protected abstract int getRecyclerViewId();
}
