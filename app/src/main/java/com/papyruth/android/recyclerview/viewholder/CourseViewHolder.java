package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.helper.CategoryHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @Bind(R.id.course_professor_image)                protected ImageView mProfessorImage;
    @Bind(R.id.course_lecture)                        protected TextView mLecture;
    @Bind(R.id.course_favorite)                       protected ImageView mFavorite;
    @Bind(R.id.course_category)                       protected TextView mCategory;
    @Bind(R.id.course_professor)                      protected TextView mProfessor;
    @Bind(R.id.course_overall_label)                  protected TextView mLabelOverall;
    @Bind(R.id.course_overall_ratingbar)              protected RatingBar mRatingBarOverall;
    @Bind(R.id.course_overall_point)                  protected TextView mPointOverall;
    @Bind(R.id.course_clarity_label)                  protected TextView mLabelClarity;
    @Bind(R.id.course_clarity_progressbar)            protected ProgressBar mProgressBarClarity;
    @Bind(R.id.course_clarity_point)                  protected TextView mPointClarity;
    @Bind(R.id.course_easiness_label)                 protected TextView mLabelEasiness;
    @Bind(R.id.course_easiness_progressbar)           protected ProgressBar mProgressBarEasiness;
    @Bind(R.id.course_easiness_point)                 protected TextView mPointEasiness;
    @Bind(R.id.course_gpa_satisfaction_label)         protected TextView mLabelGpaSatisfaction;
    @Bind(R.id.course_gpa_satisfaction_progressbar)   protected ProgressBar mProgressBarGpaSatisfaction;
    @Bind(R.id.course_gpa_satisfaction_point)         protected TextView mPointGpaSatisfaction;
    @Bind(R.id.course_hashtags)                       protected TextView mHashtags;
    @Bind(R.id.course_statistics)                     protected LinearLayout mStatistics;
    @Bind(R.id.course_evaluation_icon)                protected ImageView mEvaluationIcon;
    @Bind(R.id.course_evaluation_count)               protected TextView mEvaluationCount;
    @Bind(R.id.course_no_evaluations_message)         protected LinearLayout mNoEvaluationsMessage;
    @Bind(R.id.material_progress_medium)              protected View mProgressbar;
    private CompositeSubscription mCompositeSubscription;
    private final Context mContext;
    private final Resources mResources;
    public CourseViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCompositeSubscription = new CompositeSubscription();
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelClarity.setPaintFlags(mLabelClarity.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelEasiness.setPaintFlags(mLabelEasiness.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelGpaSatisfaction.setPaintFlags(mLabelGpaSatisfaction.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public void bind(Course course) {
        if(course.needToUpdateData()) {
            AnimatorHelper.FADE_IN(mProgressbar).start();
        } else {
            final Integer count = course.getEvaluationCount();
            Picasso.with(mContext).load(R.drawable.ic_favorite_32dp).transform(new ColorFilterTransformation(mResources.getColor(course.getIsFavorite() ? R.color.active : R.color.inactive))).into(mFavorite);
            mFavorite.setOnClickListener(this);
            CategoryHelper.assignColor(mContext, mCategory, mProfessor, course.getCategory());
            mLecture.setText(course.getName());
            mProfessor.setText(String.format("%s%s %s", mResources.getString(R.string.professor_prefix), course.getProfessorName(), mResources.getString(R.string.professor_postfix)));
            Picasso.with(mContext).load(course.getProfessorPhotoUrl()).transform(new CircleTransformation()).into(mProfessorImage);
            mLabelOverall.setText(R.string.course_label_point_overall);
            mLabelClarity.setText(R.string.course_label_point_clarity);
            mLabelGpaSatisfaction.setText(R.string.course_label_point_gpa_satisfaction);
            mLabelEasiness.setText(R.string.course_label_point_easiness);
            if(User.getInstance().emailConfirmationRequired() || User.getInstance().mandatoryEvaluationsRequired()) {
                PointHelper.applyRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, 0, 0);
                PointHelper.applyProgress(mContext, mLabelClarity, mProgressBarClarity, mPointClarity, 0, 0);
                PointHelper.applyProgress(mContext, mLabelGpaSatisfaction, mProgressBarGpaSatisfaction, mPointGpaSatisfaction, 0, 0);
                PointHelper.applyProgress(mContext, mLabelEasiness, mProgressBarEasiness, mPointEasiness, 0, 0);
                mPointOverall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                mPointClarity.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                mPointEasiness.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                mPointGpaSatisfaction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                mPointOverall.setText(mContext.getString(R.string.invalid_point_closed));
                mPointClarity.setText(mContext.getString(R.string.invalid_point_closed));
                mPointEasiness.setText(mContext.getString(R.string.invalid_point_closed));
                mPointGpaSatisfaction.setText(mContext.getString(R.string.invalid_point_closed));
            }else {
                mPointOverall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
                mPointClarity.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                mPointEasiness.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                mPointGpaSatisfaction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                PointHelper.applyRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, course.getPointOverall(), course.getEvaluationCount());
                PointHelper.applyProgress(mContext, mLabelClarity, mProgressBarClarity, mPointClarity, course.getPointClarity(), count);
                PointHelper.applyProgress(mContext, mLabelGpaSatisfaction, mProgressBarGpaSatisfaction, mPointGpaSatisfaction, course.getPointGpaSatisfaction(), count);
                PointHelper.applyProgress(mContext, mLabelEasiness, mProgressBarEasiness, mPointEasiness, course.getPointEasiness(), count);
            }
            if (count == null || count <= 0) {
                mHashtags.setVisibility(View.GONE);
                mStatistics.setVisibility(View.GONE);
                mNoEvaluationsMessage.setVisibility(View.VISIBLE);
            } else {
                mHashtags.setVisibility(View.VISIBLE);
                mStatistics.setVisibility(View.VISIBLE);
                mNoEvaluationsMessage.setVisibility(View.GONE);
                mHashtags.setText(Hashtag.plainString(course.getHashtags()));
                Picasso.with(mContext).load(R.drawable.ic_evaluation_count_24dp).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.icon_skew_dark))).into(mEvaluationIcon);
                mEvaluationCount.setText(String.valueOf(count));
            }
            AnimatorHelper.FADE_OUT(mProgressbar).start();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.course_favorite) setFavorite(!Course.getInstance().getIsFavorite());
    }

    private void setFavorite(boolean favorite) {
        mCompositeSubscription.add(
            Api.papyruth().post_course_favorite(User.getInstance().getAccessToken(), Course.getInstance().getId(), favorite)
                .filter(response -> response.success)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        Course.getInstance().setIsFavorite(favorite);
                        Picasso.with(mContext).load(R.drawable.ic_favorite_32dp)
                            .transform(new ColorFilterTransformation(mResources.getColor(favorite? R.color.active : R.color.inactive)))
                            .into(mFavorite);
                    }, Throwable::printStackTrace
                )
        );
    }
}
