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
import android.widget.LinearLayout;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.evaluation_item_avatar)            protected ImageView mAvatar;
    @BindView(R.id.evaluation_item_nickname)          protected TextView mNickname;
    @BindView(R.id.evaluation_item_timestamp)         protected RobotoTextView mTimestamp;
    @BindView(R.id.evaluation_item_body)              protected TextView mBody;
    @BindView(R.id.evaluation_item_overall_label)     protected TextView mLabelOverall;
    @BindView(R.id.evaluation_item_overall_point)     protected RobotoTextView mPointOverall;
    @BindView(R.id.evaluation_item_overall_ratingbar) protected RatingBar mRatingBarOverall;
    @BindView(R.id.evaluation_item_hashtags)          protected TextView mHashtags;
    @BindView(R.id.evaluation_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @BindView(R.id.evaluation_item_up_vote_count)     protected RobotoTextView mVoteUpCount;
    @BindView(R.id.evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @BindView(R.id.evaluation_item_down_vote_count)   protected RobotoTextView mVoteDownCount;
    @BindView(R.id.evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @BindView(R.id.evaluation_item_comment_count)     protected RobotoTextView mCommentCount;
    @BindView(R.id.material_progress_medium)          protected View mProgressbar;
    @BindView(R.id.evaluation_item_statistics)          protected LinearLayout mStatisticsContainer;
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
        mTimestamp.setText(DateTimeHelper.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_TIME_12HR));
        mLabelOverall.setText(R.string.evaluation_label_point_overall);
        VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, evaluation);
        Picasso.with(mContext).load(R.drawable.ic_comment_24dp).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.icon_skew_dark))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));

        if(isValidData(evaluation, User.getInstance())) {
            mBody.setTextColor(mContext.getResources().getColor(R.color.white_40p));
            mBody.setText(evaluation.body);
            PointHelper.applyRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, evaluation.point_overall);
            mHashtags.setText(Hashtag.plainString(evaluation.hashtags));
            mPointOverall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
            mStatisticsContainer.setVisibility(View.VISIBLE);
            mHashtags.setVisibility(View.VISIBLE);
        } else {
            String messageEvaluationForbidden = String.format(mContext.getResources().getString(R.string.evaluation_forbidden),
                User.getInstance().emailConfirmationRequired()?
                    mContext.getString(R.string.evaluation_forbidden_email_confirmation_required) :
                    User.getInstance().mandatoryEvaluationsRequired()?
                        mContext.getString(R.string.evaluation_forbidden_mandatory_evaluation_required) :
                            evaluation.university_confirmation_needed != null && evaluation.university_confirmation_needed?
                                mContext.getString(R.string.evaluation_forbidden_university_confirmation_required) :
                                    mContext.getString(R.string.empty)
            );
            mBody.setTextColor(mContext.getResources().getColor(R.color.colorchip_red));
            mBody.setText(messageEvaluationForbidden);
            mPointOverall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            mPointOverall.setText(mContext.getString(R.string.evaluation_point_hidden));
            for(int i = 0; i < 3; i++) ((LayerDrawable) mRatingBarOverall.getProgressDrawable()).getDrawable(i).setColorFilter(mContext.getResources().getColor(R.color.white_60p), PorterDuff.Mode.SRC_ATOP);
            mRatingBarOverall.setRating(10);
            mStatisticsContainer.setVisibility(View.GONE);
            mHashtags.setVisibility(View.GONE);
        }
        AnimatorHelper.FADE_OUT(mProgressbar).start();
    }

    public static boolean isValidData(EvaluationData evaluation, User user){
        return evaluation.isValidData()
                && !user.emailConfirmationRequired()
                && !user.mandatoryEvaluationsRequired()
                && (!(evaluation.university_confirmation_needed != null && evaluation.university_confirmation_needed )|| User.getInstance().getUniversityConfirmed());
    }
}
