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
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.melnykov.fab.FloatingActionButton;
import com.montserrat.activity.R;
import com.montserrat.utils.recycler.HidingScrollListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ClientFragmentWithRecyclerView<T extends RecyclerView.Adapter<RecyclerView.ViewHolder>, E> extends ClientFragment{
    private static final String TAG = "ClientFragment";

    protected Toolbar toolbar = null;
    protected FloatingActionButton fab = null;
    protected SwipeRefreshLayout swipe = null;
    protected T adapter = null;
    protected List<E> items = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /* Register Toolbar */
        this.toolbar = (Toolbar) this.getActivity().findViewById(this.getToolbarId());
        if (this.toolbar == null) Log.d(TAG, "Couldn't find ID of toolbar");

        /* Register FAB */
        this.fab = (FloatingActionButton) view.findViewById(this.getFloatingActionButtonId());
        if (this.fab == null) Log.d(TAG, "Couldn't find ID of FAB");

        /* Register SwipeRefreshLayout */
        this.swipe = (SwipeRefreshLayout) view.findViewById(this.getSwipeRefreshLayoutId());
        if (this.swipe == null) Log.d(TAG, "Couldn't find ID of SwipeRefreshLayout");
        else {
            Log.d("DEBUG", "Height of Toolbar : " + this.toolbar.getHeight());
            if(this.toolbar != null) this.swipe.setProgressViewOffset(false, PX2DP(this.toolbar.getHeight()), PX2DP(this.toolbar.getHeight() + 80));
            this.swipe.setColorSchemeColors(this.getResources().getColor(R.color.appDefaultForegroundColor));
            this.swipe.setEnabled(false);
        }

        /* Register RecyclerView & its Adapter&Items */
        this.items = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(this.getRecyclerViewId());
        if (recyclerView == null) throw new RuntimeException("Couldn't find RecyclerView");

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity())); // TODO : has issue of dependency on LinearLayout
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
        return view;
    }

    protected abstract T getAdapter (List<E> items);

    protected int getToolbarId() {
        return 0;
    }

    protected int getFloatingActionButtonId() {
        return 0;
    }

    protected abstract int getRecyclerViewId();

    protected int getSwipeRefreshLayoutId() { return 0; }


    private void hideViews() {
        if(this.toolbar != null) {
            this.toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        }
        if(this.fab != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            this.fab.animate().translationY(this.fab.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
        }
    }

    private void showViews() {
        if(this.toolbar != null) this.toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        if(this.fab != null) this.fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    public void submit(JSONObject jsonToRequest) {
        super.submit(jsonToRequest);
        this.swipe.setRefreshing(true);
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
        this.swipe.setRefreshing(false);
    }

    private int PX2DP(int px){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}
