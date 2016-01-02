package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.helper.VoteHelper;
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
    @Bind(R.id.evaluation_item_up_vote_count)     protected RobotoTextView mVoteUpCount;
    @Bind(R.id.evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @Bind(R.id.evaluation_item_down_vote_count)   protected RobotoTextView mVoteDownCount;
    @Bind(R.id.evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @Bind(R.id.evaluation_item_comment_count)     protected RobotoTextView mCommentCount;
    @Bind(R.id.material_progress_medium)          protected View mProgressbar;
    private Integer mEvaluationId;
    private final Context mContext;
    private final Resources mResources;


    public EvaluationItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public void bind(EvaluationData evaluation) {
        mProgressbar.setVisibility(View.VISIBLE);
        mEvaluationId = evaluation.id;
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mNickname.setText(evaluation.user_nickname);
        mTimestamp.setText(DateTimeHelper.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        mLabelOverall.setText(R.string.label_point_overall);
        VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, evaluation);
        Picasso.with(mContext).load(R.drawable.ic_comment_24dp).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.icon_skew_dark))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));

        if(evaluation.isValidData()) {
            mBody.setText(evaluation.body);
            PointHelper.applyRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, evaluation.point_overall);
            mHashtags.setText(Hashtag.plainString(evaluation.hashtags));
        }else{
            String bodyAlert = String.format(
                mContext.getResources().getString(R.string.invalid_evaluation)
                    , User.getInstance().needEmailConfirmed() ? mContext.getString(R.string.invalid_evaluation_cause_normal_email)
                        : User.getInstance().needMoreEvaluation() ? mContext.getString(R.string.invalid_evaluation_cause_mandantory)
                        : mContext.getString(R.string.invalid_evaluation_cause_university_email)
            );
            mBody.setTextColor(mContext.getResources().getColor(R.color.colorchip_red));
            mBody.setText(bodyAlert);
            mPointOverall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            mPointOverall.setText("비공개");
            for(int i = 0; i < 3; i++) ((LayerDrawable) mRatingBarOverall.getProgressDrawable()).getDrawable(i).setColorFilter(mContext.getResources().getColor(R.color.white_60p), PorterDuff.Mode.SRC_ATOP);
            mRatingBarOverall.setRating(10);
        }
        AnimatorHelper.FADE_OUT(mProgressbar).start();
    }
}