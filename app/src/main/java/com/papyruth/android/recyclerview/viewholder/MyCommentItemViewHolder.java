package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.MyCommentData;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.VoteHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class MyCommentItemViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.my_comment_item_lecture)           protected TextView mLecture;
    @BindView(R.id.my_comment_item_timestamp)         protected TextView mTimestamp;
    @BindView(R.id.my_comment_item_body)              protected TextView mBody;
    @BindView(R.id.my_comment_item_up_vote_icon)      protected ImageView mVoteUpIcon;
    @BindView(R.id.my_comment_item_up_vote_count)     protected RobotoTextView mVoteUpCount;
    @BindView(R.id.my_comment_item_down_vote_icon)    protected ImageView mVoteDownIcon;
    @BindView(R.id.my_comment_item_down_vote_count)   protected RobotoTextView mVoteDownCount;
    private final Context mContext;

    public MyCommentItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        mLecture.setPaintFlags(mLecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, this.getAdapterPosition()));
    }

    public void bind(MyCommentData comment) {
        mLecture.setText(comment.lecture_name);
        mTimestamp.setText(DateTimeHelper.timestamp(comment.created_at, AppConst.DateFormat.DATE_TIME_12HR));
        mBody.setText(comment.body);
        VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, comment);
    }
}
