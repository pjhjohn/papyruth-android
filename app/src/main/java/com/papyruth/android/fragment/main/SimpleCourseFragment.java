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
import com.papyruth.android.model.CandidateData;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.SimpleCourseAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.ScrollableFragment;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.support.utility.search.SearchToolbar;
import com.papyruth.support.utility.customview.EmptyStateView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Simple Course Fragment for listing limited contents for Course
 * TODO : should be able to expand when clicking recyclerview item to show evaluation data in detail
 */

public class SimpleCourseFragment extends ScrollableFragment implements RecyclerViewItemObjectClickListener {
    private Navigator mNavigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mNavigator = (Navigator) activity;
        SearchToolbar.getInstance().setSelectedQuery(null).setSelectedCandidate(new CandidateData());
    }

    @Bind(R.id.common_swipe_refresh) protected SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.common_recycler_view) protected RecyclerView mRecyclerView;
    @Bind(R.id.common_empty_state_view)   protected EmptyStateView mEmptyState;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;
    private SimpleCourseAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_recyclerview, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();

        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        mSwipeRefresh.setEnabled(false);
        if(mAdapter == null) mAdapter = new SimpleCourseAdapter(getActivity(), mEmptyState, this.mNavigator, this);
        mAdapter.setFragment(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionControl.getInstance().closeMenuButton(true);
        ButterKnife.unbind(this);
        SearchToolbar.getInstance().setOnSearchByQueryListener(null).setItemObjectClickListener(null);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeSubscription.clear();
        this.mCompositeSubscription.add(
            this.getRecyclerViewScrollObservable(mRecyclerView, mToolbar, true)
                .filter(passIfNull -> passIfNull == null && (!SearchToolbar.getInstance().getSelectedCandidate().isEmpty() || SearchToolbar.getInstance().getSelectedQuery() != null))
                .subscribe(unused -> mAdapter.loadSearchResult(false))
        );
        this.mToolbar.setTitle(R.string.toolbar_search);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_red).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_red);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, true);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);

        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_new_evaluation_red).show(true, 200, TimeUnit.MILLISECONDS);
        FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread()).subscribe(
            unused -> {
                EvaluationForm.getInstance().clear();
                mNavigator.navigate(EvaluationStep1Fragment.class, true);
            },
            error -> ErrorHandler.handle(error, this)
        );
        if(SearchToolbar.getInstance().isReadyToSearch())
            mAdapter.loadSearchResult(true);
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof CourseData) {
            if (User.getInstance().emailConfirmationRequired()) {
                AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.USER_CONFIRMATION_REQUIRED);
                return;
            }
            if (User.getInstance().mandatoryEvaluationsRequired()) {
                AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.MANDATORY_EVALUATION_REQUIRED);
                return;
            }
            Course.getInstance().update(((CourseData) object));
            this.mNavigator.navigate(CourseFragment.class, true);
        }else if(object instanceof Footer){
            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
        }
    }
}