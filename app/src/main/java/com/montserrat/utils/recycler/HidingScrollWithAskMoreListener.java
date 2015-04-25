package com.montserrat.utils.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class HidingScrollWithAskMoreListener extends RecyclerView.OnScrollListener {
    private static final int HIDE_THRESHOLD = 20;
    private static final int ITEM_LEFT_TO_LOAD_MORE = 10;

    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;
    private boolean isPending = false;

    public void setIsRequestPending(boolean isPending) {
        this.isPending = isPending;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        int visibleItemCount = manager.getChildCount();
        int totalItemCount = manager.getItemCount();
        int firstVisibleItemPosition = -1;
        int lastVisibleItemPosition = -1;
        /* ONLY LINEAR & GRID (NOT staggered_grid) */
        if (manager instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
            lastVisibleItemPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if(manager instanceof GridLayoutManager) {
            firstVisibleItemPosition = ((GridLayoutManager) manager).findFirstVisibleItemPosition();
            lastVisibleItemPosition = ((GridLayoutManager) manager).findLastVisibleItemPosition();
        } else throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager");

        /* FOR ASK_MORE */
        if (((totalItemCount - lastVisibleItemPosition) <= ITEM_LEFT_TO_LOAD_MORE || (totalItemCount - lastVisibleItemPosition) == 0 && totalItemCount > visibleItemCount) && !isPending) {

//            mMoreProgress.setVisibility(View.VISIBLE);
            onAskMore(recyclerView.getAdapter().getItemCount(), ITEM_LEFT_TO_LOAD_MORE, lastVisibleItemPosition);
        }

        /* FOR HIDE/SHOW TOOLBAR */

        if (firstVisibleItemPosition == 0) {
            if(!mControlsVisible) {
                onShow();
                mControlsVisible = true;
            }
        } else {
            if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                onHide();
                mControlsVisible = false;
                mScrolledDistance = 0;
            } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                onShow();
                mControlsVisible = true;
                mScrolledDistance = 0;
            }
        }
        if((mControlsVisible && dy>0) || (!mControlsVisible && dy<0)) {
            mScrolledDistance += dy;
        }
    }

    public abstract void onAskMore(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition);
    public abstract void onHide();
    public abstract void onShow();
}
