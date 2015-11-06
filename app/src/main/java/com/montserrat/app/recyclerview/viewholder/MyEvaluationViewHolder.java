package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.picasso.ContrastColorFilterTransformation;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyEvaluationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.my_written_body) protected TextView body;
    @InjectView(R.id.my_written_lecture) protected TextView lecture;
    @InjectView(R.id.my_written_professor) protected TextView professor;
    @InjectView(R.id.my_written_timestamp) protected TextView timestamp;
    @InjectView(R.id.my_written_category) protected TextView category;
    @InjectView (R.id.evaluation_item_up_vote_icon) protected ImageView upIcon;
    @InjectView (R.id.evaluation_item_up_vote_count) protected TextView upCount;
    @InjectView (R.id.evaluation_item_down_vote_icon) protected ImageView downIcon;
    @InjectView (R.id.evaluation_item_down_vote_count) protected TextView downCount;
    @InjectView (R.id.evaluation_item_comment_icon) protected ImageView commentIcon;
    @InjectView (R.id.evaluation_item_comment_count) protected TextView commentCount;

    private RecyclerViewItemClickListener itemClickListener;
    public MyEvaluationViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
    }

    public void bind(EvaluationData evaluation) {
        final Context context = this.itemView.getContext();
        this.lecture.setText(evaluation.lecture_name);
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), evaluation.professor_name, context.getResources().getString(R.string.professor_postfix))));
        this.body.setText(evaluation.body);
        this.timestamp.setText(DateTimeUtil.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        this.category.setTextColor(itemView.getContext().getResources().getColor(R.color.colorchip_green_highlight));
        this.category.setText(R.string.lecture_type_major);

        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.upIcon);
        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.downIcon);
        this.setVoteCount(evaluation.up_vote_count, evaluation.down_vote_count);

        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_comment_16dp).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.commentIcon);
        this.commentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        this.upCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        this.downCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }
}