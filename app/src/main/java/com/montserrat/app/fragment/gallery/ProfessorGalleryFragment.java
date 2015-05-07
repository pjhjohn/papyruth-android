package com.montserrat.app.fragment.gallery;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.adapter.GalleryRecyclerAdapter;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONObject;

import java.util.List;

public class ProfessorGalleryFragment extends ClientFragmentWithRecyclerView<GalleryRecyclerAdapter, GalleryRecyclerAdapter.Holder.Data> {
    public ProfessorGalleryFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    protected GalleryRecyclerAdapter getAdapter (List<GalleryRecyclerAdapter.Holder.Data> items) {
        return null;
    }

    @Override
    public void onPendingRequest () {

    }

    @Override
    public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        //TODO : Implement it.
    }

    @Override
    public void onRequestResponse (JSONObject response) {

    }

    @Override
    public void onRefreshResponse (JSONObject response) {

    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new GridLayoutManager(this.getActivity(), 3);
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {

    }
}