package com.papyruth.support.utility.recyclerview;

import android.view.View;

/**
 * Created by pjhjohn on 2015-04-17.
 */
public interface RecyclerViewItemObjectClickListener {
    /**
     * Callback function for item click within RecyclerView
     * @param view view of clicked item
     * @param object item in adapter
     */
    void onRecyclerViewItemObjectClick(View view, Object object);
}