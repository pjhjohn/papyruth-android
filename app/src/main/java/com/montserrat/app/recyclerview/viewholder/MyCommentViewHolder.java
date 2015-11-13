package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
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

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyCommentViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.evaluation_item_body)              protected TextView mBody;
    @InjectView(R.id.evaluation_item_lecture)           protected TextView mLecture;
    @InjectView(R.id.evaluation_item_professor)         protected TextView mProfessor;
    @InjectView(R.id.evaluation_item_timestamp)         protected TextView mTimestamp;
    @InjectView(R.id.evaluation_item_category)          protected TextView mCategory;
    @InjectView(R.id.evaluation_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @InjectView(R.id.evaluation_item_up_vote_count)     protected TextView mVoteUpCount;
    @InjectView(R.id.evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @InjectView(R.id.evaluation_item_down_vote_count)   protected TextView mVoteDownCount;
    @InjectView(R.id.evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @InjectView(R.id.evaluation_item_comment_count)     protected TextView mCommentCount;
    @InjectView(R.id.evaluation_item_comment)           protected RelativeLayout mComment;
    private final Context mContext;
    private final Resources mResources;
    public MyCommentViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, this.getAdapterPosition()));
    }

    public void bind(MyCommentData comment) {
        mBody.setText(comment.body);
        mLecture.setText(comment.lecture_name);
        mProfessor.setText(comment.professor_name);
        mTimestamp.setText(DateTimeUtil.timestamp(comment.created_at, AppConst.DateFormat.DATE_AND_TIME));
        mCategory.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
        mCategory.setText(comment.category);
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(mResources.getColor(R.color.inactive))).into(this.mVoteUpIcon);
        this.mVoteUpCount.setText(String.valueOf(comment.up_vote_count == null ? 0 : comment.up_vote_count));
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(mResources.getColor(R.color.inactive))).into(this.mVoteDownIcon);
        this.mVoteDownCount.setText(String.valueOf(comment.down_vote_count == null ? 0 : comment.down_vote_count));
        mComment.setVisibility(View.GONE);
    }
}