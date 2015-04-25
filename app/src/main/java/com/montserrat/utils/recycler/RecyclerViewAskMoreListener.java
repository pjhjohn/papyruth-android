package com.montserrat.utils.recycler;

/**
 * Created by pjhjohn on 2015-04-25.
 */
public interface RecyclerViewAskMoreListener {
    public void onAskMoreIfAny(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition);
}
