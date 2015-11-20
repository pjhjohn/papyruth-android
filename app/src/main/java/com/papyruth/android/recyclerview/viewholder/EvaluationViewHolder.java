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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
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
    @InjectView(R.id.evaluation_lecture)                    protected TextView mLecture;
    @InjectView(R.id.evaluation_category)                   protected TextView mCategory;
    @InjectView(R.id.evaluation_professor)                  protected TextView mProfessor;
    @InjectView(R.id.evaluation_avatar)                     protected ImageView mAvatar;
    @InjectView(R.id.evaluation_nickname)                   protected TextView mNickname;
    @InjectView(R.id.evaluation_timestamp)                  protected TextView mTimestamp;
    @InjectView(R.id.evaluation_overall_label)              protected TextView mLabelOverall;
    @InjectView(R.id.evaluation_overall_point)              protected RobotoTextView mPointOverall;
    @InjectView(R.id.evaluation_overall_ratingbar)          protected RatingBar mRatingBarOverall;
    @InjectView(R.id.evaluation_clarity_label)              protected TextView mLabelClarity;
    @InjectView(R.id.evaluation_clarity_point)              protected RobotoTextView mPointClarity;
    @InjectView(R.id.evaluation_clarity_postfix)            protected RobotoTextView mPostfixClarity;
    @InjectView(R.id.evaluation_easiness_label)             protected TextView mLabelEasiness;
    @InjectView(R.id.evaluation_easiness_point)             protected RobotoTextView mPointEasiness;
    @InjectView(R.id.evaluation_easiness_postfix)           protected RobotoTextView mPostfixEasiness;
    @InjectView(R.id.evaluation_gpa_satisfaction_label)     protected TextView mLabelGpaSatisfaction;
    @InjectView(R.id.evaluation_gpa_satisfaction_point)     protected RobotoTextView mPointGpaSatisfaction;
    @InjectView(R.id.evaluation_gpa_satisfaction_postfix)   protected RobotoTextView mPostfixGpaSatisfaction;
    @InjectView(R.id.evaluation_body)                       protected TextView mBody;
    @InjectView(R.id.evaluation_hashtags)                   protected LinearLayout mHashtags;
    @InjectView(R.id.evaluation_up_vote_icon)               protected ImageView mVoteUpIcon;
    @InjectView(R.id.evaluation_up_vote_count)              protected RobotoTextView mVoteUpCount;
    @InjectView(R.id.evaluation_down_vote_icon)             protected ImageView mVoteDownIcon;
    @InjectView(R.id.evaluation_down_vote_count)            protected RobotoTextView mVoteDownCount;
    @InjectView(R.id.evaluation_edit)                       protected ImageView mEdit;
    @InjectView(R.id.hr_shadow)                             protected FrameLayout mShadow;
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
        mNickname.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelClarity.setPaintFlags(mLabelClarity.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelEasiness.setPaintFlags(mLabelEasiness.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelGpaSatisfaction.setPaintFlags(mLabelGpaSatisfaction.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mVoteUpIcon.setOnClickListener(this);
        mVoteUpCount.setOnClickListener(this);
        mVoteDownIcon.setOnClickListener(this);
        mVoteDownCount.setOnClickListener(this);
        setVoteStatus(VoteStatus.NONE);
    }

    public void bind(Evaluation evaluation, View.OnClickListener listener){
        mEdit.setOnClickListener(listener);
        mLecture.setOnClickListener(listener);
        bind(evaluation);
    }

    public void bind(Evaluation evaluation) {
        mEvaluationId = evaluation.getId();
        mLecture.setText(evaluation.getLectureName());
        Picasso.with(mContext).load(R.drawable.ic_light_edit).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mEdit);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), evaluation.getProfessorName(), " " + mResources.getString(R.string.professor_postfix))));
        setCategoryProfessorColor(mCategory, mProfessor, evaluation.getCategory());
        Picasso.with(mContext).load(evaluation.getAvatarUrl()).transform(new CircleTransformation()).into(mAvatar);
        mNickname.setText(evaluation.getUserNickname());
        mTimestamp.setText(DateTimeUtil.timestamp(evaluation.getCreatedAt(), AppConst.DateFormat.SIMPLE));

        mLabelOverall.setText(R.string.label_point_overall);
        setPointRating(mLabelOverall, mRatingBarOverall, mPointOverall, evaluation.getPointOverall());
        mLabelClarity.setText(R.string.label_point_clarity);
        setPointProgress(mLabelClarity, mPointClarity, mPostfixClarity, evaluation.getPointClarity());
        mLabelEasiness.setText(R.string.label_point_easiness);
        setPointProgress(mLabelEasiness, mPointEasiness, mPostfixEasiness, evaluation.getPointEasiness());
        mLabelGpaSatisfaction.setText(R.string.label_point_gpa_satisfaction);
        setPointProgress(mLabelGpaSatisfaction, mPointGpaSatisfaction, mPostfixGpaSatisfaction, evaluation.getPointGpaSatisfaction());

        mBody.setText(evaluation.getBody());
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

        if(evaluation.getRequestUserVote() == null) setVoteStatus(VoteStatus.NONE);
        else if(evaluation.getRequestUserVote() == 1) setVoteStatus(VoteStatus.UP);
        else setVoteStatus(VoteStatus.DOWN);

        setVoteCount(evaluation.getUpVoteCount(), evaluation.getDownVoteCount());
        mEdit.setVisibility(User.getInstance().getId() != null && User.getInstance().getId().equals(evaluation.getUserId()) ? View.VISIBLE : View.GONE);
        mShadow.setVisibility(evaluation.getCommentCount() <= 0? View.GONE : View.VISIBLE);
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

    /* TODO : Better response based on category value */
    private void setCategoryProfessorColor(TextView category, TextView professor, String value) {
        int color;
        if(value == null || value.isEmpty()) {
            color = mResources.getColor(R.color.lecture_type_etc);
            category.setText(R.string.lecture_type_etc);
        } else {
            color = mResources.getColor(R.color.lecture_type_major);
            category.setText(R.string.lecture_type_major);
        }
        category.setTextColor(color);
        category.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        professor.setTextColor(color);
    }

    private void setPointRating(TextView label, RatingBar ratingbar, TextView point, Integer value) {
        final int pointColor = mResources.getColor(pointInRange(value)? ( value>=8?R.color.point_high:R.color.point_low ) : R.color.point_none);
        label.setTextColor(pointColor);
        point.setTextColor(pointColor);
        point.setText(pointInRange(value) ? (value >= 10 ? "10" : String.format("%d.0", value)) : "N/A");
        for(int i = 0; i < 3; i ++) ((LayerDrawable) ratingbar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        ratingbar.setRating(pointInRange(value) ? (float) value / 2f : 5.0f);
    }

    private void setPointProgress(TextView label, TextView point, TextView postfix, Integer value) {
        if(!pointInRange(value)) {
            label.setTextColor(mResources.getColor(R.color.point_none));
            point.setTextColor(mResources.getColor(R.color.point_none));
            postfix.setTextColor(mResources.getColor(R.color.point_none));
            point.setText("N/A");
        } else point.setText(String.valueOf(value));
    }

    private boolean pointInRange(Integer point) {
        return point!=null && point >= 0 && point <= 10;
    }
}
