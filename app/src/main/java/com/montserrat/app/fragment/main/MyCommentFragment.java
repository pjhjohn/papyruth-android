package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.MyCommentAdapter;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MyCommentFragment extends RecyclerViewFragment<MyCommentAdapter, CommentData>{
    private Navigator navigator;
    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        navigator = (Navigator) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    @InjectView (R.id.recyclerview) protected RecyclerView recyclerView;
    @InjectView (R.id.swipe) protected SwipeRefreshLayout swipeRefresh;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();

        /* View Initialization */
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.swipeRefresh.setEnabled(true);
        this.setupRecyclerView(this.recyclerView);
        this.setupSwipeRefresh(this.swipeRefresh);

        toolbar.setTitle(R.string.my_evaluation);
        toolbar.setTitleTextColor(Color.WHITE);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_EASINESS).start();

        return view;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        if(this.subscriptions != null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
        ButterKnife.reset(this);
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
    }
    private int page = 1;
    @Override
    public void onResume() {
        super.onResume();

        this.subscriptions.add(
            super.getRefreshObservable(this.swipeRefresh)
                .flatMap(unused -> {
                    this.swipeRefresh.setRefreshing(true);
                    return RetrofitApi.getInstance().users_me_comments(User.getInstance().getAccessToken(), page = 1);
                })
                .filter(response -> response.success)
                .map(mywritten -> mywritten.comments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    this.swipeRefresh.setRefreshing(false);
                    this.items.clear();
                    if (comments != null) {
                        this.items.addAll(comments);
                    }
                    this.adapter.notifyDataSetChanged();
                }, error -> {
                    this.swipeRefresh.setRefreshing(false);
                    error.printStackTrace();
                })
        );

        this.subscriptions.add(
            super.getRecyclerViewScrollObservable(this.recyclerView, this.toolbar, false)
                .startWith((Boolean) null)
                .filter(passIfNull -> passIfNull == null && this.progress.getVisibility() != View.VISIBLE)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(unused -> {
                    this.progress.setVisibility(View.VISIBLE);
                    this.swipeRefresh.setRefreshing(false);
                    // TODO : handle the case for max_id == 0 : prefer not to request to server
                    Timber.d("calling");
                    return RetrofitApi.getInstance().users_me_comments(User.getInstance().getAccessToken(), page++);
                })
                .map(mywritten -> mywritten.comments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    this.progress.setVisibility(View.GONE);
                    if (comments != null) this.items.addAll(comments);
                    this.adapter.notifyDataSetChanged();
                }, error -> {
                    this.progress.setVisibility(View.GONE);
                    error.printStackTrace();
                })
        );
    }



    @Override
    protected MyCommentAdapter getAdapter () {
        return new MyCommentAdapter(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager () {
        return new LinearLayoutManager(this.getActivity());
    }
}