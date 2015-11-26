package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.R;
import com.papyruth.android.model.CourseData;
import com.papyruth.utils.support.picasso.CircleTransformation;
import com.papyruth.utils.support.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.utils.view.Hashtag;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseItemViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.course_item_professor_image)    protected ImageView mProfessorImage;
    @InjectView(R.id.course_item_lecture)            protected TextView mLecture;
    @InjectView(R.id.course_item_category)           protected TextView mCategory;
    @InjectView(R.id.course_item_professor)          protected TextView mProfessor;
    @InjectView(R.id.course_item_overall_label)      protected TextView mLabelOverall;
    @InjectView(R.id.course_item_overall_point)      protected RobotoTextView mPointOverall;
    @InjectView(R.id.course_item_overall_ratingbar)  protected RatingBar mRatingBarOverall;
    @InjectView(R.id.course_item_hashtags)           protected LinearLayout mHashtags;
    @InjectView(R.id.course_item_evaluation_icon)    protected ImageView mEvaluationIcon;
    @InjectView(R.id.course_item_evaluation_count)   protected TextView mEvaluationCount;
    private final Context mContext;
    private final Resources mResources;
    public CourseItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition() - 1));
    }

    public void bind(CourseData course) {
        Picasso.with(mContext).load(course.professor_photo_url).transform(new CircleTransformation()).into(mProfessorImage);
        mLecture.setText(course.name);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), course.professor_name, mResources.getString(R.string.professor_postfix))));
        setCategoryProfessorColor(mCategory, mProfessor, course.category);
        mLabelOverall.setText(R.string.label_point_overall);
        if (course.point_overall != null && course.evaluation_count != null && course.evaluation_count != 0) setPointRating(mLabelOverall, mRatingBarOverall, mPointOverall, (float) course.point_overall / (float) course.evaluation_count);
        else setPointRating(mLabelOverall, mRatingBarOverall, mPointOverall, -1);
        mHashtags.removeAllViews();
        if(course.hashtags!=null) mHashtags.post(() -> {
            float totalWidth = 0;
            for (String hashtag : course.hashtags) {
                Hashtag tag = new Hashtag(mContext, hashtag);
                tag.setMaxLines(1);
                float width = tag.getPaint().measureText((String) tag.getText());
                if (width + totalWidth > mHashtags.getWidth()) break;
                mHashtags.addView(tag);
                totalWidth += width;
            }
        });
        Picasso.with(mContext).load(R.drawable.ic_light_evaluation_count).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mEvaluationIcon);
        mEvaluationCount.setText(pointInRange(course.evaluation_count) ? String.valueOf(course.evaluation_count) : "N/A");
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

    private void setPointRating(TextView label, RatingBar ratingbar, TextView point, float value) {
        final int pointColor = mResources.getColor(pointInRange(value)? ( value>=8?R.color.point_high:R.color.point_low ) : R.color.point_none);
        label.setTextColor(pointColor);
        point.setTextColor(pointColor);
        point.setText(pointInRange(value) ? (value >= 10 ? "10" : String.format("%.1f", value)) : "N/A");
        for(int i = 0; i < 3; i ++) ((LayerDrawable) ratingbar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        ratingbar.setRating(pointInRange(value) ? value / 2f : 5.0f);
    }

    private boolean pointInRange(float point) {
        return 0 <= point && point <= 10;
    }
}