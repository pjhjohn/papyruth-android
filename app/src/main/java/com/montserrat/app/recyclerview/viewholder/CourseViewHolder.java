package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
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
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.Hashtag;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.course_category) TextView category;
    @InjectView(R.id.course_lecture) protected TextView lecture;
    @InjectView(R.id.course_professor) protected TextView professor;
    @InjectView(R.id.course_professor_image) protected ImageView professor_image;
    @InjectView(R.id.course_point_overall_prefix) protected TextView pointOverallPrefix;
    @InjectView(R.id.course_point_overall_text) protected TextView pointOverallText;
    @InjectView(R.id.course_point_overall_star) protected RatingBar pointOverallStar;
    @InjectView(R.id.course_point_clarity_prefix) protected TextView pointClarityPrefix;
    @InjectView(R.id.course_point_clarity_text) protected TextView pointClarityText;
    @InjectView(R.id.course_point_clarity_progress) protected ProgressBar pointClarityProgress;
    @InjectView(R.id.course_point_easiness_prefix) protected TextView pointEasinessPrefix;
    @InjectView(R.id.course_point_easiness_text) protected TextView pointEasinessText;
    @InjectView(R.id.course_point_easiness_progress) protected ProgressBar pointEasinessProgress;
    @InjectView(R.id.course_point_gpa_satisfaction_prefix) protected TextView pointGpaSatisfactionPrefix;
    @InjectView(R.id.course_point_gpa_satisfaction_text) protected TextView pointGpaSatisfactionText;
    @InjectView(R.id.course_point_gpa_satisfaction_progress) protected ProgressBar pointGpaSatisfactionProgress;
    @InjectView(R.id.course_hashtags) protected LinearLayout hashtags;
    @InjectView(R.id.course_evaluator_icon) protected ImageView evaluatorIcon;
    @InjectView(R.id.course_evaluator_count) protected TextView evaluatorCount;

    public CourseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.lecture.setPaintFlags(this.lecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setPaintFlags(this.category.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setTextColor(itemView.getContext().getResources().getColor(R.color.fg_accent));
    }

    private void setRatingBarColor(int color) {
        LayerDrawable stars = (LayerDrawable) pointOverallStar.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void setOverallRating(Float point) {
        if(point == null || point < 0) this.setRatingBarColor(AppConst.COLOR_NEUTRAL);
        else if(point >= 8) this.setRatingBarColor(AppConst.COLOR_POINT_HIGH);
        else this.setRatingBarColor(AppConst.COLOR_POINT_LOW);
        this.pointOverallStar.setRating(point == null || point < 0 ? 5.0f : point / 2f);
    }

    public void bind(Course course) {
        final Context context = this.itemView.getContext();
        this.category.setText(context.getString(R.string.category_major)); // TODO -> evaluation.category
        this.lecture.setText(course.getName());
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), course.getProfessorName(), context.getResources().getString(R.string.professor_postfix))));
        Picasso.with(context).load(R.drawable.avatar_dummy).transform(new CircleTransformation()).into(this.professor_image);
        this.pointOverallPrefix.setText(R.string.label_point_overall);
        this.setOverallRating((float) course.getPointOverall() / (float) course.getEvaluationCount());
        this.pointOverallText.setText("TODO");
        this.pointClarityPrefix.setText(R.string.label_point_clarity);
        this.pointClarityProgress.setProgress(25);
        this.pointClarityText.setText("TODO");
        this.pointEasinessPrefix.setText(R.string.label_point_easiness);
        this.pointEasinessProgress.setProgress(50);
        this.pointEasinessText.setText("TODO");
        this.pointGpaSatisfactionPrefix.setText(R.string.label_point_gpa_satisfaction);
        this.pointGpaSatisfactionProgress.setProgress(75);
        this.pointGpaSatisfactionText.setText("TODO");
        this.hashtags.removeAllViews();
        this.hashtags.post(() -> {
            float totalWidth = 0;
            for (String hashtag : course.getHashtags()) {
                Hashtag tag = new Hashtag(this.itemView.getContext(), hashtag);
                float width = tag.getPaint().measureText((String) tag.getText());
                if (width + totalWidth > hashtags.getWidth()) break;
                this.hashtags.addView(tag);
                totalWidth += width;
            }
        });
        Picasso.with(context).load(R.drawable.ic_light_shuffle).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.evaluatorIcon);
        this.evaluatorCount.setText("3");
    }
}
