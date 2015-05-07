package com.montserrat.utils.request;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.utils.recycler.PanelControllerOnScrollWithAskMore;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class RecyclerViewFragment<ADAPTER extends RecyclerView.Adapter<RecyclerView.ViewHolder>, ITEM>
    extends PanelFragment
    implements RecyclerViewClickListener /*TODO:Implement it*/{

    protected SwipeRefreshLayout vSwipeRefresh;
    protected RecyclerView vRecycler;
    protected ADAPTER adapter;
    protected List<ITEM> items;
    protected boolean hideToolbarOnScroll, hideFloatingActionButtonOnScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.hideToolbarOnScroll = true;
        this.hideFloatingActionButtonOnScroll = true;
    }

    @Override
    public void onResume() {
        super.onResume();


    }
//    final int toolbarHeight = this.vToolbar == null? 0 : this.vToolbar.getHeight();
//    if(this.vToolbar != null) this.swipeRefreshView.setProgressViewOffset(false, PX2DP(toolbarHeight), PX2DP(toolbarHeight + 80));

    protected Observable<Boolean> getRefreshObservable() {
        this.vSwipeRefresh.setEnabled(false);
        return Observable.create( observer -> {
            this.vSwipeRefresh.setOnRefreshListener(() -> {
                observer.onNext(true);
                observer.onCompleted();
            });
        });
    }
    protected void setupSwipeRefresh(int offset) {
        this.vSwipeRefresh.setProgressViewOffset(false, offset, offset + 80); // TODO : avoid hard-coding
        this.vSwipeRefresh.setColorSchemeColors(this.getResources().getColor(R.color.appDefaultForegroundColor));
    }

    protected void setupRecyclerView () {
        this.items = new ArrayList<ITEM>();
        this.vRecycler.setLayoutManager(this.getRecyclerViewLayoutManager());
        this.adapter = this.getAdapter(this.items);
        this.vRecycler.setAdapter(this.adapter);
    }

    protected Observable<Boolean> getRecyclerViewScrollObservable() {
        return Observable.create( observer -> this.vRecycler.setOnScrollListener( new PanelControllerOnScrollWithAskMore(AppConst.DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE) {
            @Override public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) { observer.onNext(null); }
            @Override public void onHidePanels () { observer.onNext(false); }
            @Override public void onShowPanels () { observer.onNext(true); }
        }))
        .map( show_panels -> {
            if ( show_panels == null ) return null;
            if ( (boolean) show_panels ) {
                if(this.vToolbar != null && this.hideToolbarOnScroll) this.vToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
                if(this.vFAB != null && this.hideFloatingActionButtonOnScroll) {
                    //Vertical
                    //this.vFAB.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    //Horizontal
                    //this.vFAB.animate().translationX(0).setInterpolator(new DecelerateInterpolator(2)).start();
                } return true;
            } else {
                if (this.vToolbar != null && this.hideToolbarOnScroll) {
                    this.vToolbar.animate().translationY(-vToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                }
                if (this.vFAB != null && this.hideFloatingActionButtonOnScroll) {
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

    public static int PX2DP(int pixels, Context context){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());
    }
}