package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.OpenSourceLicenseData;
import com.papyruth.android.recyclerview.adapter.OpenSourceLicensesAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.OpenSourceLicenseDialog;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

public class OpenSourceLicensesFragment extends Fragment implements RecyclerViewItemObjectClickListener {
    private Tracker mTracker;
    private Toolbar mToolbar;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
    }

    @InjectView(R.id.common_recycler_view) protected RecyclerView mRecyclerView;
    @InjectView(R.id.common_swipe_refresh) protected SwipeRefreshLayout mSwipeRefresh;
    private OpenSourceLicensesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_recyclerview, container, false);
        ButterKnife.inject(this, view);
        this.mCompositeSubscription = new CompositeSubscription();
        mSwipeRefresh.setEnabled(false);

        FloatingActionControl.getInstance().clear();

        this.mAdapter = new OpenSourceLicensesAdapter(getActivity(), this);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mToolbar.setTitle(R.string.toolbar_osl);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        OpenSourceLicenseDialog.show(getActivity(), ((OpenSourceLicenseData) object));
    }
}