package com.montserrat.utils.view.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.github.clans.fab.FloatingActionButton;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.utils.view.recycler.PanelControllerOnScrollWithAskMore;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class RecyclerViewFragment<ADAPTER extends RecyclerView.Adapter<RecyclerView.ViewHolder>, ITEM> extends PanelFragment
    implements RecyclerViewClickListener /*TODO:Implement it*/{

    protected ADAPTER adapter;
    protected List<ITEM> items;
    protected boolean hideToolbarOnScroll, hideFloatingActionButtonOnScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.hideToolbarOnScroll = true;
        this.hideFloatingActionButtonOnScroll = true;
    }

    protected Observable<Boolean> getRefreshObservable(SwipeRefreshLayout view) {
        return Observable.create( observer -> view.setOnRefreshListener(() -> observer.onNext(true)));
    }

    protected void setupSwipeRefresh(SwipeRefreshLayout view) {
        int offset = 0;
        TypedValue value = new TypedValue();
        if (this.getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true))
            offset = TypedValue.complexToDimensionPixelSize(value.data, getResources().getDisplayMetrics());
        view.setProgressViewOffset(false, offset, offset + 80); // TODO : avoid hard-coding
        view.setColorSchemeColors(this.getResources().getColor(R.color.fg_accent));
    }

    protected void setupRecyclerView (RecyclerView view) {
        this.items = new ArrayList<>();
        this.adapter = this.getAdapter(this.items);
        view.setLayoutManager(this.getRecyclerViewLayoutManager());
        view.setAdapter(this.adapter);
    }

    protected Observable<Boolean> getRecyclerViewScrollObservable(RecyclerView view, Toolbar toolbar, FloatingActionButton fab) {
        return Observable.create( observer -> view.setOnScrollListener( new PanelControllerOnScrollWithAskMore(AppConst.DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE) {
            @Override public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) { observer.onNext(null); }
            @Override public void onHidePanels () { observer.onNext(false); }
            @Override public void onShowPanels () { observer.onNext(true); }
        }))
        .map( show_panels -> {
            if ( show_panels == null ) return null;
            if ( (boolean) show_panels ) {
                if(toolbar != null && this.hideToolbarOnScroll) toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
                if(fab != null && this.hideFloatingActionButtonOnScroll) {
                    //Vertical
                    //this.vFAB.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    //Horizontal
                    //this.vFAB.animate().translationX(0).setInterpolator(new DecelerateInterpolator(2)).start();
                } return true;
            } else {
                if (toolbar != null && this.hideToolbarOnScroll) {
                    toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                }
                if (fab != null && this.hideFloatingActionButtonOnScroll) {
                    //Vertical
                    //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.vFAB.getLayoutParams();
                    //int fabBottomMargin = lp.bottomMargin;
                    //this.vFAB.animate().translationY(this.vFAB.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
                    //Horizontal
                    //this.vFAB.animate().translationX(this.vFAB.getWidth()/2).setInterpolator(new AccelerateInterpolator(2)).start();
                } return false;
            }
        });
    }

    protected abstract ADAPTER getAdapter (List<ITEM> items);

    protected abstract RecyclerView.LayoutManager getRecyclerViewLayoutManager ();
}