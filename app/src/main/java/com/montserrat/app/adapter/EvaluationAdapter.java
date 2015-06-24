package com.montserrat.app.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.utils.support.mpandroidchart.ChartUtil;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
        @InjectView (R.id.evaluation_user_avatar) protected ImageView avatar;
        @InjectView (R.id.evaluation_user_nickname) protected TextView nickname;
        @InjectView (R.id.evaluation_body) protected TextView body;
        @InjectView (R.id.evaluation_chart) protected HorizontalBarChart chart;
        public EvaluationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        public void bind(Evaluation evaluation) {
            // TODO : set avatar
            this.nickname.setText(evaluation.getUserNickname());
            this.body.setText(evaluation.getBody());
            ChartUtil.init(this.chart);
            ChartUtil.bindData(this.chart, evaluation);
            this.chart.animateY(1000);
        }
    }

    protected class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.comment_user_nickname) protected TextView nickname;
        @InjectView (R.id.comment_body) protected TextView body;
        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(CommentData comment) {
            this.nickname.setText(comment.user_nickname);
            this.body.setText(comment.body);
        }

        @Override
        public void onClick(View view) {
            commentItemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 2);
        }
    }
}