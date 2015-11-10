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
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.CourseData;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.Hashtag;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.course_item_category) TextView category;
    @InjectView(R.id.course_item_lecture) protected TextView lecture;
    @InjectView(R.id.course_item_professor) protected TextView professor;
    @InjectView(R.id.course_item_professor_image) protected ImageView professor_image;
    @InjectView(R.id.course_item_point_overall_star) protected RatingBar pointOverallRating;
    @InjectView(R.id.course_item_point_overall_text) protected TextView pointOverallText;
    @InjectView(R.id.course_item_hashtags) protected LinearLayout hashtags;
    @InjectView(R.id.course_item_evaluator_icon) protected ImageView evaluatorIcon;
    @InjectView(R.id.course_item_evaluator_count) protected TextView evaluatorCount;
    RecyclerViewItemClickListener itemClickListener;
    private final Context context;
    public CourseItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.context = itemView.getContext();
        itemView.setOnClickListener(this);
        this.itemClickListener = listener;
        this.lecture.setPaintFlags(this.lecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setPaintFlags(this.category.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setTextColor(itemView.getContext().getResources().getColor(R.color.colorchip_green_highlight));
        this.professor.setTextColor(itemView.getContext().getResources().getColor(R.color.colorchip_green_highlight));
    }

    private void setRatingBarColor(RatingBar rating, int color) {
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        this.pointOverallText.setTextColor(color);
    }

    private void setPointRating(RatingBar rating, Integer point_sum, Integer count) {
        Float rating_value = count == null || count <= 0 ? null : point_sum == null ? null : (float)point_sum / (float)count / 2f;
        if(rating_value == null || rating_value < 0) this.setRatingBarColor(rating, context.getResources().getColor(R.color.inactive));
        else this.setRatingBarColor(rating, context.getResources().getColor(rating_value >= 8? R.color.point_high : R.color.point_low));
        rating.setRating(rating_value == null || rating_value < 0 ? 5.0f : rating_value);
        this.pointOverallText.setText(rating_value == null || rating_value < 0 ? "0" : String.format("%.1f", rating_value));
    }

    public void bind(CourseData course) {
        final Context context = this.itemView.getContext();
        this.category.setText(context.getString(R.string.category_major)); // TODO -> evaluation.category
        this.lecture.setText(course.name);
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), course.professor_name, context.getResources().getString(R.string.professor_postfix))));
        Picasso.with(context).load(course.professor_photo_url).transform(new CircleTransformation()).into(this.professor_image);
        this.setPointRating(this.pointOverallRating, course.point_overall, course.evaluation_count);
        this.hashtags.removeAllViews();
        if(course.hashtags!=null) this.hashtags.post(() -> {
            float totalWidth = 0;
            for (String hashtag : course.hashtags) {
                Hashtag tag = new Hashtag(this.itemView.getContext(), hashtag);
                float width = tag.getPaint().measureText((String) tag.getText());
                if (width + totalWidth > hashtags.getWidth()) break;
                this.hashtags.addView(tag);
                totalWidth += width;
            }
        });
        this.evaluatorCount.setText(course.evaluation_count == null || course.evaluation_count < 0 ? "N/A" : String.valueOf(course.evaluation_count));
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 1);
    }
}