package com.montserrat.parts.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONObject;

import java.util.List;

public class CourseFragment extends ClientFragmentWithRecyclerView<CourseRecyclerAdapter, CourseRecyclerAdapter.Holder.Data> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

//        this.swipeRefreshView.setEnabled(false);
        View info = (View) view.findViewById(R.id.detail_info);
        TextView title = (TextView) info.findViewById(R.id.info_title);
        TextView prof = (TextView) info.findViewById(R.id.info_prof);
//        Button btb = (Button)view.findViewById(R.id.button2);
        title.setText("this is contents");
        prof.setText("this is prof");
//        btb.setText("button!!!!!");
//        View list = (RecyclerView) view.findViewById(R.id.detail_recyclerview);

//        this.fabView.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                CourseFragment.this.submit();
//            }
//        });
        this.items.add(new CourseRecyclerAdapter.Holder.Data("good", "good","3"));

        return view;
    }

    @Override
    protected CourseRecyclerAdapter getAdapter (List<CourseRecyclerAdapter.Holder.Data> items) {
        return CourseRecyclerAdapter.newInstance(this.items, this);
    }

    @Override
    public void onRequestResponse(JSONObject response) {


    }

    @Override
    public void onRefreshResponse(JSONObject response) {

    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        this.setParameters(new JSONObject()).submit();
    }

    @Override
    public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        //TODO : Implement it.
    }

    @Override
    public void onPendingRequest () {
        Toast.makeText(this.getActivity(), "Another request is pending...", Toast.LENGTH_SHORT).show();
    }

    public static Fragment newInstance () {
        Fragment fragment = new CourseFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "detail/dummy");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_course);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.detail_recyclerview);
        bundle.putInt(AppConst.Resource.TOOLBAR, R.id.toolbar);
//        bundle.putInt(AppConst.Resource.FAB, R.id.detail_fab);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}
