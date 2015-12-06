package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.model.UniversityData;
import com.papyruth.android.recyclerview.viewholder.UniversityViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

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
        if(mSelectedPosition != null)
            holder.itemView.setSelected(position == mSelectedPosition);
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

    private Integer mSelectedPosition;
    public void setSelected(int position){
        mSelectedPosition = position;
    }
}
