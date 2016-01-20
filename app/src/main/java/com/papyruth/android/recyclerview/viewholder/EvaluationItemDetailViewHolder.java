package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.utility.helper.CategoryHelper;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.PointHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

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
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, getAdapterPosition()));
        mContext = view.getContext();
        mResources = mContext.getResources();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public void bind(EvaluationData evaluation) {
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mLecture.setSelected(true);
        mLecture.setText(evaluation.lecture_name);
        mProfessor.setText(String.format("%s%s %s", mResources.getString(R.string.professor_prefix), evaluation.professor_name, mResources.getString(R.string.professor_postfix)));
        CategoryHelper.assignColor(mContext, mCategory, mProfessor, evaluation.lecture_category);
        mTimestamp.setText(DateTimeHelper.timeago(mContext, evaluation.created_at));
        mBody.setText(evaluation.body.replace('\n', ' '));
        mNickname.setText(evaluation.user_nickname);
        PointHelper.applyRating(mContext, mRatingBarOverall, evaluation.point_overall);
    }
}