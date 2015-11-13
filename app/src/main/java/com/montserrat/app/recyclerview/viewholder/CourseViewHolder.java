package com.montserrat.app.recyclerview.viewholder;

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
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.Hashtag;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.course_category)                        protected TextView mCategory;
    @InjectView(R.id.course_lecture)                         protected TextView mLecture;
    @InjectView(R.id.course_professor)                       protected TextView mProfessor;
    @InjectView(R.id.course_professor_image)                 protected ImageView mProfessorImage;
    @InjectView(R.id.course_point_overall_prefix)            protected TextView mPointOverallPrefix;
    @InjectView(R.id.course_point_overall_text)              protected TextView mPointOverallText;
    @InjectView(R.id.course_point_overall_star)              protected RatingBar mPointOverallStar;
    @InjectView(R.id.course_point_clarity_prefix)            protected TextView mPointClarityPrefix;
    @InjectView(R.id.course_point_clarity_text)              protected TextView mPointClarityText;
    @InjectView(R.id.course_point_clarity_progress)          protected ProgressBar mPointClarityProgress;
    @InjectView(R.id.course_point_gpa_satisfaction_prefix)   protected TextView mPointGpaSatisfactionPrefix;
    @InjectView(R.id.course_point_gpa_satisfaction_text)     protected TextView mPointGpaSatisfactionText;
    @InjectView(R.id.course_point_gpa_satisfaction_progress) protected ProgressBar mPointGpaSatisfactionProgress;
    @InjectView(R.id.course_point_easiness_prefix)           protected TextView mPointEasinessPrefix;
    @InjectView(R.id.course_point_easiness_text)             protected TextView mPointEasinessText;
    @InjectView(R.id.course_point_easiness_progress)         protected ProgressBar mPointEasinessProgress;
    @InjectView(R.id.course_hashtags)                        protected LinearLayout mHashtags;
    @InjectView(R.id.course_evaluator_icon)                  protected ImageView mEvaluatorIcon;
    @InjectView(R.id.course_evaluator_count)                 protected TextView mEvaluatorCount;
    @InjectView(R.id.course_bookmark)                        protected ImageView mBookmark;
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
        mCategory.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
        mCompositeSubscription = new CompositeSubscription();
        mColorInactive = mResources.getColor(R.color.inactive);
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
        mCategory.setText(mContext.getString(R.string.category_major)); // TODO -> evaluation.category
        mLecture.setText(course.getName());
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), course.getProfessorName(), mResources.getString(R.string.professor_postfix))));
        Picasso.with(mContext).load(course.getProfessorPhotoUrl()).transform(new CircleTransformation()).into(mProfessorImage);
        mPointOverallPrefix.setText(R.string.label_point_overall);
        setPointRating(mPointOverallPrefix, mPointOverallStar, mPointOverallText, course.getPointOverall(), count);
        mPointClarityPrefix.setText(R.string.label_point_clarity);
        setPointProgress(mPointClarityPrefix, mPointClarityProgress, mPointClarityText, course.getPointClarity(), count);
        mPointGpaSatisfactionPrefix.setText(R.string.label_point_gpa_satisfaction);
        setPointProgress(mPointGpaSatisfactionPrefix, mPointGpaSatisfactionProgress, mPointGpaSatisfactionText, course.getPointGpaSatisfaction(), count);
        mPointEasinessPrefix.setText(R.string.label_point_easiness);
        setPointProgress(mPointEasinessPrefix, mPointEasinessProgress, mPointEasinessText, course.getPointEasiness(), count);
        mHashtags.removeAllViews();
        if(course.getHashtags()!=null) mHashtags.post(() -> {
            float totalWidth = 0;
            for (String hashtag : course.getHashtags()) {
                Hashtag tag = new Hashtag(mContext, hashtag);
                float width = tag.getPaint().measureText((String) tag.getText());
                if (width + totalWidth > mHashtags.getWidth()) break;
                mHashtags.addView(tag);
                totalWidth += width;
            }
        });
        Picasso.with(mContext).load(R.drawable.ic_light_people).transform(new ColorFilterTransformation(mColorInactive)).into(mEvaluatorIcon);
        mEvaluatorCount.setText(count == null || count < 0 ? "N/A" : String.valueOf(count));
        Picasso.with(mContext).load(R.drawable.ic_light_bookmark)
            .transform(new ColorFilterTransformation(mResources.getColor(course.getIsFavorite() ? R.color.active : R.color.inactive)))
            .into(mBookmark);
        mBookmark.setOnClickListener(this);
        Timber.d("favor %s", Course.getInstance().getIsFavorite());
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
                    }, error -> {
                        error.printStackTrace();
                    }
                )
        );
    }
}
