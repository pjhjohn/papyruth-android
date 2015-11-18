package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.materialdialog.VotersDialog;
import com.papyruth.utils.support.picasso.CircleTransformation;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.picasso.ContrastColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.DateTimeUtil;
import com.papyruth.utils.view.Hashtag;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.evaluation_header)                          protected RelativeLayout mHeader;
    @InjectView(R.id.evaluation_lecture)                         protected TextView mLecture;
    @InjectView(R.id.evaluation_timestamp)                       protected TextView mTimestamp;
    @InjectView(R.id.evaluation_category)                        protected TextView mCategory;
    @InjectView(R.id.evaluation_professor)                       protected TextView mProfessor;
    @InjectView(R.id.evaluation_avatar)                          protected ImageView mAvatar;
    @InjectView(R.id.evaluation_nickname)                        protected TextView mNickname;
    @InjectView(R.id.evaluation_body)                            protected TextView mBody;
    @InjectView(R.id.evaluation_point_overall_prefix)            protected TextView mPointOverallPrefix;
    @InjectView(R.id.evaluation_point_overall_text)              protected TextView mPointOverallText;
    @InjectView(R.id.evaluation_point_overall_star)              protected RatingBar mPointOverallRating;
    @InjectView(R.id.evaluation_point_clarity_prefix)            protected TextView mPointClarityPrefix;
    @InjectView(R.id.evaluation_point_clarity_text)              protected TextView mPointClarityText;
    @InjectView(R.id.evaluation_point_clarity_progress)          protected ProgressBar mPointClarityProgress;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_prefix)   protected TextView mPointGpaSatisfactionPrefix;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_text)     protected TextView mPointGpaSatisfactionText;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_progress) protected ProgressBar mPointGpaSatisfactionProgress;
    @InjectView(R.id.evaluation_point_easiness_prefix)           protected TextView mPointEasinessPrefix;
    @InjectView(R.id.evaluation_point_easiness_text)             protected TextView mPointEasinessText;
    @InjectView(R.id.evaluation_point_easiness_progress)         protected ProgressBar mPointEasinessProgress;
    @InjectView(R.id.evaluation_hashtags)                        protected LinearLayout mHashtags;
    @InjectView(R.id.evaluation_up_vote_icon)                    protected ImageView mVoteUpIcon;
    @InjectView(R.id.evaluation_up_vote_count)                   protected TextView mVoteUpCount;
    @InjectView(R.id.evaluation_down_vote_icon)                  protected ImageView mVoteDownIcon;
    @InjectView(R.id.evaluation_down_vote_count)                 protected TextView mVoteDownCount;
    @InjectView(R.id.evaluation_comment_icon)                    protected ImageView mCommentIcon;
    @InjectView(R.id.evaluation_comment_count)                   protected TextView mCommentCount;
    @InjectView(R.id.evaluation_modify)                          protected ImageView mModify;
    private Integer mEvaluationId;
    private VoteStatus mVoteStatus;
    private final Context mContext;
    private final Resources mResources;
    public enum VoteStatus {
        UP, DOWN, NONE
    }
    public EvaluationViewHolder(View view) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
        mNickname.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mVoteUpIcon.setOnClickListener(this);
        mVoteUpCount.setOnClickListener(this);
        mVoteDownIcon.setOnClickListener(this);
        mVoteDownCount.setOnClickListener(this);
        setVoteStatus(VoteStatus.NONE);
    }



    public void bind(Evaluation evaluation, View.OnClickListener listener){
        mModify.setOnClickListener(listener);
        mHeader.setOnClickListener(listener);
        bind(evaluation);
    }

    public void bind(Evaluation evaluation) {
        mEvaluationId = evaluation.getId();
        mLecture.setText(evaluation.getLectureName());
        mTimestamp.setText(DateTimeUtil.timestamp(evaluation.getCreatedAt(), AppConst.DateFormat.DATE_AND_TIME));
        mCategory.setText(mContext.getString(R.string.category_major)); // TODO -> evaluation.category
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), evaluation.getProfessorName(), " " + mResources.getString(R.string.professor_postfix))));
        Picasso.with(mContext).load(evaluation.getAvatarUrl()).transform(new CircleTransformation()).into(mAvatar);
        mNickname.setText(evaluation.getUserNickname());
        mBody.setText(evaluation.getBody());
        mPointOverallPrefix.setText(R.string.label_point_overall);
        setPointRating(mPointOverallPrefix, mPointOverallRating, mPointOverallText, evaluation.getPointOverall());
        mPointClarityPrefix.setText(R.string.label_point_clarity);
        setPointProgress(mPointClarityPrefix, mPointClarityProgress, mPointClarityText, evaluation.getPointClarity());
        mPointGpaSatisfactionPrefix.setText(R.string.label_point_gpa_satisfaction);
        setPointProgress(mPointGpaSatisfactionPrefix, mPointGpaSatisfactionProgress, mPointGpaSatisfactionText, evaluation.getPointGpaSatisfaction());
        mPointEasinessPrefix.setText(R.string.label_point_easiness);
        setPointProgress(mPointEasinessPrefix, mPointEasinessProgress, mPointEasinessText, evaluation.getPointEasiness());
        mHashtags.removeAllViews();
        if(mEvaluationId != null) Api.papyruth()
            .get_evaluation_hashtag(User.getInstance().getAccessToken(), mEvaluationId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.hashtags != null) mHashtags.post(() -> {
                    float totalWidth = 0;
                    for (String hashtag : response.hashtags) {
                        Evaluation.getInstance().addHashTag(hashtag);
                        Hashtag tag = new Hashtag(mContext, hashtag);
                        float width = tag.getPaint().measureText((String) tag.getText());
                        if (width + totalWidth > mHashtags.getWidth()) break;
                        mHashtags.addView(tag);
                        totalWidth += width;
                    }
                });
            }, error ->  ErrorHandler.throwError(error, this));

        Picasso.with(mContext).load(R.drawable.ic_light_comment_16dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.inactive))).into(mCommentIcon);
        mCommentCount.setText(evaluation.getCommentCount() == null || evaluation.getCommentCount() < 0 ? "N/A" : String.valueOf(evaluation.getCommentCount()));

        if(evaluation.getRequestUserVote() == null) setVoteStatus(VoteStatus.NONE);
        else if(evaluation.getRequestUserVote() == 1) setVoteStatus(VoteStatus.UP);
        else setVoteStatus(VoteStatus.DOWN);

        setVoteCount(evaluation.getUpVoteCount(), evaluation.getDownVoteCount());
        mModify.setVisibility(User.getInstance().getId()!=null&&User.getInstance().getId().equals(evaluation.getUserId())? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        if(mEvaluationId == null) return;
        switch(view.getId()) {
            case R.id.evaluation_up_vote_icon:
                if(mVoteStatus == VoteStatus.UP) Api.papyruth()
                    .delete_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.NONE);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.throwError(error, this));
                else Api.papyruth()
                    .post_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.UP);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.throwError(error, this));
                break;
            case R.id.evaluation_down_vote_icon:
                if(mVoteStatus == VoteStatus.DOWN) Api.papyruth()
                    .delete_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.NONE);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.throwError(error, this));
                else Api.papyruth()
                    .post_evaluation_vote(User.getInstance().getAccessToken(), mEvaluationId, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.DOWN);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.throwError(error, this));
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
                    ), error ->  ErrorHandler.throwError(error, this));
                break;
            default : Timber.d("Clicked view : %s", view);
        }
    }

    private void setVoteStatus(VoteStatus newStatus) {
        mVoteStatus = newStatus;
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none))).into(mVoteUpIcon);
        mVoteUpCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none));
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none))).into(mVoteDownIcon);
        mVoteDownCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none));
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        mVoteUpCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        mVoteDownCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }

    private void setPointRating(TextView prefix, RatingBar rating, TextView text, Integer point) {
        final int pointColor = mResources.getColor(pointInRange(point)? ( point>=8?R.color.point_high:R.color.point_low ) : R.color.point_none);
        prefix.setTextColor(pointColor);
        text.setTextColor(pointColor);
        text.setText(pointInRange(point)? ( point>=10?"10":String.format("%d", point)) : "N/A");
        for(int i = 0; i < 3; i ++) ((LayerDrawable) rating.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        rating.setRating(pointInRange(point) ? (float) point / 2f : 5.0f );
    }

    private boolean pointInRange(Integer point) {
        return point!=null && point >= 0 && point <= 10;
    }

    private void setPointProgress(TextView prefix, ProgressBar progress, TextView text, Integer point) {
        if(point == null || point < 0) {
            prefix.setTextColor(mResources.getColor(R.color.point_none));
            progress.setProgressDrawable(new ColorDrawable(mResources.getColor(R.color.point_none)));
            progress.setProgress(100);
            text.setTextColor(mResources.getColor(R.color.point_none));
            text.setText("N/A");
        } else {
            progress.setProgress(point);
            text.setText(point >= 10 ? "10" : String.format("%d.0", point));
        }
    }
}
