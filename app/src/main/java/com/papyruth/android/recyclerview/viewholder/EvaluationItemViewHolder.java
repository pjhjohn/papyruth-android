package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.unique.User;
import com.papyruth.utils.support.picasso.CircleTransformation;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.picasso.ContrastColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.DateTimeUtil;
import com.papyruth.utils.view.Hashtag;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemViewHolder extends RecyclerView.ViewHolder {
    @InjectView (R.id.evaluation_item_avatar)             protected ImageView mAvatar;
    @InjectView (R.id.evaluation_item_timestamp)          protected TextView mTimestamp;
    @InjectView (R.id.evaluation_item_body)               protected TextView mBody;
    @InjectView (R.id.evaluation_item_nickname)           protected TextView mNickname;
    @InjectView (R.id.evaluation_item_hashtags)           protected LinearLayout mHashtags;
    @InjectView (R.id.evaluation_item_point_overall_text) protected TextView mPointOverallText;
    @InjectView (R.id.evaluation_item_point_overall_star) protected RatingBar mPointOverallStar;
    @InjectView (R.id.evaluation_item_up_vote_icon)       protected ImageView mVoteUpIcon;
    @InjectView (R.id.evaluation_item_up_vote_count)      protected TextView mVoteUpCount;
    @InjectView (R.id.evaluation_item_down_vote_icon)     protected ImageView mVoteDownIcon;
    @InjectView (R.id.evaluation_item_down_vote_count)    protected TextView mVoteDownCount;
    @InjectView (R.id.evaluation_item_comment_icon)       protected ImageView mCommentIcon;
    @InjectView (R.id.evaluation_item_comment_count)      protected TextView mCommentCount;
    private Integer mEvaluationId;
    private VoteStatus mVoteStatus;
    private final Context mContext;
    private final Resources mResources;
    public enum VoteStatus {
        UP, DOWN, NONE
    }

    public EvaluationItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        mContext = itemView.getContext();
        mResources = mContext.getResources();
        mNickname.setPaintFlags(mNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        itemView.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(EvaluationData evaluation) {
        mEvaluationId = evaluation.id;
        Picasso.with(mContext).load(evaluation.avatar_url).transform(new CircleTransformation()).into(mAvatar);
        mTimestamp.setText(DateTimeUtil.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        mBody.setText(evaluation.body);
        mNickname.setText(evaluation.user_nickname);

        mHashtags.removeAllViews();
        if(mEvaluationId != null) Api.papyruth()
            .get_evaluation_hashtag(User.getInstance().getAccessToken(), mEvaluationId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.hashtags != null) mHashtags.post(() -> {
                    float totalWidth = 0;
                    for (String hashtag : response.hashtags) {
                        Evaluation.getInstance().addHashTag(hashtag);
                        Hashtag tag = new Hashtag(mContext, hashtag);
                        float width = tag.getPaint().measureText((String) tag.getText());
                        if (width + totalWidth > mHashtags.getWidth()) break;
                        mHashtags.addView(tag);
                        totalWidth += width;
                    }
                });
            });
        setPointRating(evaluation.point_overall);

        if(evaluation.request_user_vote == null) setVoteStatus(VoteStatus.NONE);
        else if(evaluation.request_user_vote == 1) setVoteStatus(VoteStatus.UP);
        else setVoteStatus(VoteStatus.DOWN);
        setVoteCount(evaluation.up_vote_count, evaluation.down_vote_count);

        Picasso.with(mContext).load(R.drawable.ic_light_comment_16dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.inactive))).into(mCommentIcon);
        mCommentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }

    private void setVoteStatus(VoteStatus newStatus) {
        mVoteStatus = newStatus;
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none))).into(mVoteUpIcon);
        mVoteUpCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.UP ? R.color.vote_up : R.color.vote_none));
        Picasso.with(mContext).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none))).into(mVoteDownIcon);
        mVoteDownCount.setTextColor(mResources.getColor(mVoteStatus == VoteStatus.DOWN ? R.color.vote_down : R.color.vote_none));
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        mVoteUpCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        mVoteDownCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }

    private void setPointRating(Integer point) {
        final int pointColor = mResources.getColor(pointInRange(point)? ( point>=8?R.color.point_high:R.color.point_low ) : R.color.point_none);
        for(int i = 0; i < 3; i ++) ((LayerDrawable) mPointOverallStar.getProgressDrawable()).getDrawable(i).setColorFilter(pointColor, PorterDuff.Mode.SRC_ATOP);
        mPointOverallText.setTextColor(pointColor);
        mPointOverallText.setText(pointInRange(point) ? (point >= 10 ? "10" : String.format("%d", point)) : "N/A");
        mPointOverallStar.setRating(pointInRange(point) ? (float)point/2f : 5.0f );
    }

    private boolean pointInRange(Integer point) {
        return point!=null && point >= 0 && point <= 10;
    }
}