package com.papyruth.android.recyclerview.adapter;

import android.app.Fragment;
import android.support.v7.widget.RecyclerView;

/**
 * Created by SSS on 2016-01-07.
 */
public abstract class TrackerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Fragment fragment;
    public void setFragment(Fragment fragment){
        this.fragment = fragment;
    }
    public Fragment getFragment(){
        return fragment;
    }
}
