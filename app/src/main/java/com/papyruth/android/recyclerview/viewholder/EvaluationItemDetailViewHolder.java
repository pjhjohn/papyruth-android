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
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.utils.support.picasso.CircleTransformation;
import com.papyruth.utils.view.DateTimeUtil;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemDetailViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.evaluation_item_avatar)               protected ImageView mAvatar;
    @InjectView(R.id.evaluation_item_nickname)             protected TextView mNickname;
    @InjectView(R.id.evaluation_item_lecture)              protected TextView mLecture;
    @InjectView(R.id.evaluation_item_timestamp)            protected RobotoTextView mTimestamp;
    @InjectView(R.id.evaluation_item_category)             protected TextView mCategory;
    @InjectView(R.id.evaluation_item_professor)            protected TextView mProfessor;
    @InjectView(R.id.evaluation_item_body)                 protected TextView mBody;
    @InjectView(R.id.evaluation_item_overall_label)        protected TextView mLabelOverall;
    @InjectView(R.id.evaluation_item_overall_point)        protected RobotoTextView mPointOverall;
    @InjectView(R.id.evaluation_item_overall_ratingbar)    protected RatingBar mRatingBarOverall;

    private final Context mContext;
    private final Resources mResources;
    public EvaluationItemDetailViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLabelOverall.setPaintFlags(mLabelOverall.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mLecture.setSelected(true);
        mLecture.setText(evaluation.lecture_name);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s </strong>%s", mResources.getString(R.string.professor_prefix), evaluation.professor_name, mResources.getString(R.string.professor_postfix))));
        setCategoryProfessorColor(mCategory, mProfessor, evaluation.category);
        mTimestamp.setText(DateTimeUtil.timeago(mContext, evaluation.created_at));
        mBody.setText(evaluation.body);
        mNickname.setText(evaluation.user_nickname);
        mLabelOverall.setText(R.string.label_point_overall);
        setPointRating(mLabelOverall, mRatingBarOverall, mPointOverall, evaluation.point_overall);
    }

    /* TODO : Better response based on category value */
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

    private void setPointRating(TextView label, RatingBar ratingbar, TextView point, Integer value) {
        final int pointColor = mResources.getColor(pointInRange(value)? ( value>=8?R.color.point_high:R.color.point_low ) : R.color.point_none);
        label.setTextColor(pointColor);
        point.setTextColor(pointColor);
        point.setText(pointInRange(value) ? (value >= 10 ? "10" : String.format("%d.0", value)) : "N/A");
        for(int i = 0; i < 3; i ++) ((LayerDrawable) ratingbar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        ratingbar.setRating(pointInRange(value) ? (float) value / 2f : 5.0f);
    }

    private boolean pointInRange(Integer point) {
        return point!=null && point >= 0 && point <= 10;
    }
}