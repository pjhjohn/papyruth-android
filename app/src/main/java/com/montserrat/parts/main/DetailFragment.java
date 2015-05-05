package com.montserrat.parts.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.montserrat.activity.R;
import com.montserrat.controller.AppConst;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONObject;

import java.util.List;

public class DetailFragment extends ClientFragmentWithRecyclerView<DetailRecyclerAdapter, DetailRecyclerAdapter.Holder.Data> {

    private Button content;
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
        ListView list = (ListView) view.findViewById(R.id.detail_list);

//        this.fabView.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                DetailFragment.this.submit();
//            }
//        });

        return view;
    }

    @Override
    protected DetailRecyclerAdapter getAdapter (List<DetailRecyclerAdapter.Holder.Data> items) {
        return DetailRecyclerAdapter.newInstance(this.items);
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

    public static Fragment newInstance () {
        Fragment fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "detail/dummy");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_detail);
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
