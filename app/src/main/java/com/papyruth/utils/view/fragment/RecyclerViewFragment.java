package com.papyruth.utils.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.view.MetricUtil;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.recycler.PanelControllerOnScrollWithAskMore;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class RecyclerViewFragment<ADAPTER extends RecyclerView.Adapter<RecyclerView.ViewHolder>, ITEM> extends Fragment implements RecyclerViewItemClickListener {

    protected ADAPTER adapter;
    protected List<ITEM> items;
    protected boolean hideToolbarOnScroll, hideFloatingActionButtonOnScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideToolbarOnScroll = true;
        hideFloatingActionButtonOnScroll = true;
    }

    protected Observable<Boolean> getRefreshObservable(SwipeRefreshLayout view) {
        return Observable.create( observer -> view.setOnRefreshListener(() -> observer.onNext(true)));
    }

    protected void setupSwipeRefresh(SwipeRefreshLayout view) {
        final int toolbarHeight = MetricUtil.getPixels(this.getActivity(), R.attr.actionBarSize);
        view.setProgressViewOffset(false, 0, 2 * toolbarHeight);
        view.setColorSchemeColors(this.getResources().getColor(R.color.colorchip_green));
    }

    protected void setupRecyclerView (RecyclerView view) {
        if(items == null) items = new ArrayList<>();
        adapter = this.getAdapter();
        view.setLayoutManager(this.getRecyclerViewLayoutManager());
        view.setAdapter(adapter);
    }

    protected Observable<Boolean> getRecyclerViewScrollObservable(RecyclerView view, Toolbar toolbar, boolean animateFloatingActionControl) {
        return Observable.create( observer -> view.setOnScrollListener( new PanelControllerOnScrollWithAskMore(AppConst.DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE) {
            @Override public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) { observer.onNext(null); }
            @Override public void onHidePanels () { observer.onNext(false); }
            @Override public void onShowPanels () { observer.onNext(true); }
        }))
        .map(show_panels -> {
            if (show_panels == null) return null;
            if ((boolean) show_panels) {
                if (hideToolbarOnScroll) ToolbarUtil.show(toolbar);
                if (hideFloatingActionButtonOnScroll)
                    FloatingActionControl.getInstance().show(animateFloatingActionControl);
                return true;
            } else {
                if (hideToolbarOnScroll) ToolbarUtil.hide(toolbar);
                if (hideFloatingActionButtonOnScroll)
                    FloatingActionControl.getInstance().hide(animateFloatingActionControl);
                return false;
            }
        });
    }

    protected abstract ADAPTER getAdapter ();

    protected abstract RecyclerView.LayoutManager getRecyclerViewLayoutManager ();
}