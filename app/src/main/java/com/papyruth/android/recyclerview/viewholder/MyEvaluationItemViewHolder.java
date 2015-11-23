package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.R;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.picasso.ContrastColorFilterTransformation;
import com.papyruth.utils.view.DateTimeUtil;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyEvaluationItemViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.my_evaluation_item_lecture)           protected TextView mLecture;
    @InjectView(R.id.my_evaluation_item_timestamp)         protected TextView mTimestamp;
    @InjectView(R.id.my_evaluation_item_body)              protected TextView mBody;
    @InjectView(R.id.my_evaluation_item_overall_ratingbar) protected RatingBar mRatingBarOverall;
    @InjectView(R.id.my_evaluation_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @InjectView(R.id.my_evaluation_item_up_vote_count)     protected TextView mVoteUpCount;
    @InjectView(R.id.my_evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @InjectView(R.id.my_evaluation_item_down_vote_count)   protected TextView mVoteDownCount;
    @InjectView(R.id.my_evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @InjectView(R.id.my_evaluation_item_comment_count)     protected TextView mCommentCount;
    private VoteStatus mVoteStatus;
    private final Context mContext;
    private final Resources mResources;
    public enum VoteStatus {
        UP, DOWN, NONE
    }
    public MyEvaluationItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        mLecture.setText(evaluation.lecture_name);
        mTimestamp.setText(DateTimeUtil.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        mBody.setText(evaluation.body);

        final int colorOverall = mResources.getColor(pointInRange(evaluation.point_overall)? ( evaluation.point_overall>=8?R.color.point_high:R.color.point_low ) : R.color.point_none);
        for(int i = 0; i < 3; i ++) ((LayerDrawable) mRatingBarOverall.getProgressDrawable()).getDrawable(i).setColorFilter(colorOverall, PorterDuff.Mode.SRC_ATOP);
        mRatingBarOverall.setRating(pointInRange(evaluation.point_overall) ? (float) evaluation.point_overall / 2f : 5.0f);

        if(evaluation.request_user_vote == null) setVoteStatus(VoteStatus.NONE);
        else if(evaluation.request_user_vote == 1) setVoteStatus(VoteStatus.UP);
        else setVoteStatus(VoteStatus.DOWN);
        setVoteCount(evaluation.up_vote_count, evaluation.down_vote_count);
        Picasso.with(mContext).load(R.drawable.ic_light_comment).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }

    private void setVoteStatus(VoteStatus newStatus) {
        mVoteStatus = newStatus;
        Picasso.with(mContext).load(R.drawable.ic_light_vote_up).transform(new ContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none))).into(mVoteUpIcon);
        mVoteUpCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none));
        Picasso.with(mContext).load(R.drawable.ic_light_vote_down).transform(new ContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none))).into(mVoteDownIcon);
        mVoteDownCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none));
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        mVoteUpCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        mVoteDownCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }

    private boolean pointInRange(Integer point) {
        return point!=null && point >= 0 && point <= 10;
    }
}