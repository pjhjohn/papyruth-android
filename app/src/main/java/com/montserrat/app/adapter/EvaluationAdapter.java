package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by SSS on 2015-05-08.
 */
public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 0;
        public static final int EVALUATION = 1;
        public static final int COMMENT = 2;
    }

    private RecyclerViewClickListener commentItemClickListener;
    private List<CommentData> comments;
    public EvaluationAdapter(List<CommentData> initialComments, RecyclerViewClickListener listener) {
        this.comments = initialComments;
        this.commentItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new HeaderViewHolder(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.EVALUATION : return new EvaluationViewHolder(inflater.inflate(R.layout.cardview_evaluation, parent, false));
            case Type.COMMENT : return new CommentViewHolder(inflater.inflate(R.layout.cardview_comment, parent, false));
            default : throw new RuntimeException("There is no ViewHolder availiable for type#" + viewType + " Make sure you're using valid type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        else if (position == 1) ((EvaluationViewHolder) holder).bind(Evaluation.getInstance());
        else ((CommentViewHolder) holder).bind(this.comments.get(position - 2));
    }

    @Override
    public int getItemCount() {
        return 2 + (this.comments == null ? 0 : this.comments.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return Type.HEADER;
        else if (position == 1) return Type.EVALUATION;
        else return Type.COMMENT;
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected class EvaluationViewHolder extends RecyclerView.ViewHolder {
        //@InjectView(something) protected Something something;
        public EvaluationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        public void bind(Evaluation evaluation) {

        }

    }

    protected class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //@InjectView(something) protected Something something;
        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(CommentData comment) {

        }

        @Override
        public void onClick(View view) {
            commentItemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 2);
        }
    }
}