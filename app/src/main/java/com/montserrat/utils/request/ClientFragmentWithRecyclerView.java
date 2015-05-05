package com.montserrat.utils.request;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.android.volley.VolleyError;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.recycler.PanelControllerOnScrollWithAskMore;
import com.montserrat.utils.recycler.RecyclerViewAskMoreListener;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ClientFragmentWithRecyclerView<T extends RecyclerView.Adapter<RecyclerView.ViewHolder>, E> extends ClientFragmentWithPanels implements SwipeRefreshLayout.OnRefreshListener, RecyclerViewClickListener, RecyclerViewAskMoreListener {
    protected SwipeRefreshLayout swipeRefreshView;
    private RecyclerView recyclerView;
    protected T adapter;
    protected List<E> items;
    private int swipeRefreshId, recyclerId;
    protected boolean hideToolbarOnScroll, hideFloatingActionButtonOnScroll;
    private boolean isRequestForRefreshing;
    protected PanelControllerOnScrollWithAskMore recyclerViewScrollListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        /* Bind Parameters passed via setArguments(Bundle bundle) */
        if (this.args != null) {
            this.swipeRefreshId = this.args.getInt(AppConst.Resource.SWIPE_REFRESH, AppConst.Resource.DEFAULT);
            this.recyclerId     = this.args.getInt(AppConst.Resource.RECYCLER, AppConst.Resource.DEFAULT);
        } else {
            this.swipeRefreshId = AppConst.Resource.DEFAULT;
            this.recyclerId     = AppConst.Resource.DEFAULT;
        }
        /* Initialize other member variables */
        this.hideToolbarOnScroll = true;
        this.hideFloatingActionButtonOnScroll = true;
        this.isRequestForRefreshing = false;
        this.recyclerViewScrollListener = null;

        /* Bind Views */
        this.swipeRefreshView = (SwipeRefreshLayout) view.findViewById(this.swipeRefreshId);
        if (this.swipeRefreshView == null) Timber.d("Couldn't find SwipeRefreshLayout by ID#%d", this.swipeRefreshId);
        if(this.swipeRefreshView != null) {
            final int toolbarHeight = this.vToolbar == null? 0 : this.vToolbar.getHeight();
            if(this.vToolbar != null) this.swipeRefreshView.setProgressViewOffset(false, PX2DP(toolbarHeight), PX2DP(toolbarHeight + 80));
            this.swipeRefreshView.setColorSchemeColors(this.getResources().getColor(R.color.appDefaultForegroundColor));
            this.swipeRefreshView.setEnabled(false);
            this.swipeRefreshView.setOnRefreshListener(this);
        }
        this.recyclerView = (RecyclerView) view.findViewById(this.recyclerId);
        if (this.recyclerView == null) Timber.d("Couldn't find RecyclerView by ID#%d", this.recyclerId);

        /* Register RecyclerView & its Adapter n Items */
        if(recyclerView != null) {
            this.items = new ArrayList<E>();
            this.recyclerView.setLayoutManager(this.getRecyclerViewLayoutManager());
            this.adapter = this.getAdapter(this.items);
            this.recyclerView.setAdapter(this.adapter);
            this.recyclerView.setOnScrollListener(recyclerViewScrollListener = new PanelControllerOnScrollWithAskMore(AppConst.DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE) {
                @Override
                public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                    ClientFragmentWithRecyclerView.this.onAskMore(overallItemsCount, itemsBeforeMore, maxLastVisiblePosition);
                }

                @Override
                public void onHidePanels () {
                    ClientFragmentWithRecyclerView.this.hideViews();
                }

                @Override
                public void onShowPanels () {
                    ClientFragmentWithRecyclerView.this.showViews();
                }
            });
        }

        return view;
    }

    protected abstract T getAdapter (List<E> items);
    private void hideViews() {
        if(this.vToolbar != null && this.hideToolbarOnScroll) {
            this.vToolbar.animate().translationY(-vToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        }
        if(this.vFAB != null && this.hideFloatingActionButtonOnScroll) {
            //Vertical
            //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.vFAB.getLayoutParams();
            //int fabBottomMargin = lp.bottomMargin;
            //this.vFAB.animate().translationY(this.vFAB.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            //Horizontal
            //this.fabView.animate().translationX(this.fabView.getWidth()/2).setInterpolator(new AccelerateInterpolator(2)).start();
        }
    }
    private void showViews() {
        if(this.vToolbar != null && this.hideToolbarOnScroll) this.vToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        if(this.vFAB != null && this.hideFloatingActionButtonOnScroll) {
            //Vertical
            //this.vFAB.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            //Horizontal
            //this.fabView.animate().translationX(0).setInterpolator(new DecelerateInterpolator(2)).start();
        }
    }

    @Override
    public void submit() {
        if(this.isRequestForRefreshing) this.setProgressActive(false);
        super.submit();
        if(this.isPending) {
            this.onPendingRequest(); // handle duplicated reqeust
            return;
        }
        this.recyclerViewScrollListener.setIsRequestPending(this.isPending = true);
        if(this.isRequestForRefreshing) this.swipeRefreshView.setRefreshing(true);
    }

    @Override
    public void onRefresh() {
        this.isRequestForRefreshing = true;
        /* this block at child class will call submit() */
    }

    @Override
    public final void onResponse(JSONObject response) {
        super.onResponse(response);
        if(this.isRequestForRefreshing) {
            this.swipeRefreshView.setRefreshing(false);
            this.isRequestForRefreshing = false;
            this.recyclerViewScrollListener.setIsRequestPending(this.isPending = false);
            this.setProgressActive(true);
            this.onRefreshResponse(response);
        } else {
            this.recyclerViewScrollListener.setIsRequestPending(this.isPending = false);
            this.onRequestResponse(response);
        }
    }

    @Override
    public abstract void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition);
    public abstract void onRequestResponse(JSONObject response);
    public abstract void onRefreshResponse(JSONObject response);

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        if(this.isRequestForRefreshing) {
            this.swipeRefreshView.setRefreshing(false);
            this.setProgressActive(true);
        }
        this.isRequestForRefreshing = false;
        this.recyclerViewScrollListener.setIsRequestPending(this.isPending = false);
    }

    private int PX2DP(int px){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    public abstract RecyclerView.LayoutManager getRecyclerViewLayoutManager();
}
