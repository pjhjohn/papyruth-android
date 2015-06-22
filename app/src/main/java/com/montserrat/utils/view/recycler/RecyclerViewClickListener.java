package com.montserrat.utils.view.recycler;

import android.view.View;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public interface RecyclerViewClickListener {
    /**
     * Callback function for item click within RecyclerView
     * @param view view of clicked item
     * @param position position in adapter
     */
    void onRecyclerViewItemClick(View view, int position);
}