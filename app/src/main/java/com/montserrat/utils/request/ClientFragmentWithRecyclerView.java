package com.montserrat.utils.request;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.android.volley.VolleyError;
import com.melnykov.fab.FloatingActionButton;
import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.recycler.HidingScrollListener;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ClientFragmentWithRecyclerView<T extends RecyclerView.Adapter<RecyclerView.ViewHolder>, E> extends ClientFragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerViewClickListener {
    private Toolbar toolbarView;
    protected FloatingActionButton fabView;
    protected SwipeRefreshLayout swipeRefreshView;
    private RecyclerView recyclerView;
    protected T adapter;
    protected List<E> items;
    private int toolbarId, fabId, swipeRefreshId, recyclerId;
    private boolean hideToolbarOnScroll, hideFloatingActionButtonOnScroll;
    private boolean isRequestFromSwipe, isPending;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        /* Bind Parameters passed via setArguments(Bundle bundle) */
        if (this.args != null) {
            this.toolbarId      = this.args.getInt(AppConst.Resource.TOOLBAR, AppConst.Resource.DEFAULT);
            this.fabId          = this.args.getInt(AppConst.Resource.FAB, AppConst.Resource.DEFAULT);
            this.swipeRefreshId = this.args.getInt(AppConst.Resource.SWIPE_REFRESH, AppConst.Resource.DEFAULT);
            this.recyclerId     = this.args.getInt(AppConst.Resource.RECYCLER, AppConst.Resource.DEFAULT);
        } else {
            this.toolbarId      = AppConst.Resource.DEFAULT;
            this.fabId          = AppConst.Resource.DEFAULT;
            this.swipeRefreshId = AppConst.Resource.DEFAULT;
            this.recyclerId     = AppConst.Resource.DEFAULT;
        }
        /* Initialize other member variables */
        this.hideToolbarOnScroll = true;
        this.hideFloatingActionButtonOnScroll = true;
        this.isRequestFromSwipe = false;
        this.isPending = false;

        /* Bind Views */
        this.toolbarView = (Toolbar) this.getActivity().findViewById(this.toolbarId);
        if (this.toolbarView == null) Log.d("ClientFragment", "Couldn't find Toolbar by ID#" + this.toolbarId);
        this.fabView = (FloatingActionButton) view.findViewById(this.fabId);
        if (this.fabView == null) Log.d("ClientFragment", "Couldn't find FloatingActionButton by ID#" + this.fabId);
        this.swipeRefreshView = (SwipeRefreshLayout) view.findViewById(this.swipeRefreshId);
        if (this.swipeRefreshView == null) Log.d("ClientFragment", "Couldn't find SwipeRefreshLayout by ID#" + this.swipeRefreshId);
        if(this.swipeRefreshView != null) {
            final int toolbarHeight = this.toolbarView == null? 0 : this.toolbarView.getHeight();
            if(this.toolbarView != null) this.swipeRefreshView.setProgressViewOffset(false, PX2DP(toolbarHeight), PX2DP(toolbarHeight + 80));
            this.swipeRefreshView.setColorSchemeColors(this.getResources().getColor(R.color.appDefaultForegroundColor));
            this.swipeRefreshView.setEnabled(false);
            this.swipeRefreshView.setOnRefreshListener(this);
        }
        this.recyclerView = (RecyclerView) view.findViewById(this.recyclerId);
        if (this.recyclerView == null) Log.d("ClientFragment", "Couldn't find RecyclerView by ID#" + this.recyclerId);

        /* Register RecyclerView & its Adapter n Items */
        if(recyclerView != null) {
            this.items = new ArrayList<E>();
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity())); // TODO : dependency to LinearLayout
            this.adapter = this.getAdapter(this.items);
            recyclerView.setAdapter(this.adapter);
            recyclerView.setOnScrollListener(new HidingScrollListener() {
                @Override
                public void onHide() {
                    ClientFragmentWithRecyclerView.this.hideViews();
                }

                @Override
                public void onShow() {
                    ClientFragmentWithRecyclerView.this.showViews();
                }
            });
        }

        return view;
    }

    protected abstract T getAdapter (List<E> items);
    private void hideViews() {
        if(this.toolbarView != null && this.hideToolbarOnScroll) {
            this.toolbarView.animate().translationY(-toolbarView.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        }
        if(this.fabView != null && this.hideFloatingActionButtonOnScroll) {
//            Animation in vertical
//            int fabBottomMargin = lp.bottomMargin;
//            this.fabView.animate().translationY(this.fabView.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            //Horizontal
            this.fabView.animate().translationX(this.fabView.getWidth()/2).setInterpolator(new AccelerateInterpolator(2)).start();
        }
    }
    private void showViews() {
        if(this.toolbarView != null && this.hideToolbarOnScroll) this.toolbarView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        if(this.fabView != null && this.hideFloatingActionButtonOnScroll) {
//            Animation-back in vertical
//            this.fabView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            //Horizontal
            this.fabView.animate().translationX(0).setInterpolator(new DecelerateInterpolator(2)).start();
        }
    }

    @Override
    public void submit() {
        super.submit();
        if(this.isPending) {
            this.anotherRequestInProgress(); // handle duplicated reqeust
            return;
        }
        this.isPending = true;
        if(this.isRequestFromSwipe) this.swipeRefreshView.setRefreshing(true);
    }

    public abstract void anotherRequestInProgress ();

    @Override
    public void onRefresh() {
        this.isRequestFromSwipe = true;
        /* this block @ child class will call submit() */
    }

    public void onRefreshResponse(JSONObject response) {

    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
        if(this.isRequestFromSwipe) {
            this.swipeRefreshView.setRefreshing(false);
            this.isRequestFromSwipe = false;
            this.isPending = false;
            this.onRefreshResponse(response);
        } else {
            this.isRequestFromSwipe = false;
            this.isPending = false;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        if(this.isRequestFromSwipe) this.swipeRefreshView.setRefreshing(false);
        this.isRequestFromSwipe = false;
        this.isPending = false;
    }

    private int PX2DP(int px){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}
