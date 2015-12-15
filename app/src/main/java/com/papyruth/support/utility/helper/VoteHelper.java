package com.papyruth.support.utility.helper;

import android.content.Context;
import android.widget.ImageView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.R;
import com.papyruth.android.model.CommentData;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.MyCommentData;
import com.papyruth.android.model.response.VoteCountResponse;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.squareup.picasso.Picasso;

/**
 * Created by pjhjohn on 2015-12-10.
 */
public class VoteHelper {
    public enum VoteStatus {
        UP, DOWN, NONE
    }

    /* MyEvaluationItemViewHolder */
    public static VoteStatus applyStatus(Context context, ImageView upVoteIcon, RobotoTextView upVoteCount, ImageView downVoteIcon, RobotoTextView downVoteCount, EvaluationData evaluation) {
        final VoteStatus status = VoteHelper.applyStatus(context, upVoteIcon, upVoteCount, downVoteIcon, downVoteCount, evaluation.request_user_vote == null ? VoteStatus.NONE : ( evaluation.request_user_vote == 1 ? VoteStatus.UP : VoteStatus.DOWN ));
        VoteHelper.applyCount(upVoteCount, downVoteCount, evaluation.up_vote_count, evaluation.down_vote_count);
        return status;
    }

    /* EvaluationViewHolder */
    public static VoteStatus applyStatus(Context context, ImageView upVoteIcon, RobotoTextView upVoteCount, ImageView downVoteIcon, RobotoTextView downVoteCount, Evaluation evaluation) {
        final VoteStatus status = VoteHelper.applyStatus(context, upVoteIcon, upVoteCount, downVoteIcon, downVoteCount, evaluation.getRequestUserVote() == null ? VoteStatus.NONE : ( evaluation.getRequestUserVote() == 1 ? VoteStatus.UP : VoteStatus.DOWN ));
        VoteHelper.applyCount(upVoteCount, downVoteCount, evaluation.getUpVoteCount(), evaluation.getDownVoteCount());
        return status;
    }

    /* MyCommentItemViewHolder */
    public static VoteStatus applyStatus(Context context, ImageView upVoteIcon, RobotoTextView upVoteCount, ImageView downVoteIcon, RobotoTextView downVoteCount, MyCommentData comment) {
        final VoteStatus status = VoteHelper.applyStatus(context, upVoteIcon, upVoteCount, downVoteIcon, downVoteCount, comment.request_user_vote == null ? VoteStatus.NONE : ( comment.request_user_vote == 1 ? VoteStatus.UP : VoteStatus.DOWN ));
        VoteHelper.applyCount(upVoteCount, downVoteCount, comment.up_vote_count, comment.down_vote_count);
        return status;
    }

    /* CommentItemViewHolder */
    public static VoteStatus applyStatus(Context context, ImageView upVoteIcon, RobotoTextView upVoteCount, ImageView downVoteIcon, RobotoTextView downVoteCount, CommentData comment) {
        final VoteStatus status = VoteHelper.applyStatus(context, upVoteIcon, upVoteCount, downVoteIcon, downVoteCount, comment.request_user_vote == null ? VoteStatus.NONE : ( comment.request_user_vote == 1 ? VoteStatus.UP : VoteStatus.DOWN ));
        VoteHelper.applyCount(upVoteCount, downVoteCount, comment.up_vote_count, comment.down_vote_count);
        return status;
    }

    public static VoteStatus applyStatus(Context context, ImageView upVoteIcon, RobotoTextView upVoteCount, ImageView downVoteIcon, RobotoTextView downVoteCount, VoteStatus newStatus, VoteCountResponse voteResponse) {
        final VoteStatus status = VoteHelper.applyStatus(context, upVoteIcon, upVoteCount, downVoteIcon, downVoteCount, newStatus);
        VoteHelper.applyCount(upVoteCount, downVoteCount, voteResponse.up_vote_count, voteResponse.down_vote_count);
        return status;
    }

    public static VoteStatus applyStatus(Context context, ImageView upVoteIcon, RobotoTextView upVoteCount, ImageView downVoteIcon, RobotoTextView downVoteCount, VoteStatus newStatus) {
        Picasso.with(context).load(R.drawable.ic_vote_up_24dp).transform(new SkewContrastColorFilterTransformation(context.getResources().getColor(newStatus == VoteStatus.UP? R.color.vote_up : R.color.vote_none))).into(upVoteIcon);
        upVoteCount.setTextColor(context.getResources().getColor(newStatus == VoteStatus.UP? R.color.vote_up : R.color.vote_none));
        Picasso.with(context).load(R.drawable.ic_vote_down_24dp).transform(new SkewContrastColorFilterTransformation(context.getResources().getColor(newStatus == VoteStatus.DOWN? R.color.vote_down : R.color.vote_none))).into(downVoteIcon);
        downVoteCount.setTextColor(context.getResources().getColor(newStatus == VoteStatus.DOWN? R.color.vote_down : R.color.vote_none));
        return newStatus;
    }
    private static void applyCount(RobotoTextView upVoteCount, RobotoTextView downVoteCount, Integer upCount, Integer downCount) {
        upVoteCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        downVoteCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }
}
