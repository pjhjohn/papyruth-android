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
    @InjectView (R.id.evaluation_item_avatar)             protected ImageView mAvatar;
    @InjectView (R.id.evaluation_item_nickname)           protected TextView mNickname;
    @InjectView (R.id.evaluation_item_lecture)            protected TextView mLecture;
    @InjectView (R.id.evaluation_item_timestamp)          protected TextView mTimestamp;
    @InjectView (R.id.evaluation_item_category)           protected TextView mCategory;
    @InjectView (R.id.evaluation_item_professor)          protected TextView mProfessor;
    @InjectView (R.id.evaluation_item_point_overall_star) protected RatingBar mPointOverallStar;
    @InjectView (R.id.evaluation_item_point_overall_text) protected TextView mPointOverallText;
    @InjectView (R.id.evaluation_item_body)               protected TextView mBody;
    private final Context mContext;
    private final Resources mResources;
    public EvaluationItemDetailViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mNickname.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, getAdapterPosition()));
    }

    private void setRatingBarColor(int color) {
        LayerDrawable stars = (LayerDrawable) mPointOverallStar.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mPointOverallText.setTextColor(color);
    }

    private void setPoint(Integer point) {
        if(point == null || point < 0) setRatingBarColor(mResources.getColor(R.color.inactive));
        else if(point >= 8) setRatingBarColor(mResources.getColor(R.color.point_high));
        else setRatingBarColor(mResources.getColor(R.color.point_low));
        mPointOverallStar.setRating(point == null || point < 0 ? 5.0f : point / 2f);
        mPointOverallText.setText(point == null || point < 0 ? "0 " : point.toString() + " ");
    }

    public void bind(EvaluationData evaluation) {
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mLecture.setSelected(true);
        mLecture.setText(evaluation.lecture_name);
        mCategory.setText(evaluation.category);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.professor_prefix), evaluation.professor_name, mResources.getString(R.string.professor_postfix))));
        mTimestamp.setText(DateTimeUtil.timeago(mContext, evaluation.created_at));
        mBody.setText(evaluation.body);
        mNickname.setText(evaluation.user_nickname);
        setPoint(evaluation.point_overall);
    }
}