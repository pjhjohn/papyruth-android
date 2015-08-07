package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyCommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.my_written_body) protected TextView body;
    @InjectView(R.id.my_written_lecture) protected TextView lecture;
    @InjectView(R.id.my_written_professor) protected TextView professor;
    @InjectView(R.id.my_written_timestamp) protected TextView timestamp;

    private RecyclerViewItemClickListener itemClickListener;
    public MyCommentViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
    }

    public void bind(CommentData comment) {
        final Context context = this.itemView.getContext();
//        this.lecture.setText(comment.evaluation_id);
//        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), comment.id, context.getResources().getString(R.string.professor_postfix))));
        this.body.setText(comment.body);
        this.timestamp.setText(DateTimeUtil.timeago(context, comment.created_at));
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}