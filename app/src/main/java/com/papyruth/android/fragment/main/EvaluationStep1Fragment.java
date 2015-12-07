package com.papyruth.android.fragment.main;


import android.app.Activity;
import android.app.Fragment;
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
import android.widget.FrameLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.Candidate;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.EvaluationSearchAdapter;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.support.utility.search.SearchToolbar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches SimpleCourse for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends Fragment implements RecyclerViewItemObjectClickListener {
    private Toolbar mToolbar;
    private Tracker mTracker;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
        mContext = activity;
    }

    @InjectView(R.id.evaluation_form_query_button) protected Button mQueryButton;
    @InjectView(R.id.evaluation_form_query_result) protected RecyclerView mRecyclerView;
    @InjectView(R.id.common_empty_state)   protected FrameLayout mEmptyState;
    private CompositeSubscription mCompositeSubscription;
    private Navigator mNavigator;
    private Context mContext;
    private EvaluationSearchAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mQueryButton.setText(R.string.toolbar_search);
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        mAdapter = new EvaluationSearchAdapter(getActivity(), mEmptyState, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        SearchToolbar.getInstance().setItemClickListener(null).setOnSearchByQueryListener(null);
        if(mCompositeSubscription == null || this.mCompositeSubscription.isUnsubscribed()) return;
        this.mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        this.nextEvaluatonStep(((CourseData) object));

        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_write_evaluation1));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mToolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_green);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
        FloatingActionControl.getInstance().clear();

        mCompositeSubscription.add(ViewObservable.clicks(mQueryButton)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> SearchToolbar.getInstance().show(), error -> ErrorHandler.handle(error, this))
        );
        SearchToolbar.getInstance()
            .setItemClickListener((v, position) -> mAdapter.searchCourse(SearchToolbar.getInstance().getCandidates().get(position), null))
            .setOnVisibilityChangedListener(visible -> mQueryButton.setVisibility(visible ? View.GONE : View.VISIBLE))
            .setOnSearchByQueryListener(() -> mAdapter.searchCourse(new Candidate(), SearchToolbar.getInstance().getSelectedQuery()));
    }

    private void nextEvaluatonStep(CourseData course){
        EvaluationForm.getInstance().setCourseId(course.id);
        EvaluationForm.getInstance().setLectureName(course.name);
        EvaluationForm.getInstance().setProfessorName(course.professor_name);
        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), course.id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    this.mNavigator.navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_POSSIBLE);
                }
            }, error -> ErrorHandler.handle(error, this));
    }

    @Override
    public void onPause() {
        super.onPause();
        SearchToolbar.getInstance().setOnVisibilityChangedListener((MainActivity) getActivity());
    }
}