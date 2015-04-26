package com.montserrat.utils.recycler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class PanelControllerOnScroll extends RecyclerView.OnScrollListener {
    private static final int HIDE_THRESHOLD = 20;

    private int scrolledDistance = 0;
    private boolean isPanelsVisible = true;


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int firstVisibleItem = -1;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        } else throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager");

        if (firstVisibleItem == 0) {
            if(!this.isPanelsVisible) {
                onShowPanels();
                this.isPanelsVisible = true;
            }
        } else {
            if (this.scrolledDistance > HIDE_THRESHOLD && this.isPanelsVisible) {
                onHidePanels();
                this.isPanelsVisible = false;
                this.scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !this.isPanelsVisible) {
                onShowPanels();
                this.isPanelsVisible = true;
                this.scrolledDistance = 0;
            }
        }
        if((this.isPanelsVisible && dy>0) || (!this.isPanelsVisible && dy<0)) {
            this.scrolledDistance += dy;
        }
    }

    public abstract void onHidePanels ();
    public abstract void onShowPanels ();
}
