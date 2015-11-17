package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.R;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.picasso.ContrastColorFilterTransformation;
import com.papyruth.utils.view.DateTimeUtil;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyEvaluationViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.evaluation_item_body)              protected TextView mBody;
    @InjectView(R.id.evaluation_item_lecture)           protected TextView mLecture;
    @InjectView(R.id.evaluation_item_professor)         protected TextView mProfessor;
    @InjectView(R.id.evaluation_item_timestamp)         protected TextView mTimestamp;
    @InjectView(R.id.evaluation_item_category)          protected TextView mCategory;
    @InjectView(R.id.evaluation_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @InjectView(R.id.evaluation_item_up_vote_count)     protected TextView mVoteUpCount;
    @InjectView(R.id.evaluation_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @InjectView(R.id.evaluation_item_down_vote_count)   protected TextView mVoteDownCount;
    @InjectView(R.id.evaluation_item_comment_icon)      protected ImageView mCommentIcon;
    @InjectView(R.id.evaluation_item_comment_count)     protected TextView mCommentCount;
    private final Context mContext;
    private final Resources mResources;
    public MyEvaluationViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        mBody.setText(evaluation.body);
        mLecture.setText(evaluation.lecture_name);
        mProfessor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mContext.getResources().getString(R.string.professor_prefix), evaluation.professor_name, mContext.getResources().getString(R.string.professor_postfix))));
        mTimestamp.setText(DateTimeUtil.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        mCategory.setTextColor(mResources.getColor(R.color.colorchip_green_highlight));
        mCategory.setText(evaluation.category);
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(mResources.getColor(R.color.inactive))).into(mVoteUpIcon);
        mVoteUpCount.setText(String.valueOf(evaluation.up_vote_count == null ? 0 : evaluation.up_vote_count));
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(mResources.getColor(R.color.inactive))).into(mVoteDownIcon);
        mVoteDownCount.setText(String.valueOf(evaluation.up_vote_count == null ? 0 : evaluation.up_vote_count));
        Picasso.with(mContext).load(R.drawable.ic_light_comment_16dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.inactive))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }
}