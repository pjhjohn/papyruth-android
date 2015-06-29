package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.recyclerview.viewholder.CommentItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.EvaluationViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

/**
 * Created by SSS on 2015-05-08.
 */
public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener commentItemClickListener;
    private List<CommentData> comments;
    public EvaluationAdapter(List<CommentData> initialComments, RecyclerViewItemClickListener listener) {
        this.comments = initialComments;
        this.commentItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, commentItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        else if (position == 1) ((EvaluationViewHolder) holder).bind(Evaluation.getInstance());
        else ((CommentItemViewHolder) holder).bind(this.comments.get(position - 2));
    }

    @Override
    public int getItemCount() {
        return 2 + (this.comments == null ? 0 : this.comments.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        else if (position == 1) return ViewHolderFactory.ViewType.EVALUATION;
        else return ViewHolderFactory.ViewType.COMMENT_ITEM;
    }
}