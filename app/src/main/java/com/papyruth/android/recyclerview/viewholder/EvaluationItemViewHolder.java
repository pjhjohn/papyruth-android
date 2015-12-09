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
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.evaluation_item_avatar)            protected ImageView mAvatar;
    @Bind(R.id.evaluation_item_nickname)          protected TextView mNickname;
    @Bind(R.id.evaluation_item_timestamp)         protected RobotoTextView mTimestamp;
    @Bind(R.id.evaluation_item_body)              protected TextView mBody;
    @Bind(R.id.evaluation_item_overall_label)     protected TextView mLabelOverall;
    @Bind(R.id.evaluation_item_overall_point)     protected RobotoTextView mPointOverall;
    @Bind(R.id.evaluation_item_overall_ratingbar) protected RatingBar mRatingBarOverall;
    @Bind(R.id.evaluation_item_hashtags)          protected TextView mHashtags;
    @Bind(R.id.evaluation_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @Bind(R.id.evaluation_item_up_vote_count)     protected TextView mVoteUpCount;
    @Bind(R.id.evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @Bind(R.id.evaluation_item_down_vote_count)   protected TextView mVoteDownCount;
    @Bind(R.id.evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @Bind(R.id.evaluation_item_comment_count)     protected TextView mCommentCount;
    @Bind(R.id.material_progress_medium)          protected View mProgressbar;
    private Integer mEvaluationId;
    private VoteStatus mVoteStatus;
    private final Context mContext;
    private final Resources mResources;
    public enum VoteStatus {
        UP, DOWN, NONE
    }

    public EvaluationItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = itemView.getContext();
        mResources = mContext.getResources();
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        itemView.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        mProgressbar.setVisibility(View.VISIBLE);

        mEvaluationId = evaluation.id;
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mTimestamp.setText(DateTimeHelper.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        mBody.setText(evaluation.body);
        mNickname.setText(evaluation.user_nickname);
        mLabelOverall.setText(R.string.label_point_overall);
        PointHelper.setPointRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, evaluation.point_overall);

        if(evaluation.isHashtagUpdated()) {
            this.mHashtags.setText(Hashtag.getHashtag(evaluation.hashtags));
            AnimatorHelper.FADE_OUT(mProgressbar).start();
        }

        if(evaluation.request_user_vote == null) setVoteStatus(VoteStatus.NONE);
        else if(evaluation.request_user_vote == 1) setVoteStatus(VoteStatus.UP);
        else setVoteStatus(VoteStatus.DOWN);
        setVoteCount(evaluation.up_vote_count, evaluation.down_vote_count);

        Picasso.with(mContext).load(R.drawable.ic_light_comment).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.inactive))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }

    private void setVoteStatus(VoteStatus newStatus) {
        mVoteStatus = newStatus;
        Picasso.with(mContext).load(R.drawable.ic_light_vote_up).transform(new SkewContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none))).into(mVoteUpIcon);
        mVoteUpCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none));
        Picasso.with(mContext).load(R.drawable.ic_light_vote_down).transform(new SkewContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none))).into(mVoteDownIcon);
        mVoteDownCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none));
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        mVoteUpCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        mVoteDownCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }
}