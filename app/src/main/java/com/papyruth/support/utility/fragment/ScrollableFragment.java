package com.papyruth.support.utility.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.helper.MetricHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.recyclerview.PanelControllerOnScrollWithAskMore;

import rx.Observable;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public abstract class ScrollableFragment extends Fragment {
    protected boolean mHideToolbarOnScroll, mHideFloatingActionControlOnScroll;
    protected Context mContext;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHideToolbarOnScroll = true;
        mHideFloatingActionControlOnScroll = true;
    }

    protected void initSwipeRefresh(SwipeRefreshLayout view) {
        final int toolbarHeight = MetricHelper.getPixels(mContext, R.attr.actionBarSize);
        view.setProgressViewOffset(false, 0, 2 * toolbarHeight);
        view.setColorSchemeColors(AppConst.DEFAULT_PROGRESSBAR_COLOR_SCHEME(mContext));
    }

    protected Observable<Boolean> getSwipeRefreshObservable(SwipeRefreshLayout view) {
        return Observable.create(observer -> view.setOnRefreshListener(() -> observer.onNext(true)));
    }

    protected Observable<Boolean> getRecyclerViewScrollObservable(RecyclerView view, Toolbar toolbar, boolean animateFAC) {
        return Observable.create( observer -> view.setOnScrollListener( new PanelControllerOnScrollWithAskMore(AppConst.DEFAULT_RECYCLERVIEW_THRESHOLD_TO_ASK_MORE) {
            @Override public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) { observer.onNext(null); }
            @Override public void onHidePanels () { observer.onNext(false); }
            @Override public void onShowPanels () { observer.onNext(true); }
        }))
        .map(show_panels -> {
            if (show_panels == null) return null;
            if ((boolean) show_panels) {
                if (mHideToolbarOnScroll) ToolbarHelper.show(toolbar);
                if (mHideFloatingActionControlOnScroll) FloatingActionControl.getInstance().show(animateFAC);
                return true;
            } else {
                if (mHideToolbarOnScroll) ToolbarHelper.hide(toolbar);
                if (mHideFloatingActionControlOnScroll) FloatingActionControl.getInstance().hide(animateFAC);
                return false;
            }
        });
    }
}