package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.professor) protected TextView professor;
    @InjectView (R.id.lecture) protected TextView lecture;
    @InjectView (R.id.evaluation_body) protected TextView comment;
    @InjectView (R.id.point_overall) protected RatingBar overall;
    private RecyclerViewItemClickListener itemClickListener;
    public EvaluationItemDetailViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
    }

    public void bind(EvaluationData evaluation) {
        this.professor.setText(evaluation.professor_name);
        this.lecture.setText(evaluation.lecture_name);
        this.comment.setText(evaluation.body);
        this.overall.setRating(evaluation.point_overall);
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 1);
    }
}