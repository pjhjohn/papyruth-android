package com.montserrat.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public interface Adaptable {
    public View buildView(View view, LayoutInflater inflater, ViewGroup parent);
}
