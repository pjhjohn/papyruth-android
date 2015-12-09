package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemDetailViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.evaluation_item_avatar)               protected ImageView mAvatar;
    @Bind(R.id.evaluation_item_nickname)             protected TextView mNickname;
    @Bind(R.id.evaluation_item_lecture)              protected TextView mLecture;
    @Bind(R.id.evaluation_item_timestamp)            protected RobotoTextView mTimestamp;
    @Bind(R.id.evaluation_item_category)             protected TextView mCategory;
    @Bind(R.id.evaluation_item_professor)            protected TextView mProfessor;
    @Bind(R.id.evaluation_item_body)                 protected TextView mBody;
    @Bind(R.id.evaluation_item_overall_ratingbar)    protected RatingBar mRatingBarOverall;

    private final Context mContext;
    private final Resources mResources;
    public EvaluationItemDetailViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mLecture.setSelected(true);
        mLecture.setText(evaluation.lecture_name);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s </strong>%s", mResources.getString(R.string.professor_prefix), evaluation.professor_name, mResources.getString(R.string.professor_postfix))));
        setCategoryProfessorColor(mCategory, mProfessor, evaluation.category);
        mTimestamp.setText(DateTimeHelper.timeago(mContext, evaluation.created_at));
        mBody.setText(evaluation.body.replace('\n', ' '));
        mNickname.setText(evaluation.user_nickname);
        PointHelper.setPointRating(mContext, mRatingBarOverall, evaluation.point_overall);
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
}