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
import com.papyruth.android.model.CommentData;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.materialdialog.VotersDialog;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.helper.VoteHelper;
import com.papyruth.support.utility.helper.VoteHelper.VoteStatus;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

public class CommentItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.comment_item_avatar)           protected ImageView mAvatar;
    @BindView(R.id.comment_item_nickname)         protected TextView mNickname;
    @BindView(R.id.comment_item_timestamp)        protected RobotoTextView mTimestamp;
    @BindView(R.id.comment_item_body)             protected TextView mBody;
    @BindView(R.id.comment_item_up_vote_icon)     protected ImageView mVoteUpIcon;
    @BindView(R.id.comment_item_up_vote_count)    protected RobotoTextView mVoteUpCount;
    @BindView(R.id.comment_item_down_vote_icon)   protected ImageView mVoteDownIcon;
    @BindView(R.id.comment_item_down_vote_count)  protected RobotoTextView mVoteDownCount;
    private int mCommentId;
    private VoteStatus mVoteStatus;
    private final Context mContext;

    public CommentItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(this);
        mContext = view.getContext();
        mNickname.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mVoteUpIcon.setOnClickListener(this);
        mVoteUpCount.setOnClickListener(this);
        mVoteDownIcon.setOnClickListener(this);
        mVoteDownCount.setOnClickListener(this);
        mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.NONE);
    }

    public void bind(CommentData comment, View.OnLongClickListener listener) {
        mCommentId = comment.id;
        Picasso.with(mContext).load(comment.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mNickname.setText(comment.user_nickname);
        mTimestamp.setText(DateTimeHelper.timestamp(comment.updated_at, AppConst.DateFormat.DATE_TIME_12HR));
        mBody.setText(comment.body);
        mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, comment);
        this.itemView.setOnLongClickListener(listener);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.comment_item_up_vote_icon:
                if(mVoteStatus == VoteStatus.UP) Api.papyruth()
                    .delete_comment_vote(User.getInstance().getAccessToken(), mCommentId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.NONE, response), error ->  ErrorHandler.handle(error, this, true));
                else Api.papyruth()
                    .post_comment_vote(User.getInstance().getAccessToken(), mCommentId, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.UP, response), error ->  ErrorHandler.handle(error, this, true));
                break;
            case R.id.comment_item_down_vote_icon:
                if(mVoteStatus == VoteStatus.DOWN) Api.papyruth()
                    .delete_comment_vote(User.getInstance().getAccessToken(), mCommentId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.NONE, response), error ->  ErrorHandler.handle(error, this, true));
                else Api.papyruth()
                    .post_comment_vote(User.getInstance().getAccessToken(), mCommentId, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> mVoteStatus = VoteHelper.applyStatus(mContext, mVoteUpIcon, mVoteUpCount, mVoteDownIcon, mVoteDownCount, VoteStatus.DOWN, response), error ->  ErrorHandler.handle(error, this, true));
                break;
            case R.id.comment_item_up_vote_count:
            case R.id.comment_item_down_vote_count:
                Api.papyruth()
                    .get_comment_vote(User.getInstance().getAccessToken(), mCommentId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> VotersDialog.show(
                        view.getContext(),
                        view.getId() == R.id.comment_item_up_vote_count ? "UP" : "DOWN",
                        view.getId() == R.id.comment_item_up_vote_count ? response.up : response.down
                    ), error ->  ErrorHandler.handle(error, this, true));
                break;
            default : break;
        }
    }
}
