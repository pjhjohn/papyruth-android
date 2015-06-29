package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.recyclerview.viewholder.EvaluationItemDetailViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.main.HomeFragment HomeFragment}
 * as an adapter for List-type {@link RecyclerView} to provide latest evaluations to user
 */
public class EvaluationItemsDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener itemClickListener;
    private List<EvaluationData> evaluations;
    public EvaluationItemsDetailAdapter(List<EvaluationData> initialEvaluations, RecyclerViewItemClickListener listener) {
        this.evaluations = initialEvaluations;
        this.itemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, itemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        ((EvaluationItemDetailViewHolder) holder).bind(this.evaluations.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return 1 + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ViewHolderFactory.ViewType.HEADER : ViewHolderFactory.ViewType.EVALUATION_ITEM_DETAIL;
    }
}
