package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.model.Candidate;
import com.papyruth.android.recyclerview.viewholder.AutoCompleteResponseViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

public class AutoCompleteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener itemClickListener; // TODO : use if implemented.
    private boolean isHistory;

    private List<Candidate> items;
    public AutoCompleteAdapter(List<Candidate> initItemList, RecyclerViewItemClickListener listener) {
        this.items = initItemList;
        this.itemClickListener = listener;
        this.isHistory = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, itemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((AutoCompleteResponseViewHolder) holder).bind(this.items.get(position), isHistory);
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ViewHolderFactory.ViewType.AUTO_COMPLETE_RESPONSE;
    }

    public void setHistory(boolean isHistory){
        this.isHistory = isHistory;
    }
}
