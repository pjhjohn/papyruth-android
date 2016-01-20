package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.materialdialog.VotersDialog;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.helper.CategoryHelper;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.helper.VoteHelper;
import com.papyruth.support.utility.helper.VoteHelper.VoteStatus;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @Bind(R.id.evaluation_header)                     protected LinearLayout mHeader;
    @Bind(R.id.evaluation_lecture)                    protected TextView mLecture;
    @Bind(R.id.evaluation_category)                   protected TextView mCategory;
    @Bind(R.id.evaluation_professor)                  protected TextView mProfessor;
    @Bind(R.id.evaluation_avatar)                     protected ImageView mAvatar;
    @Bind(R.id.evaluation_nickname)                   protected TextView mNickname;
    @Bind(R.id.evaluation_timestamp)                  protected TextView mTimestamp;
    @Bind(R.id.evaluation_overall_label)              protected TextView mLabelOverall;
    @Bind(R.id.evaluation_overall_point)              protected RobotoTextView mPointOverall;
    @Bind(R.id.evaluation_overall_ratingbar)          protected RatingBar mRatingBarOverall;
    @Bind(R.id.evaluation_clarity_label)              protected TextView mLabelClarity;
    @Bind(R.id.evaluation_clarity_point)              protected RobotoTextView mPointClarity;
    @Bind(R.id.evaluation_clarity_postfix)            protected RobotoTextView mPostfixClarity;
    @Bind(R.id.evaluation_easiness_label)             protected TextView mLabelEasiness;
    @Bind(R.id.evaluation_easiness_point)             protected RobotoTextView mPointEasiness;
    @Bind(R.id.evaluation_easiness_postfix)           protected RobotoTextView mPostfixEasiness;
    @Bind(R.id.evaluation_gpa_satisfaction_label)     protected TextView mLabelGpaSatisfaction;
    @Bind(R.id.evaluation_gpa_satisfaction_point)     protected RobotoTextView mPointGpaSatisfaction;
    @Bind(R.id.evaluation_gpa_satisfaction_postfix)   protected RobotoTextView mPostfixGpaSatisfaction;
    @Bind(R.id.evaluation_body)                       protected TextView mBody;
    @Bind(R.id.evaluation_hashtags)                   protected TextView mHashtags;
    @Bind(R.id.evaluation_up_vote_icon)               protected ImageView mVoteUpIcon;
    @Bind(R.id.evaluation_up_vote_count)              protected RobotoTextView mVoteUpCount;
    @Bind(R.id.evaluation_down_vote_icon)             protected ImageView mVoteDownIcon;
    @Bind(R.id.evaluation_down_vote_count)            protected RobotoTextView mVoteDownCount;
    @Bind(R.id.evaluation_comment_container)          protected LinearLayout mCommentContainer;
    @Bind(R.id.evaluation_comment_count)              protected TextView mCommentCount;
    @Bind(R.id.evaluation_comment_more)               protected TextView mCommentMore;
    @Bind(R.id.material_progress_medium)              protected View mProgressbar;
    private Integer mEvaluationId;
    private VoteStatus mVoteStatus;
    private final Context mContext;
    private final Resources mResources;

    public EvaluationViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        if(listener != null) {
            mHeader.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
            mCommentContainer.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
        }
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelClarity.setPaintFlags(mLabelClarity.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelEasiness.setPaintFlags(mLabelEasiness.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelGpaSatisfaction.setPaintFlags(mLabelGpaSatisfaction.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mVoteUpIcon.setOnClickListener(this);
        mVoteUpCount.setOnClickListener(this);
        mVoteDownIcon.setOnClickListener(this);
        mVoteDownCount.setOnClickListener(this);
        mCommentCount.setPaintFlags(mCommentCount.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public void bind(Evaluation evaluation, View.OnClickListener listener){
        mLecture.setOnClickListener(listener);
        bind(evaluation, false);
    }

    public void bind(Evaluation evaluation, boolean loadMore) {
        if(evaluation.hasContents()) {
            mProgressbar.setVisibility(View.VISIBLE);
            mEvaluationId = evaluation.getId();
            mLecture.setText(evaluation.getLectureName());
            mProfessor.setText(String.format("%s%s %s", mResources.getString(R.string.professor_prefix), evaluation.getProfessorName(), mResources.getString(R.string.professor_postfix)));
            CategoryHelper.assignColor(mContext, mCategory, mProfessor, evaluation.getCategory());
            Picasso.with(mContext).load(evaluation.getAvatarUrl()).transform(new CircleTransformation()).into(mAvatar);
            mNickname.setText(evaluation.getUserNickname());
            mTimestamp.setText(DateTimeHelper.timestamp(evaluation.getCreatedAt(), AppConst.DateFormat.DATE));
            mLabelOverall.setText(R.string.evaluation_label_point_overall);
            PointHelper.applyRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, evaluation.getPointOverall());
            mLabelClarity.setText(R.string.evaluation_label_point_clarity);
            mPostfixClarity.setText(R.string.evaluation_point_denominator);
            mPostfixClarity.setTextColor(mContext.getResources().getColor(R.color.point_clarity));
            PointHelper.applyProgress(mContext, mLabelClarity, mPointClarity, mPostfixClarity, evaluation.getPointClarity());
            mLabelEasiness.setText(R.string.evaluation_label_point_easiness);
            mPostfixEasiness.setText(R.string.evaluation_point_denominator);
            mPostfixEasiness.setTextColor(mContext.getResources().getColor(R.color.point_easiness));
            PointHelper.applyProgress(mContext, mLabelEasiness, mPointEasiness, mPostfixEasiness, evaluation.getPointEasiness());
            mLabelGpaSatisfaction.setText(R.string.evaluation_label_point_gpa_satisfaction);
            mPostfixGpaSatisfaction.setText(R.string.evaluation_point_denominator);
            mPostfixGpaSatisfaction.setTextColor(mContext.getResources().getColor(R.color.point_gpa_satisfaction));
            PointHelper.applyProgress(mContext, mLabelGpaSatisfaction, mPointGpaSatisfaction, mPostfixGpaSatisfaction, evaluation.getPointGpaSatisfaction());
            mBody.setText(evaluation.getBody());
            mHashtags.setText(Hashtag.plainString(evaluation.getHashTag()));
            mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, evaluation);
            AnimatorHelper.FADE_OUT(mProgressbar).start();
            final int commentCount = evaluation.getCommentCount();
            if(commentCount <= 0) mCommentCount.setText(mContext.getResources().getString(R.string.evaluation_no_comments));
            else mCommentCount.setText(String.format(mContext.getResources().getQuantityString(R.plurals.comments, commentCount), commentCount));
            if(loadMore) {
                mCommentMore.setVisibility(View.VISIBLE);
                mCommentMore.setText(R.string.data_load_more);
            } else mCommentMore.setVisibility(View.GONE);
        } else {
            AnimatorHelper.FADE_IN(mProgressbar).start();
        }
    }

    @Override
    public void onClick(View view) {
        if(mEvaluationId == null) return;
        switch(view.getId()) {
            case R.id.evaluation_up_vote_icon:
                if(mVoteStatus == VoteStatus.UP) Api.papyruth()
                    .delete_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.NONE, response), error ->  ErrorHandler.handle(error, this, true));
                else Api.papyruth()
                    .post_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.UP, response), error ->  ErrorHandler.handle(error, this, true));
                break;
            case R.id.evaluation_down_vote_icon:
                if(mVoteStatus == VoteStatus.DOWN) Api.papyruth()
                    .delete_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.NONE, response), error ->  ErrorHandler.handle(error, this, true));
                else Api.papyruth()
                    .post_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.DOWN, response), error ->  ErrorHandler.handle(error, this, true));
                break;
            case R.id.evaluation_up_vote_count:
            case R.id.evaluation_down_vote_count:
                Api.papyruth()
                    .get_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> VotersDialog.show(
                        view.getContext(),
                        view.getId() == R.id.evaluation_up_vote_count ? "UP" : "DOWN",
                        view.getId() == R.id.evaluation_up_vote_count ? response.up : response.down
                    ), error ->  ErrorHandler.handle(error, this, true));
                break;
            default : Timber.d("Clicked view : %s", view);
        }
    }
}
