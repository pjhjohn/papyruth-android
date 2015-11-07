package com.montserrat.app.fragment.main;

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

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.materialdialog.AlertMandatoryDialog;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.search.ToolbarSearchView;

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

    @InjectView(R.id.recyclerview) protected RecyclerView recycler;
    @InjectView(R.id.swipe) protected SwipeRefreshLayout refresh;
    @InjectView(R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        this.toolbar.setTitle(R.string.toolbar_search);
        ToolbarSearchView.getInstance().setPartialItemClickListener((v, position)->{
            ToolbarSearchView.getInstance().setSelectedCandidate(position);
            this.getSearchResult();
        });

        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_CLARITY).start();
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);

        this.subscriptions.add(
            FloatingActionControl
                .clicks(R.id.fab_new_evaluation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true))
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
                    error -> Timber.d("search error : %s", error)
                )
        );
//        toolbarSearch.searchCourse();
        this.getSearchResult();
    }
    private void getSearchResult(){
        Candidate candidate = ToolbarSearchView.getInstance().getSelectedCandidate();
        this.subscriptions.add(
            Api.papyruth().search_search(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                candidate.lecture_id,
                candidate.professor_id,
                null
            )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(courses -> {
                    this.notifyDataSetChanged(courses);
                }, error -> error.printStackTrace())
        );
    }

    public void notifyDataSetChanged(List<CourseData> courseDatas){
        this.items.clear();
        this.items.addAll(courseDatas);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(User.getInstance().needMoreEvaluation()) AlertMandatoryDialog.show(getActivity(), navigator);
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