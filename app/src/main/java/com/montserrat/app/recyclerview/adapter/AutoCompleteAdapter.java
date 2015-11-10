package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.model.Candidate;
import com.montserrat.app.recyclerview.viewholder.AutoCompleteResponseViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

/**
 * Created by SSS on 2015-04-25.
 */
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
