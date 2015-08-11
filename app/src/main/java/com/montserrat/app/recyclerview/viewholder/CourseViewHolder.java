package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
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

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.retrofit.RetrofitApi;
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
    @InjectView(R.id.course_category) TextView category;
    @InjectView(R.id.course_lecture) protected TextView lecture;
    @InjectView(R.id.course_professor) protected TextView professor;
    @InjectView(R.id.course_professor_image) protected ImageView professor_image;
    @InjectView(R.id.course_point_overall_prefix) protected TextView pointOverallPrefix;
    @InjectView(R.id.course_point_overall_text) protected TextView pointOverallText;
    @InjectView(R.id.course_point_overall_star) protected RatingBar pointOverallRating;
    @InjectView(R.id.course_point_clarity_prefix) protected TextView pointClarityPrefix;
    @InjectView(R.id.course_point_clarity_text) protected TextView pointClarityText;
    @InjectView(R.id.course_point_clarity_progress) protected ProgressBar pointClarityProgress;
    @InjectView(R.id.course_point_gpa_satisfaction_prefix) protected TextView pointGpaSatisfactionPrefix;
    @InjectView(R.id.course_point_gpa_satisfaction_text) protected TextView pointGpaSatisfactionText;
    @InjectView(R.id.course_point_gpa_satisfaction_progress) protected ProgressBar pointGpaSatisfactionProgress;
    @InjectView(R.id.course_point_easiness_prefix) protected TextView pointEasinessPrefix;
    @InjectView(R.id.course_point_easiness_text) protected TextView pointEasinessText;
    @InjectView(R.id.course_point_easiness_progress) protected ProgressBar pointEasinessProgress;
    @InjectView(R.id.course_hashtags) protected LinearLayout hashtags;
    @InjectView(R.id.course_evaluator_icon) protected ImageView evaluatorIcon;
    @InjectView(R.id.course_evaluator_count) protected TextView evaluatorCount;
    @InjectView(R.id.course_bookmark) protected ImageView bookmark;

    private CompositeSubscription subscription;
    public CourseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.lecture.setPaintFlags(this.lecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setPaintFlags(this.category.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setTextColor(itemView.getContext().getResources().getColor(R.color.fg_accent));
        this.subscription = new CompositeSubscription();
    }

    private void setRatingBarColor(RatingBar rating, int color) {
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void setPointRating(TextView prefix, RatingBar rating, TextView text, Integer point_sum, Integer count) {
        Float rating_value = count == null || count <= 0 ? null : point_sum == null ? null : (float)point_sum / (float)count / 2f;
        if(rating_value == null || rating_value < 0) {
            prefix.setTextColor(AppConst.COLOR_NEUTRAL);
            this.setRatingBarColor(rating, AppConst.COLOR_NEUTRAL);
            text.setTextColor(AppConst.COLOR_NEUTRAL);
            text.setText("N/A");
        } else {
            prefix.setTextColor(rating_value >= 8 ? AppConst.COLOR_POINT_HIGH : AppConst.COLOR_POINT_LOW);
            this.setRatingBarColor(rating, rating_value >= 8 ? AppConst.COLOR_POINT_HIGH : AppConst.COLOR_POINT_LOW);
            text.setTextColor(rating_value >= 8 ? AppConst.COLOR_POINT_HIGH : AppConst.COLOR_POINT_LOW);
            text.setText(rating_value >= 10 ? "10" : String.format("%.1f", rating_value));
        } rating.setRating(rating_value == null || rating_value < 0 ? 5.0f : rating_value);
    }

    private void setPointProgress(TextView prefix, ProgressBar progress, TextView text, Integer point_sum, Integer count) {
        Integer progress_value = count == null || count <= 0 ? null : point_sum == null ? null : (int)(((float)point_sum / (float)count) * 10);
        if(progress_value == null || progress_value < 0) {
            prefix.setTextColor(AppConst.COLOR_NEUTRAL);
            progress.setProgressDrawable(new ColorDrawable(AppConst.COLOR_NEUTRAL));
            progress.setProgress(100);
            text.setTextColor(AppConst.COLOR_NEUTRAL);
            text.setText("N/A");
        } else {
            progress.setProgress(progress_value);
            text.setText(progress_value >= 100 ? "10" : String.format("%d.%d", progress_value/10, progress_value%10));
        }
    }

    public void bind(Course course) {
        final Context context = this.itemView.getContext();
        final Integer count = course.getEvaluationCount();
        this.category.setText(context.getString(R.string.category_major)); // TODO -> evaluation.category
        this.lecture.setText(course.getName());
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), course.getProfessorName(), context.getResources().getString(R.string.professor_postfix))));
        Picasso.with(context).load(course.getProfessorPhotoUrl()).transform(new CircleTransformation()).into(this.professor_image);
        this.pointOverallPrefix.setText(R.string.label_point_overall);
        this.setPointRating(this.pointOverallPrefix, this.pointOverallRating, this.pointOverallText, course.getPointOverall(), count);
        this.pointClarityPrefix.setText(R.string.label_point_clarity);
        this.setPointProgress(this.pointClarityPrefix, this.pointClarityProgress, this.pointClarityText, course.getPointClarity(), count);
        this.pointGpaSatisfactionPrefix.setText(R.string.label_point_gpa_satisfaction);
        this.setPointProgress(this.pointGpaSatisfactionPrefix, this.pointGpaSatisfactionProgress, this.pointGpaSatisfactionText, course.getPointGpaSatisfaction(), count);
        this.pointEasinessPrefix.setText(R.string.label_point_easiness);
        this.setPointProgress(this.pointEasinessPrefix, this.pointEasinessProgress, this.pointEasinessText, course.getPointEasiness(), count);
        this.hashtags.removeAllViews();
        if(course.getHashtags()!=null) this.hashtags.post(() -> {
            float totalWidth = 0;
            for (String hashtag : course.getHashtags()) {
                Hashtag tag = new Hashtag(this.itemView.getContext(), hashtag);
                float width = tag.getPaint().measureText((String) tag.getText());
                if (width + totalWidth > hashtags.getWidth()) break;
                this.hashtags.addView(tag);
                totalWidth += width;
            }
        });
        Picasso.with(context).load(R.drawable.ic_light_people).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.evaluatorIcon);
        this.evaluatorCount.setText(count == null || count < 0 ? "N/A" : String.valueOf(count));
        Picasso.with(context).load(R.drawable.ic_light_bookmark)
            .transform(
                new ColorFilterTransformation(course.getIsFavorite()? AppConst.COLOR_POINT_CLARITY : AppConst.COLOR_GRAY ))
            .into(this.bookmark);
        this.bookmark.setOnClickListener(this);
        Timber.d("favor %s", Course.getInstance().getIsFavorite());
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.course_bookmark){
            this.favorite(!Course.getInstance().getIsFavorite());
        }
    }
    private void favorite(boolean setting){
        final Context context = this.itemView.getContext();
        this.subscription.add(
            RetrofitApi.getInstance().post_course_favorite(User.getInstance().getAccessToken(), Course.getInstance().getId(), setting)
                .filter(response -> response.success)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        Course.getInstance().setIsFavorite(setting);
                        Picasso.with(context).load(R.drawable.ic_light_bookmark)
                            .transform(
                                new ColorFilterTransformation(setting ? AppConst.COLOR_HIGHLIGHT_YELLOW : AppConst.COLOR_GRAY))
                            .into(this.bookmark);
                    }, error -> {
                        error.printStackTrace();
                    }
                )
        );
    }
}
