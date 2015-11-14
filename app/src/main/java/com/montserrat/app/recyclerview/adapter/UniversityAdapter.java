package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.model.UniversityData;
import com.montserrat.app.recyclerview.viewholder.UniversityViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

public class UniversityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private List<UniversityData> mUniversityDataList;

    public UniversityAdapter(List<UniversityData> initialUniversityDataList, RecyclerViewItemClickListener listener) {
        mRecyclerViewItemClickListener = listener;
        mUniversityDataList = initialUniversityDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, mRecyclerViewItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UniversityViewHolder) holder).bind(mUniversityDataList.get(position));
    }

    @Override
    public int getItemCount() {
        if (mUniversityDataList == null) return 0;
        return mUniversityDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ViewHolderFactory.ViewType.UNIVERSITY;
    }
}
