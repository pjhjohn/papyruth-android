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

import com.papyruth.android.R;
import com.papyruth.android.model.CourseData;
import com.papyruth.utils.support.picasso.CircleTransformation;
import com.papyruth.utils.view.Hashtag;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseItemViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.course_item_category)           protected TextView mCategory;
    @InjectView(R.id.course_item_lecture)            protected TextView mLecture;
    @InjectView(R.id.course_item_professor)          protected TextView mProfessor;
    @InjectView(R.id.course_item_professor_image)    protected ImageView mProfessorImage;
    @InjectView(R.id.course_item_point_overall_star) protected RatingBar mPointOverallStar;
    @InjectView(R.id.course_item_point_overall_text) protected TextView mPointOverallText;
    @InjectView(R.id.course_item_hashtags)           protected LinearLayout mHashtags;
    @InjectView(R.id.course_item_evaluator_icon)     protected ImageView mEvaluatorIcon;
    @InjectView(R.id.course_item_evaluator_count)    protected TextView mEvaluatorCount;
    private final Context mContext;
    private final Resources mResources;
    public CourseItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition() - 1));
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
        mProfessor.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
    }

    private void setRatingBarColor(RatingBar rating, int color) {
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mPointOverallText.setTextColor(color);
    }

    private void setPointRating(RatingBar rating, Integer point_sum, Integer count) {
        Float rating_value = count == null || count <= 0 ? null : point_sum == null ? null : (float)point_sum / (float)count / 2f;
        if(rating_value == null || rating_value < 0) setRatingBarColor(rating, mResources.getColor(R.color.inactive));
        else setRatingBarColor(rating, mResources.getColor(rating_value >= 8 ? R.color.point_high : R.color.point_low));
        rating.setRating(rating_value == null || rating_value < 0 ? 5.0f : rating_value);
        mPointOverallText.setText(rating_value == null || rating_value < 0 ? "0" : String.format("%.1f", rating_value));
    }

    public void bind(CourseData course) {
        mCategory.setText(course.category);
        mLecture.setText(course.name);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), course.professor_name, mResources.getString(R.string.professor_postfix))));
        Picasso.with(mContext).load(course.professor_photo_url).transform(new CircleTransformation()).into(mProfessorImage);
        setPointRating(mPointOverallStar, course.point_overall, course.evaluation_count);
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
        mEvaluatorCount.setText(course.evaluation_count == null || course.evaluation_count < 0 ? "N/A" : String.valueOf(course.evaluation_count));
    }
}