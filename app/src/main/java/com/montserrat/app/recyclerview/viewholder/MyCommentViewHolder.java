package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.MyCommentData;
import com.montserrat.utils.support.picasso.ContrastColorFilterTransformation;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyCommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
    @InjectView(R.id.evaluation_item_comment) protected RelativeLayout commentContainer;
    private RecyclerViewItemClickListener itemClickListener;
    private final Context context;
    private CompositeSubscription subscription;
    public MyCommentViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.context = itemView.getContext();
        itemView.setOnClickListener(this);
        this.itemClickListener = listener;
        this.subscription = new CompositeSubscription();
    }

    public void bind(MyCommentData comment) {
        this.body.setText(comment.body);
        this.timestamp.setText(DateTimeUtil.timestamp(comment.created_at, AppConst.DateFormat.DATE_AND_TIME));

        commentContainer.setVisibility(View.GONE);
        this.professor.setText(comment.professor_name);
        this.lecture.setText(comment.lecture_name);

        //Temp text. API not supported.
        comment.category = this.itemView.getResources().getString(R.string.category_major);

        this.category.setText(comment.category);
        this.category.setTextColor(context.getResources().getColor(R.color.colorchip_green_highlight));

        Picasso.with(context).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(context.getResources().getColor(R.color.inactive))).into(this.upIcon);
        Picasso.with(context).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(context.getResources().getColor(R.color.inactive))).into(this.downIcon);
        this.setVoteCount(comment.up_vote_count, comment.down_vote_count);
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