package com.papyruth.support.utility.recyclerview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by pjhjohn on 2015-04-25.
 */
public abstract class PanelControllerOnScrollWithAskMore extends PanelControllerOnScroll implements RecyclerViewAskMoreListener {
    /* ItemCount to the end of current list. Triggers onAskMore when the count is lower than following constant. */
    private int numOfItemsLeftToAskMore;
    public PanelControllerOnScrollWithAskMore(int numOfItemsLeftToAskMore) {
        this.numOfItemsLeftToAskMore = numOfItemsLeftToAskMore;
    }

    private boolean isRequestPending = false;
    public void setIsRequestPending(boolean isPending) {
        this.isRequestPending = isPending;
    }
    public void setNumOfItemsLeftToAskMore (int numOfItemsLeftToAskMore) {
        this.numOfItemsLeftToAskMore = numOfItemsLeftToAskMore;
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
        if (!isRequestPending && ((nItemTotal - iItemVisibleLast) <= numOfItemsLeftToAskMore || (nItemTotal - iItemVisibleLast) == 0 && nItemTotal > nItemVisible )) {
            onAskMore(recyclerView.getAdapter().getItemCount(), numOfItemsLeftToAskMore, iItemVisibleLast);
        }
    }
}
