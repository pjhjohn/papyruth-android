package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.model.Candidate;
import com.papyruth.android.recyclerview.viewholder.AutoCompleteResponseViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

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
        return ViewHolderFactory.getInstance().create(parent, viewType, ((view, position) -> {
            itemClickListener.onRecyclerViewItemClick(view, position-getItemHeader());
        }));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position < items.size() + getItemHeader() && position > 0)
            ((AutoCompleteResponseViewHolder) holder).bind(this.items.get(position- getItemHeader()), isHistory);
    }

    public int getItemHeader(){
        return items.size() > 0 ? 1 : 0;
    }
    public int getItemFooter(){
        return items.size() > 0 ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size() + getItemHeader() + getItemFooter();
    }

    @Override
    public int getItemViewType(int position) {
        if(position <= 0)
            return ViewHolderFactory.ViewType.HR_WHITE;
        else if(position < items.size() + getItemHeader())
            return ViewHolderFactory.ViewType.AUTO_COMPLETE_RESPONSE;
        else
            return ViewHolderFactory.ViewType.HR_SHADOW;
    }

    public void setIsHistory(boolean isHistory){
        this.isHistory = isHistory;
    }
}
