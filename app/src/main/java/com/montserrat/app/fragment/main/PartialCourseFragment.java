package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.app.adapter.PartialCourseAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PartialCourseFragment extends RecyclerViewFragment<PartialCourseAdapter, PartialCourse> implements OnPageFocus {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        // TODO : implement it!
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) this.onPageFocused();
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().setMenu(R.layout.fam_home).hideMenuButton(false);
        FloatingActionControl.show(true);

        this.subscriptions.add(FloatingActionControl
                        .clicks(R.id.fab_new_evaluation)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(unused -> this.callback.onCategorySelected(NavFragment.CategoryType.EVALUATION))
        );

        this.subscriptions.add(
            this.getRefreshObservable(this.refresh)
                .flatMap(unused -> {
                    this.refresh.setRefreshing(true);
                    return RetrofitApi.getInstance().lectures(User.getInstance().getAccessToken(), null, null);
                })
                .map(response -> response.lectures)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lectures -> {
                    this.refresh.setRefreshing(false);
                    this.items.clear();
                    this.items.addAll(lectures);
                    this.adapter.notifyDataSetChanged();
                })
        );
        this.subscriptions.add(
            getRecyclerViewScrollObservable(this.recycler, this.toolbar, false)
                .filter(askmoreifnull -> askmoreifnull == null)
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    return RetrofitApi.getInstance().lectures(User.getInstance().getAccessToken(), null, null);
                })
                .map(response -> response.lectures)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lectures -> {
                    this.progress.setVisibility(View.GONE);
                    this.items.addAll(lectures);
                    this.adapter.notifyDataSetChanged();
                })
        );
    }
}