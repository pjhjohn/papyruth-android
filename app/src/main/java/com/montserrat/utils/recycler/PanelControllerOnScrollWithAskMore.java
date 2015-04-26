package com.montserrat.utils.recycler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by pjhjohn on 2015-04-25.
 */
public abstract class PanelControllerOnScrollWithAskMore extends PanelControllerOnScroll {
    private static final int ITEM_LEFT_TO_LOAD_MORE = 10;

    private boolean isRequestPending = false;

    public void setIsRequestPending(boolean isPending) {
        this.isRequestPending = isPending;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        /* Retrieve Visible Item Count & Positions */
        final RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        final int nItemVisible = manager.getChildCount();
        final int nItemTotal = manager.getItemCount();
        int iItemVisibleLast  = -1;
        /* Only Accepts Linear&Grid LayoutManager, not STAGGERED_GRID. GridLayoutManager is subclass of LinearLayoutManager */
        if (manager instanceof LinearLayoutManager) {
            iItemVisibleLast  = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager");

        /* FOR ASK_MORE */
        if (((nItemTotal - iItemVisibleLast) <= ITEM_LEFT_TO_LOAD_MORE || (nItemTotal - iItemVisibleLast) == 0 && nItemTotal > nItemVisible) && !isRequestPending) {
            onAskMore(recyclerView.getAdapter().getItemCount(), ITEM_LEFT_TO_LOAD_MORE, iItemVisibleLast);
        }
    }

    public abstract void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition);
}
