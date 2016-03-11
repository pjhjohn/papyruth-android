package com.papyruth.support.utility.recyclerview;

import android.view.View;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public interface RecyclerViewItemObjectLongClickListener {
    /**
     * Callback function for item click within RecyclerView
     * @param view view of clicked item
     * @param object object in adapter
     */
    void onRecyclerViewItemObjectLongClick(View view, Object object);
}