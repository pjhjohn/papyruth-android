package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.R;
import com.papyruth.android.model.CourseData;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseItemViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.course_item_professor_image)    protected ImageView mProfessorImage;
    @Bind(R.id.course_item_lecture)            protected TextView mLecture;
    @Bind(R.id.course_item_category)           protected TextView mCategory;
    @Bind(R.id.course_item_professor)          protected TextView mProfessor;
    @Bind(R.id.course_item_overall_label)      protected TextView mLabelOverall;
    @Bind(R.id.course_item_overall_point)      protected RobotoTextView mPointOverall;
    @Bind(R.id.course_item_overall_ratingbar)  protected RatingBar mRatingBarOverall;
    @Bind(R.id.course_item_hashtags)           protected TextView mHashtags;
    @Bind(R.id.course_item_evaluation_icon)    protected ImageView mEvaluationIcon;
    @Bind(R.id.course_item_evaluation_count)   protected TextView mEvaluationCount;
    private final Context mContext;
    private final Resources mResources;
    public CourseItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(CourseData course) {
        Picasso.with(mContext).load(course.professor_photo_url).transform(new CircleTransformation()).into(mProfessorImage);
        mLecture.setText(course.name);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), course.professor_name, mResources.getString(R.string.professor_postfix))));
        setCategoryProfessorColor(mCategory, mProfessor, course.category);
        mLabelOverall.setText(R.string.label_point_overall);
        PointHelper.setPointRating(mContext, mLabelOverall, mRatingBarOverall, mPointOverall, course.point_overall, course.evaluation_count);

        this.mHashtags.setText(Hashtag.getHashtag(course.hashtags));

        Picasso.with(mContext).load(R.drawable.ic_evaluation_count_24dp).transform(new SkewContrastColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mEvaluationIcon);
        mEvaluationCount.setText(String.valueOf(course.evaluation_count == null ? 0 : String.valueOf(course.evaluation_count)));
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