package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
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

    private final int COURSE = 0;
    private final int LECTURE = 1;
    private final int PROFESSOR = 2;
    private final int HISTORY = 3;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_search, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        this.refresh.setEnabled(true);

        this.setupRecyclerView(this.recycler);

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

    private void searchCourse(int type) {
        if(type == HISTORY){
            // TODO : (ISSUE) Shared Preferences.
        }else {
            RetrofitApi.getInstance().search_search(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                Search.getInstance().getLectureId(),
                Search.getInstance().getProfessorId(),
                Search.getInstance().getQuery())
                    .map(response -> response.courses)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(courses -> {
                        Timber.d("Search result : %s", courses);
                        this.items.clear();
                        this.items.addAll(courses);
                        this.adapter.notifyDataSetChanged();
                    },error -> {
                        Timber.d("Search error : %s", error);
                    });
        }
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
        setup(position);
        getHistory();
        this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.SEARCH, AppConst.ViewPager.Search.COURSE), true);

    }

    public void setup(int position){
        PartialCourse item = items.get(position);
        Course.getInstance().clear();
        Course.getInstance().setId(item.id);
        Course.getInstance().setName(item.name);
        Course.getInstance().setCode(item.code);
        Course.getInstance().setProfessor(item.professor_name);
        Course.getInstance().setProfessorId(item.professor_id);
        Course.getInstance().setLectureId(item.lecture_id);
        Course.getInstance().setUniversityId(User.getInstance().getUniversityId());
        Course.getInstance().setUnit(item.unit);
        Course.getInstance().setPointOverall(item.point_overall);
        Course.getInstance().setPointEasiness(item.point_easiness);
        Course.getInstance().setPointClarity(item.point_clarity);
        Course.getInstance().setPointGpaSatisfaction(item.point_gpa_satisfaction);

    }

    public List<PartialCourse> getHistory(){
        Timber.d("getHistory");
        // TODO : implement it!
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext());

        SharedPreferences.Editor editor = preferences.edit();
        Gson data = new Gson();
        String json = data.toJson(items.get(0));
        Timber.d("json : %s", json);

        return null;
    }
    public boolean addHistory(PartialCourse item){
        Timber.d("getHistory");
        return false;
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
                .subscribe(lectures -> {
                    this.refresh.setRefreshing(false);

                    this.items.clear();
                    this.items.addAll(lectures);
                    this.adapter.notifyDataSetChanged();
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
                .subscribe(lectures -> {
                    this.progress.setVisibility(View.GONE);
                    this.items.addAll(lectures);
                    this.adapter.notifyDataSetChanged();
                },
                error ->{
                    Timber.d("error : %s", error);
                })
        );

        if (Search.getInstance().isEmpty()) {
//            getHistory();
        } else {
            if(Search.getInstance().getCourse() != null) {
                searchCourse(COURSE);
            } else if(Search.getInstance().getLectureId() != null) {
                searchCourse(LECTURE);
            } else if(Search.getInstance().getProfessorId() != null) {
                searchCourse(PROFESSOR);
            } else if(Search.getInstance().getQuery() != null) {
                searchCourse(HISTORY);
            }
        }
    }
}