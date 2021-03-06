package com.papyruth.android.fragment.main;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.CandidateData;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.recyclerview.adapter.EvaluationSearchAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.customview.EmptyStateView;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.ScrollableFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.navigator.OnBack;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.support.utility.search.SearchToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches SimpleCourse for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends ScrollableFragment implements RecyclerViewItemObjectClickListener, OnBack {
    private Toolbar mToolbar;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mContext = activity;
        SearchToolbar.getInstance().setSelectedQuery(null).setSelectedCandidate(new CandidateData());
    }

    @BindView(R.id.evaluation_form_query_button) protected Button mQueryButton;
    @BindView(R.id.evaluation_form_query_result) protected RecyclerView mRecyclerView;
    @BindView(R.id.evaluation_step_empty_state_view)  protected EmptyStateView mEmptyStateView;
    private CompositeSubscription mCompositeSubscription;
    private Navigator mNavigator;
    private Unbinder mUnbinder;
    private Context mContext;
    private EvaluationSearchAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mQueryButton.setText(R.string.compose_evaluation_label_search);
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        mEmptyStateView.findViewById(R.id.empty_state_shadow).setVisibility(View.GONE);
        if(mAdapter == null) mAdapter = new EvaluationSearchAdapter(getActivity(), mEmptyStateView, this.mNavigator, this);
        mAdapter.setFragment(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        SearchToolbar.getInstance().setItemObjectClickListener(null).setOnSearchByQueryListener(null);
        if(mCompositeSubscription == null || this.mCompositeSubscription.isUnsubscribed()) return;
        this.mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if (object instanceof CourseData) mAdapter.nextEvaluatonStep(((CourseData) object));
        else if(object instanceof Footer) this.mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_compose_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_green);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
        FloatingActionControl.getInstance().clear();

        mCompositeSubscription.clear();

        mCompositeSubscription.add(RxView.clicks(mQueryButton)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(event -> SearchToolbar.getInstance().show(), error -> ErrorHandler.handle(error, this))
        );
        mCompositeSubscription.add(
            this.getRecyclerViewScrollObservable(mRecyclerView, mToolbar, false, true)
                .filter(passIfNull -> passIfNull == null && (!SearchToolbar.getInstance().getSelectedCandidate().isEmpty() || SearchToolbar.getInstance().getSelectedQuery() != null))
                .subscribe(unused -> this.mAdapter.searchCourse(SearchToolbar.getInstance().getSelectedCandidate(), SearchToolbar.getInstance().getSelectedQuery(), false))
            );
        SearchToolbar.getInstance()
            .setItemObjectClickListener((view, object) -> mAdapter.searchCourse(((CandidateData) object), null, true))
            .setOnVisibilityChangedListener(visible -> mQueryButton.setVisibility(visible ? View.GONE : View.VISIBLE))
            .setOnSearchByQueryListener(() -> mAdapter.searchCourse(new CandidateData(), SearchToolbar.getInstance().getSelectedQuery(), true));
    }

    @Override
    public void onPause() {
        super.onPause();
        SearchToolbar.getInstance().setOnVisibilityChangedListener((MainActivity) getActivity());
    }


    @Override
    public boolean onBack() {
        EvaluationForm.getInstance().clear();
        return false;
    }
}
