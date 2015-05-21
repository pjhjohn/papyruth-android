package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.app.adapter.CourseAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import rx.subscriptions.CompositeSubscription;

public class CourseFragment extends RecyclerViewFragment<CourseAdapter, PartialEvaluation> {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle savedInstanceState) {
        View view = infalter.inflate(R.layout.fragment_course, container, false);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.setupRecyclerView((RecyclerView)view.findViewById(R.id.detail_recyclerview));
        this.items.add(newev("1", "comment", 3));
        this.items.add(newev("1", "comment", 3));
        this.items.add(newev("1", "comment", 3));
        this.items.add(newev("1", "comment", 3));
        this.items.add(newev("1", "comment", 3));
        this.items.add(newev("1", "comment", 3));
        this.adapter.notifyDataSetChanged();
        return view;
    }

    public PartialEvaluation newev(String userid, String comment, int like){
        PartialEvaluation ev = new PartialEvaluation();
        ev.professor_name = userid;
        ev.comment = comment;
        ev.point_overall = like;
        return ev;
    }

    @Override
    protected CourseAdapter getAdapter (List<PartialEvaluation> items) {
        return CourseAdapter.newInstance(this.items, this);
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}
