package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.model.OpenSourceLicenseData;
import com.papyruth.android.recyclerview.viewholder.OpenSourceLicenseViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

public class OpenSourceLicensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private List<OpenSourceLicenseData> mOpenSourceLicenseDataList;

    public OpenSourceLicensesAdapter(List<OpenSourceLicenseData> initialOpenSourceLicenseDataList, RecyclerViewItemClickListener listener) {
        mRecyclerViewItemClickListener = listener;
        mOpenSourceLicenseDataList = initialOpenSourceLicenseDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, mRecyclerViewItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        ((OpenSourceLicenseViewHolder) holder).bind(mOpenSourceLicenseDataList.get(position - 1));  // 1 for header
    }

    @Override
    public int getItemCount() {
        if (mOpenSourceLicenseDataList == null) return 1;   // 1 for header
        return mOpenSourceLicenseDataList.size() + 1;       // 1 for header
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        else return ViewHolderFactory.ViewType.OPEN_SOURCE_LICENSE;
    }
}