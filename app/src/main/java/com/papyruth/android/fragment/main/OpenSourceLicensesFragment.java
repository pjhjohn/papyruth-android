package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.OpenSourceLicenseData;
import com.papyruth.android.recyclerview.adapter.OpenSourceLicensesAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.OpenSourceLicenseDialog;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class OpenSourceLicensesFragment extends TrackerFragment implements RecyclerViewItemObjectClickListener {
    private Toolbar mToolbar;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
    }

    @Bind(R.id.common_recycler_view) protected RecyclerView mRecyclerView;
    @Bind(R.id.common_swipe_refresh) protected SwipeRefreshLayout mSwipeRefresh;
    private OpenSourceLicensesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_recyclerview, container, false);
        ButterKnife.bind(this, view);
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

        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
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