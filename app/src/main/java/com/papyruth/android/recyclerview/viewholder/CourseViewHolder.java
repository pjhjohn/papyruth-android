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
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.course_professor_image)                protected ImageView mProfessorImage;
    @InjectView(R.id.course_lecture)                        protected TextView mLecture;
    @InjectView(R.id.course_bookmark)                       protected ImageView mBookmark;
    @InjectView(R.id.course_category)                       protected TextView mCategory;
    @InjectView(R.id.course_professor)                      protected TextView mProfessor;
    @InjectView(R.id.course_overall_label)                  protected TextView mLabelOverall;
    @InjectView(R.id.course_overall_ratingbar)              protected RatingBar mRatingBarOverall;
    @InjectView(R.id.course_overall_point)                  protected TextView mPointOverall;
    @InjectView(R.id.course_clarity_label)                  protected TextView mLabelClarity;
    @InjectView(R.id.course_clarity_progressbar)            protected ProgressBar mProgressBarClarity;
    @InjectView(R.id.course_clarity_point)                  protected TextView mPointClarity;
    @InjectView(R.id.course_easiness_label)                 protected TextView mLabelEasiness;
    @InjectView(R.id.course_easiness_progressbar)           protected ProgressBar mProgressBarEasiness;
    @InjectView(R.id.course_easiness_point)                 protected TextView mPointEasiness;
    @InjectView(R.id.course_gpa_satisfaction_label)         protected TextView mLabelGpaSatisfaction;
    @InjectView(R.id.course_gpa_satisfaction_progressbar)   protected ProgressBar mProgressBarGpaSatisfaction;
    @InjectView(R.id.course_gpa_satisfaction_point)         protected TextView mPointGpaSatisfaction;
    @InjectView(R.id.course_hashtags)                       protected TextView mHashtags;
    @InjectView(R.id.course_evaluation_icon)                protected ImageView mEvaluationIcon;
    @InjectView(R.id.course_evaluation_count)               protected TextView mEvaluationCount;
    private CompositeSubscription mCompositeSubscription;
    private final Context mContext;
    private final Resources mResources;
    private final int mColorInactive;
    public CourseViewHolder(View view) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCompositeSubscription = new CompositeSubscription();
        mColorInactive = mResources.getColor(R.color.inactive);
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelClarity.setPaintFlags(mLabelClarity.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelEasiness.setPaintFlags(mLabelEasiness.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelGpaSatisfaction.setPaintFlags(mLabelGpaSatisfaction.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    private void setRatingBarColor(RatingBar rating, int color) {
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void setPointRating(TextView prefix, RatingBar rating, TextView text, Integer point_sum, Integer count) {
        Float rating_value = count == null || count <= 0 ? null : point_sum == null ? null : (float)point_sum / (float)count / 2f;
        if(rating_value == null || rating_value < 0) {
            prefix.setTextColor(mColorInactive);
            setRatingBarColor(rating, mColorInactive);
            text.setTextColor(mColorInactive);
            text.setText("N/A");
        } else {
            final int pointColor = mResources.getColor(rating_value >= 8 ? R.color.point_high : R.color.point_low);
            prefix.setTextColor(pointColor);
            setRatingBarColor(rating, pointColor);
            text.setTextColor(pointColor);
            text.setText(rating_value >= 10 ? "10" : String.format("%.1f", rating_value));
        } rating.setRating(rating_value == null || rating_value < 0 ? 5.0f : rating_value);
    }

    private void setPointProgress(TextView prefix, ProgressBar progress, TextView text, Integer point_sum, Integer count) {
        Integer progress_value = count == null || count <= 0 ? null : point_sum == null ? null : (int)(((float)point_sum / (float)count) * 10);
        if(progress_value == null || progress_value < 0) {
            prefix.setTextColor(mColorInactive);
            progress.setProgressDrawable(new ColorDrawable(mColorInactive));
            progress.setProgress(100);
            text.setTextColor(mColorInactive);
            text.setText("N/A");
        } else {
            progress.setProgress(progress_value);
            text.setText(progress_value >= 100 ? "10" : String.format("%d.%d", progress_value/10, progress_value%10));
        }
    }

    public void bind(Course course) {
        final Integer count = course.getEvaluationCount();
        setCategoryProfessorColor(mCategory, mProfessor, course.getCategory());
        mCategory.setText(mContext.getString(R.string.category_major)); // TODO -> evaluation.category
        mLecture.setText(course.getName());
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), course.getProfessorName(), mResources.getString(R.string.professor_postfix))));
        Picasso.with(mContext).load(course.getProfessorPhotoUrl()).transform(new CircleTransformation()).into(mProfessorImage);
        mLabelOverall.setText(R.string.label_point_overall_average);
        setPointRating(mLabelOverall, mRatingBarOverall, mPointOverall, course.getPointOverall(), count);
        mLabelClarity.setText(R.string.label_point_clarity);
        setPointProgress(mLabelClarity, mProgressBarClarity, mPointClarity, course.getPointClarity(), count);
        mLabelGpaSatisfaction.setText(R.string.label_point_gpa_satisfaction);
        setPointProgress(mLabelGpaSatisfaction, mProgressBarGpaSatisfaction, mPointGpaSatisfaction, course.getPointGpaSatisfaction(), count);
        mLabelEasiness.setText(R.string.label_point_easiness);
        setPointProgress(mLabelEasiness, mProgressBarEasiness, mPointEasiness, course.getPointEasiness(), count);

        this.mHashtags.setText(Hashtag.getHashtag(course.getHashtags()));

        Picasso.with(mContext).load(R.drawable.ic_light_evaluation_count).transform(new SkewContrastColorFilterTransformation(mColorInactive)).into(mEvaluationIcon);
        mEvaluationCount.setText(String.valueOf(count == null ? 0 : String.valueOf(count)));
        Picasso.with(mContext).load(R.drawable.ic_light_bookmark).transform(new ColorFilterTransformation(mResources.getColor(course.getIsFavorite() ? R.color.active : R.color.inactive))).into(mBookmark);
        mBookmark.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.course_bookmark) setFavorite(!Course.getInstance().getIsFavorite());
    }

    private void setFavorite(boolean favorite){
        mCompositeSubscription.add(
            Api.papyruth().post_course_favorite(User.getInstance().getAccessToken(), Course.getInstance().getId(), favorite)
                .filter(response -> response.success)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        Course.getInstance().setIsFavorite(favorite);
                        Picasso.with(mContext).load(R.drawable.ic_light_bookmark)
                            .transform(new ColorFilterTransformation(mResources.getColor(favorite ? R.color.active : R.color.inactive)))
                            .into(mBookmark);
                    }, Throwable::printStackTrace
                )
        );
    }

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
}
