package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
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
    @Bind(R.id.course_bookmark)                       protected ImageView mBookmark;
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
    @Bind(R.id.course_evaluation_icon)                protected ImageView mEvaluationIcon;
    @Bind(R.id.course_evaluation_count)               protected TextView mEvaluationCount;
    private CompositeSubscription mCompositeSubscription;
    private final Context mContext;
    private final Resources mResources;
    private final int mColorInactive;
    public CourseViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
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
        PointHelper.setPointRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, course.getPointOverall(), course.getEvaluationCount());
        mLabelClarity.setText(R.string.label_point_clarity);
        setPointProgress(mLabelClarity, mProgressBarClarity, mPointClarity, course.getPointClarity(), count);
        mLabelGpaSatisfaction.setText(R.string.label_point_gpa_satisfaction);
        setPointProgress(mLabelGpaSatisfaction, mProgressBarGpaSatisfaction, mPointGpaSatisfaction, course.getPointGpaSatisfaction(), count);
        mLabelEasiness.setText(R.string.label_point_easiness);
        setPointProgress(mLabelEasiness, mProgressBarEasiness, mPointEasiness, course.getPointEasiness(), count);

        this.mHashtags.setText(Hashtag.getHashtag(course.getHashtags()));

        Picasso.with(mContext).load(R.drawable.ic_evaluation_count_24dp).transform(new SkewContrastColorFilterTransformation(mColorInactive)).into(mEvaluationIcon);
        mEvaluationCount.setText(String.valueOf(count == null ? 0 : String.valueOf(count)));
        Picasso.with(mContext).load(R.drawable.ic_bookmark_24dp).transform(new ColorFilterTransformation(mResources.getColor(course.getIsFavorite() ? R.color.active : R.color.inactive))).into(mBookmark);
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
                        Picasso.with(mContext).load(R.drawable.ic_bookmark_24dp)
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
