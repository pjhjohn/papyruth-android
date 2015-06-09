package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.Search.AutoCompletableSearchView;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PartialCourseFragment extends RecyclerViewFragment<PartialCourseAdapter, PartialCourse> implements OnPageFocus {
    private ViewPagerContainerController controller;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.controller = (ViewPagerContainerController) activity;
    }

    @InjectView(R.id.recyclerview) protected RecyclerView recycler;
    @InjectView(R.id.swipe) protected SwipeRefreshLayout refresh;
    @InjectView(R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;
    private AutoCompletableSearchView search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_search, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        this.refresh.setEnabled(true);
        this.search = new AutoCompletableSearchView(this, this.getActivity().getBaseContext(), AutoCompletableSearchView.Type.COURSE);
        this.search.courseSetup(this.recycler);
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setupSwipeRefresh(this.refresh);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) this.onPageFocused();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        Search.getInstance().clear();
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    protected PartialCourseAdapter getAdapter (List<PartialCourse> partialCourses) {
        return new PartialCourseAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        this.search.recyclerViewListClicked(view, position);
        this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.SEARCH, AppConst.ViewPager.Search.COURSE), true);
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);

        this.subscriptions.add(FloatingActionControl
            .clicks(R.id.fab_new_evaluation)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(unused -> this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.EVALUATION_STEP1), true))
        );

        this.subscriptions.add(
            this.getRefreshObservable(this.refresh)
                .flatMap(unused -> {
                    this.refresh.setRefreshing(true);
                    return RetrofitApi.getInstance().search_search(
                        User.getInstance().getAccessToken(),
                        User.getInstance().getUniversityId(),
                        Search.getInstance().getLectureId(),
                        Search.getInstance().getProfessorId(),
                        Search.getInstance().getQuery());
                })
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(courses -> {
                    this.refresh.setRefreshing(false);
                    this.search.notifycourseChanged(courses);
                },
                error ->{
                    Timber.d("search error : %s", error);
                }

                )
        );
        this.subscriptions.add(
            getRecyclerViewScrollObservable(this.recycler, this.toolbar, false)
                .filter(askmoreifnull -> askmoreifnull == null)
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    return RetrofitApi.getInstance().search_search(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, "");
                })
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(courses -> {
                    this.progress.setVisibility(View.GONE);
                    this.search.notifycourseChanged(courses);
                },
                error ->{
                    Timber.d("error : %s", error);
                })
        );
        Timber.d("search : %s", Search.getInstance().toString());

        if (Search.getInstance().isEmpty()) {
            this.search.searchCourse(AutoCompletableSearchView.Type.HISTORY);
        } else {
            this.search.searchCourse(AutoCompletableSearchView.Type.SEARCH);
        }
    }
}