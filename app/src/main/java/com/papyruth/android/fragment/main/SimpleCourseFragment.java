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
import android.widget.FrameLayout;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.SimpleCourseAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.support.utility.search.SearchToolbar;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Simple Course Fragment for listing limited contents for Course
 * TODO : should be able to expand when clicking recyclerview item to show evaluation data in detail
 */

public class SimpleCourseFragment extends TrackerFragment implements RecyclerViewItemObjectClickListener {
    private Navigator mNavigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mNavigator = (Navigator) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SearchToolbar.getInstance().setItemClickListener(null);
    }
    @InjectView(R.id.common_swipe_refresh) protected SwipeRefreshLayout mSwipeRefresh;
    @InjectView(R.id.common_recycler_view) protected RecyclerView mRecyclerView;
    @InjectView(R.id.common_empty_state)   protected FrameLayout mEmptyState;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;
    private SimpleCourseAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_recyclerview, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();

        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        mSwipeRefresh.setEnabled(false);

        mAdapter = new SimpleCourseAdapter(getActivity(), mEmptyState, this.mNavigator, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionControl.getInstance().closeMenuButton(true);
        ButterKnife.reset(this);
        SearchToolbar.getInstance().setOnSearchByQueryListener(null).setItemClickListener(null);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mToolbar.setTitle(R.string.toolbar_search);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_red).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_red);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);

        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_red).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread()).subscribe(
            unused -> mNavigator.navigate(EvaluationStep1Fragment.class, true),
            error -> ErrorHandler.handle(error, this)
        );
        if(SearchToolbar.getInstance().isReadyToSearch())
            mAdapter.loadSearchResult();
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(User.getInstance().needEmailConfirmed()){
            AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.NEED_CONFIRMATION);
            return;
        }
        if(User.getInstance().needMoreEvaluation()) {
            AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.EVALUATION_MANDATORY);
            return;
        }
        Course.getInstance().update(((CourseData) object));
        this.mNavigator.navigate(CourseFragment.class, true);
    }
}