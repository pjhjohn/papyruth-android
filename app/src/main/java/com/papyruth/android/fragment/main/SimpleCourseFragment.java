package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.Candidate;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.CourseItemsAdapter;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.materialdialog.AlertDialog;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.fragment.RecyclerViewFragment;
import com.papyruth.utils.view.navigator.Navigator;
import com.papyruth.utils.view.search.ToolbarSearchView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Simple Course Fragment for listing limited contents for Course
 * TODO : should be able to expand when clicking recyclerview item to show evaluation data in detail
 */

public class SimpleCourseFragment extends RecyclerViewFragment<CourseItemsAdapter, CourseData> {
    private Navigator navigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ToolbarSearchView.getInstance().setPartialItemClickListener(null);
    }

    @InjectView(R.id.recyclerview) protected RecyclerView recycler;
    @InjectView(R.id.swipe) protected SwipeRefreshLayout refresh;
    @InjectView(R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_search, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.refresh.setEnabled(true);

        this.setupRecyclerView(recycler);
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setupSwipeRefresh(this.refresh);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        ToolbarSearchView.getInstance().setToolbarSearchViewSearchListener(null).setPartialItemClickListener(null);
    }

    @Override
    protected CourseItemsAdapter getAdapter () {
        if(this.adapter != null)
            return adapter;
        return new CourseItemsAdapter(this.items, this, R.string.no_data_search);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        this.toolbar.setTitle(R.string.toolbar_search);
        ToolbarSearchView.getInstance().setPartialItemClickListener((v, position)->{
            ToolbarSearchView.getInstance().setSelectedCandidate(position);
            ToolbarSearchView.getInstance().addHistory(ToolbarSearchView.getInstance().getSelectedCandidate());
            this.getSearchResult();
        }).setToolbarSearchViewSearchListener(() -> this.getSearchResult());

        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.colorchip_red).start();
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);

        this.subscriptions.add(
            FloatingActionControl
                .clicks(R.id.fab_new_evaluation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true)
                    ,error->ErrorHandler.throwError(error, this))
        );
        this.subscriptions.add(
            this.getRefreshObservable(this.refresh)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    courses -> {
                        this.refresh.setRefreshing(false);
                        this.getSearchResult();
                    },
                    error -> ErrorHandler.throwError(error, this)
                )
        );
//        toolbarSearch.searchCourse();
        this.getSearchResult();
    }
    private void getSearchResult(){
        Candidate candidate = ToolbarSearchView.getInstance().getSelectedCandidate();
        String query = ToolbarSearchView.getInstance().getSelectedQuery();
        this.subscriptions.add(
            Api.papyruth().search_search(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                candidate.lecture_id,
                candidate.professor_id,
                query
            )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(courses -> {
                    this.notifyDataSetChanged(courses);
                }, error -> ErrorHandler.throwError(error, this))
        );
    }

    public void notifyDataSetChanged(List<CourseData> courseDatas){
        this.items.clear();
        this.items.addAll(courseDatas);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(User.getInstance().needMoreEvaluation()) AlertDialog.show(getActivity(), navigator, AlertDialog.Type.EVALUATION_MANDATORY);
        if(this.items.size() -1 < position){
            Toast.makeText(getActivity().getBaseContext(),"please wait for loading", Toast.LENGTH_LONG).show();
            return;
        }
        if(this.items.get(position).id == null){
            return;
        }

        Course.getInstance().update(this.items.get(position));
        this.navigator.navigate(CourseFragment.class, true);
    }
}