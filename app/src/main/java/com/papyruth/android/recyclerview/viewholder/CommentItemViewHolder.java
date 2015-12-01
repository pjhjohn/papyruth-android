package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
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
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.materialdialog.VotersDialog;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.picasso.SkewContrastColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.helper.DateTimeHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CommentItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.comment_item_avatar)           protected ImageView mAvatar;
    @InjectView(R.id.comment_item_nickname)         protected TextView mNickname;
    @InjectView(R.id.comment_item_timestamp)        protected RobotoTextView mTimestamp;
    @InjectView(R.id.comment_item_body)             protected TextView mBody;
    @InjectView(R.id.comment_item_up_vote_icon)     protected ImageView mVoteUpIcon;
    @InjectView(R.id.comment_item_up_vote_count)    protected RobotoTextView mVoteUpCount;
    @InjectView(R.id.comment_item_down_vote_icon)   protected ImageView mVoteDownIcon;
    @InjectView(R.id.comment_item_down_vote_count)  protected RobotoTextView mVoteDownCount;
    private int mCommentId;
    private VoteStatus mVoteStatus;
    private final Context mContext;
    private final Resources mResources;
    public enum VoteStatus {
        UP, DOWN, NONE
    }

    public CommentItemViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
        mResources = mContext.getResources();
        view.setOnClickListener(this);
        mNickname.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mVoteUpIcon.setOnClickListener(this);
        mVoteUpCount.setOnClickListener(this);
        mVoteDownIcon.setOnClickListener(this);
        mVoteDownCount.setOnClickListener(this);
        setVoteStatus(VoteStatus.NONE);
    }

    private void setVoteStatus(VoteStatus newStatus) {
        mVoteStatus = newStatus;

        Picasso.with(mContext).load(R.drawable.ic_light_vote_up).transform(new SkewContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none))).into(mVoteUpIcon);
        mVoteUpCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none));

        Picasso.with(mContext).load(R.drawable.ic_light_vote_down).transform(new SkewContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none))).into(mVoteDownIcon);
        mVoteDownCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none));
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        mVoteUpCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        mVoteDownCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }

    public void bind(CommentData comment) {
        mCommentId = comment.id;

        Picasso.with(mContext).load(comment.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mNickname.setText(comment.user_nickname);
        mTimestamp.setText(DateTimeHelper.timestamp(comment.updated_at, AppConst.DateFormat.DATE_AND_TIME));
        mBody.setText(comment.body);

        if(comment.request_user_vote == null) setVoteStatus(VoteStatus.NONE);
        else if(comment.request_user_vote == 1) setVoteStatus(VoteStatus.UP);
        else setVoteStatus(VoteStatus.DOWN);

        setVoteCount(comment.up_vote_count, comment.down_vote_count);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.comment_item_up_vote_icon:
                if(mVoteStatus == VoteStatus.UP) Api.papyruth()
                    .delete_comment_vote(User.getInstance().getAccessToken(), mCommentId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.NONE);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.handle(error, this));
                else Api.papyruth()
                    .post_comment_vote(User.getInstance().getAccessToken(), mCommentId, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.UP);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.handle(error, this));
                break;
            case R.id.comment_item_down_vote_icon:
                if(mVoteStatus == VoteStatus.DOWN) Api.papyruth()
                    .delete_comment_vote(User.getInstance().getAccessToken(), mCommentId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.NONE);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.handle(error, this));
                else Api.papyruth()
                    .post_comment_vote(User.getInstance().getAccessToken(), mCommentId, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        setVoteStatus(VoteStatus.DOWN);
                        setVoteCount(response.up_vote_count, response.down_vote_count);
                    }, error ->  ErrorHandler.handle(error, this));
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
                    ), error ->  ErrorHandler.handle(error, this));
                break;
            default : Timber.d("Clicked view : %s", view);
        }
    }
}
