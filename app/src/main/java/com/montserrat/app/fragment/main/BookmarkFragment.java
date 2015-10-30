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
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.FavoriteData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.search.ToolbarSearch;

import java.util.ArrayList;
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

public class BookmarkFragment extends RecyclerViewFragment<CourseItemsAdapter, CourseData> {
    private Navigator navigator;
    private int page;
    private List<FavoriteData> favorites;

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

        this.favorites = new ArrayList<>();
        this.page = 1;

        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.toolbar.setTitle(R.string.toolbar_favorite);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_CLARITY).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_search, container, false);
        ButterKnife.inject(this, view);
        this.refresh.setEnabled(true);
        this.setupRecyclerView(recycler);
        this.subscriptions = new CompositeSubscription();

        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
        this.getBookmark();
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
        return new CourseItemsAdapter(this.items, this);
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

    public void getBookmark(){
        this.subscriptions.add(
            RetrofitApi.getInstance().users_me_favorites(User.getInstance().getAccessToken(), page++)
                .map(response -> response.favorites)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    favorites -> {
                        notifyDataChanged(favorites);
                    }, error -> error.printStackTrace()
                )
        );
    }
    public void notifyDataChanged(List<FavoriteData> favorites){
        this.items.clear();
        this.favorites.clear();
        this.favorites.addAll(favorites);
        for (FavoriteData f : favorites){
            this.items.add(f.course);
        }
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    },
                    error -> Timber.d("search error : %s", error)
                )
        );
    }
}