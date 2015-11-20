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
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.FavoriteData;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.R;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.CourseItemsAdapter;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.fragment.RecyclerViewFragment;
import com.papyruth.utils.view.navigator.Navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Simple Course Fragment for listing limited contents for Course
 * TODO : should be able to expand when clicking recyclerview item to show evaluation data in detail
 */

public class BookmarkFragment extends RecyclerViewFragment<CourseItemsAdapter, CourseData> {
    private Navigator navigator;
    private int page;
    private List<FavoriteData> favorites;
    private boolean askMore;
    private Tracker mTracker;

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
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();

        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.favorites = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_search, container, false);
        ButterKnife.inject(this, view);
        this.page = 1;
        this.refresh.setEnabled(true);
        this.setupRecyclerView(recycler);
        this.subscriptions = new CompositeSubscription();
        askMore = true;

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
        return new CourseItemsAdapter(this.items, this, R.string.no_data_favorite);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(this.items.size() -1 < position){
            Toast.makeText(getActivity().getBaseContext(),"please wait for loading", Toast.LENGTH_LONG).show();
            return;
        }

        Course.getInstance().update(this.items.get(position));
        this.navigator.navigate(CourseFragment.class, true);
    }

    public void notifyDataChanged(List<FavoriteData> favorites){
        if(page == 1) {
            this.items.clear();
            this.favorites.clear();
        }
        this.favorites.addAll(favorites);
        for (FavoriteData f : favorites){
            this.items.add(f.course);
        }
        this.adapter.setShowPlaceholder(favorites.isEmpty());
        this.adapter.notifyDataSetChanged();

        if(favorites.size() < 1){
            askMore = false;
        }
        page ++;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        this.toolbar.setTitle(R.string.toolbar_favorite);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.toolbar_red).start();
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, true);

        this.subscriptions.add(
            FloatingActionControl
                .clicks(R.id.fab_new_evaluation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> this.navigator.navigate(EvaluationStep1Fragment.class, true),
                    error -> {
                        ErrorHandler.throwError(error, this);
                    })
        );

        this.subscriptions.add(
            Api.papyruth().users_me_favorites(User.getInstance().getAccessToken(), page)
                .map(response -> response.favorites)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    favorites -> {
                        notifyDataChanged(favorites);
                    }, error -> {
                        ErrorHandler.throwError(error, this);
                    }
                )
        );

        this.subscriptions.add(
            this.getRefreshObservable(this.refresh)
                .flatMap(
                    unused -> {
                        this.refresh.setRefreshing(true);
                        page = 1;
                        return Api.papyruth().users_me_favorites(User.getInstance().getAccessToken(), page);
                    }
                )
                .map(favorites -> favorites.favorites)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    favorites -> {
                        notifyDataChanged(favorites);
                        this.refresh.setRefreshing(false);
                    },
                    error -> ErrorHandler.throwError(error, this)
                )
        );


        this.subscriptions.add(
            super.getRecyclerViewScrollObservable(this.recycler, this.toolbar, true)
                .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE && askMore )
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    return Api.papyruth().users_me_favorites(User.getInstance().getAccessToken(), page);
                })
                .map(favorites -> favorites.favorites)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favorites -> {
                    this.progress.setVisibility(View.GONE);
                    if (favorites != null) {
                        this.notifyDataChanged(favorites);
                    }
                }, error -> ErrorHandler.throwError(error, this))
        );
    }
}