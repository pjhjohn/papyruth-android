package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.adapter.CourseRecyclerAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

public class CourseFragment extends RecyclerViewFragment<CourseRecyclerAdapter, CourseRecyclerAdapter.Holder.Data> {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @InjectView (R.id.detail_recyclerview) protected RecyclerView recycler;
//    @InjectView (R.id.progress) protected View progress;

    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater infalter, ViewGroup container, Bundle savedInstanceState) {
        View view = infalter.inflate(R.layout.fragment_course, container, false);

        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        View info = (View) view.findViewById(R.id.detail_info);
        TextView title = (TextView) info.findViewById(R.id.info_title);
        TextView prof = (TextView) info.findViewById(R.id.info_prof);
        CourseRecyclerAdapter.Holder.Data data = new CourseRecyclerAdapter.Holder.Data("good", "good","3");
        this.setupRecyclerView(this.recycler);

        this.items.add(data);

        this.adapter.notifyDataSetChanged();

        return view;
    }

    @Override
    protected CourseRecyclerAdapter getAdapter (List<CourseRecyclerAdapter.Holder.Data> items) {
        return CourseRecyclerAdapter.newInstance(this.items, this);
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}
