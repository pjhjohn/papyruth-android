package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.helper.VoteHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyEvaluationItemViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.my_evaluation_item_lecture)           protected TextView mLecture;
    @BindView(R.id.my_evaluation_item_timestamp)         protected TextView mTimestamp;
    @BindView(R.id.my_evaluation_item_body)              protected TextView mBody;
    @BindView(R.id.my_evaluation_item_overall_ratingbar) protected RatingBar mRatingBarOverall;
    @BindView(R.id.my_evaluation_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @BindView(R.id.my_evaluation_item_up_vote_count)     protected RobotoTextView mVoteUpCount;
    @BindView(R.id.my_evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @BindView(R.id.my_evaluation_item_down_vote_count)   protected RobotoTextView mVoteDownCount;
    @BindView(R.id.my_evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @BindView(R.id.my_evaluation_item_comment_count)     protected RobotoTextView mCommentCount;
    private final Context mContext;
    private final Resources mResources;

    public MyEvaluationItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        mLecture.setText(evaluation.lecture_name);
        mTimestamp.setText(DateTimeHelper.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_TIME_12HR));
        mBody.setText(evaluation.body);
        PointHelper.applyRating(mContext, mRatingBarOverall, evaluation.point_overall);
        VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, evaluation);
        Picasso.with(mContext).load(R.drawable.ic_comment_24dp).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.icon_skew_dark))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }
}
