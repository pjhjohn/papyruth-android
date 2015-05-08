package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.melnykov.fab.FloatingActionButton;
import com.montserrat.app.R;
import com.montserrat.app.adapter.LecturesRecyclerAdapter;
import com.montserrat.app.model.User;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.FragmentHelper;
import com.montserrat.utils.request.RecyclerViewFragment;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class LecturesFragment extends RecyclerViewFragment<LecturesRecyclerAdapter, LecturesRecyclerAdapter.Holder.Data> {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @InjectView(R.id.recyclerview) protected RecyclerView recycler;
    @InjectView(R.id.fab) protected FloatingActionButton fab;
    @InjectView(R.id.swipe) protected SwipeRefreshLayout refresh;
    @InjectView(R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        this.refresh.setEnabled(true);

        this.setupRecyclerView(this.recycler);

        this.setupFloatingActionButton(this.fab);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypedValue tv = new TypedValue();
        int actionbarHeight = 0;
        if (this.getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionbarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        Timber.d("Toolbar height : %d", actionbarHeight);
        this.setupSwipeRefresh(this.refresh, actionbarHeight);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    protected LecturesRecyclerAdapter getAdapter (List<LecturesRecyclerAdapter.Holder.Data> datas) {
        return LecturesRecyclerAdapter.newInstance(this.items, this);
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
    public void onStart() {
        super.onStart();
        this.subscriptions.add(getRefreshObservable(this.refresh)
            .flatMap(unused -> {
                JSONObject params = new JSONObject();
                try {
                    params.putOpt("university_id", User.getInstance().getUniversityId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return RxVolley.createObservable(Api.url("lectures"), Request.Method.GET, User.getInstance().getAccessToken(), params);
            })
            .subscribe(response -> {
                this.refresh.setRefreshing(false);
                switch (response.optInt("status")) {
                    case 200:
                        JSONArray lectures = response.optJSONArray("lectures");
                        if (lectures == null) return;
                        this.items.clear();
                        for (int i = 0; i < lectures.length(); i++) {
                            JSONObject lecture = lectures.optJSONObject(i);
                            if (lecture == null) continue;
                            this.items.add(new LecturesRecyclerAdapter.Holder.Data(
                                lecture.optString("name", "NO-NAME"),
                                lecture.optString("professor", "<PROFESSOR>"),
                                (float) lecture.optDouble("rating", 0.0)
                            ));
                        }
                        this.adapter.notifyDataSetChanged();
                        break;
                    default:
                        Timber.e("Unexpected Status code : %d - Needs to be implemented", response.optInt("status"));
                }
            })
        );

        this.subscriptions.add(getRecyclerViewScrollObservable(this.recycler, this.toolbar, this.fab)
            .filter(askmoreifnull -> askmoreifnull == null)
            .flatMap(unused -> {
                FragmentHelper.showProgress(this.progress, true);
                JSONObject params = new JSONObject();
                try {
                    params.putOpt("university_id", User.getInstance().getUniversityId());
                    params.putOpt("limit", 10);
                } catch (JSONException e) {
                    Timber.e("JSONException Occured with possible null value UserInfo.university_id : %s", User.getInstance().getUniversityId());
                }
                return RxVolley.createObservable(Api.url("lectures"), Request.Method.GET, User.getInstance().getAccessToken(), params);
            })
            .subscribe(response ->{
                FragmentHelper.showProgress(this.progress, false);
                switch (response.optInt("status")) {
                    case 200:
                        JSONArray lectures = response.optJSONArray("lectures");
                        if (lectures == null) return;
                        for (int i = 0; i < lectures.length(); i++) {
                            JSONObject lecture = lectures.optJSONObject(i);
                            if (lecture == null) continue;
                            this.items.add(new LecturesRecyclerAdapter.Holder.Data(
                                lecture.optString("name", "NO-NAME"),
                                lecture.optString("professor", "<PROFESSOR>"),
                                (float) lecture.optDouble("rating", 0.0)
                            ));
                        }
                        this.adapter.notifyDataSetChanged();
                    default:
                        Timber.e("Unexpected Status code : %d - Needs to be implemented", response.optInt("status"));
                }
            })
        );
    }
}