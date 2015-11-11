package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
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
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.picasso.ContrastColorFilterTransformation;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.Hashtag;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView (R.id.evaluation_item_avatar) protected ImageView avatar;
    @InjectView (R.id.evaluation_item_timestamp) protected TextView timestamp;
    @InjectView (R.id.evaluation_item_body) protected TextView body;
    @InjectView (R.id.evaluation_item_nickname) protected TextView nickname;
    @InjectView (R.id.evaluation_item_hashtags) protected LinearLayout hashtags;
    @InjectView (R.id.evaluation_item_point_overall_text) protected TextView pointText;
    @InjectView (R.id.evaluation_item_point_overall_star) protected RatingBar pointStar;
    @InjectView (R.id.evaluation_item_up_vote_icon) protected ImageView upIcon;
    @InjectView (R.id.evaluation_item_up_vote_count) protected TextView upCount;
    @InjectView (R.id.evaluation_item_down_vote_icon) protected ImageView downIcon;
    @InjectView (R.id.evaluation_item_down_vote_count) protected TextView downCount;
    @InjectView (R.id.evaluation_item_comment_icon) protected ImageView commentIcon;
    @InjectView (R.id.evaluation_item_comment_count) protected TextView commentCount;
    private RecyclerViewItemClickListener itemClickListener;
    private VoteStatus status;
    private final Context context;
    public enum VoteStatus {
        UP, DOWN, NONE
    }

    public EvaluationItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.context = itemView.getContext();
        itemView.setOnClickListener(this);
        this.itemClickListener = listener;
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    private void setStatus(VoteStatus newStatus) {
        this.status = newStatus;

        Picasso.with(context).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(context.getResources().getColor(status == VoteStatus.UP ? R.color.vote_up : R.color.vote_none))).into(this.upIcon);
        this.upCount.setTextColor(context.getResources().getColor(status == VoteStatus.UP ? R.color.vote_up : R.color.vote_none));

        Picasso.with(context).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(context.getResources().getColor(status==VoteStatus.DOWN? R.color.vote_down : R.color.vote_none))).into(this.downIcon);
        this.downCount.setTextColor(context.getResources().getColor(status==VoteStatus.DOWN? R.color.vote_down : R.color.vote_none));
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        this.upCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        this.downCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }

    private void setRatingBarColor(int color) {
        LayerDrawable stars = (LayerDrawable) pointStar.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void setPoint(Integer point) {
        final Resources res = context.getResources();
        String pointStr;
        if(point == null || point < 0) {
            pointStr = "N/A";
            this.pointText.setTextColor(res.getColor(R.color.inactive));
            this.setRatingBarColor(res.getColor(R.color.inactive));
            this.pointStar.setRating(5.0f);
        } else if(point >= 8) {
            if(point >= 10) pointStr = "10";
            else pointStr = String.format("%d.0", point);
            this.pointText.setTextColor(res.getColor(R.color.point_high));
            this.setRatingBarColor(res.getColor(R.color.point_high));
        } else {
            pointStr = String.format("%d.0", point);
            this.pointText.setTextColor(res.getColor(R.color.point_low));
            this.setRatingBarColor(res.getColor(R.color.point_low));
        }
        this.pointText.setText(Html.fromHtml(String.format("%s<strong>%s</strong>", "", pointStr)));
        this.pointStar.setRating(point/2.0f);
    }

    public void bind(EvaluationData evaluation) {
        Picasso.with(context).load(evaluation.avatar_url).transform(new CircleTransformation()).into(this.avatar);
        this.timestamp.setText(DateTimeUtil.timestamp(evaluation.created_at, AppConst.DateFormat.DATE_AND_TIME));
        this.body.setText(evaluation.body);
        this.nickname.setText(evaluation.user_nickname);
        this.hashtags.removeAllViews();
        this.hashtags.post(() -> {
            float totalWidth = 0;
            for(int i = 0; i < 5; i ++) {
                Hashtag hashtag = new Hashtag(context, "hashtag" + i);
                float width = hashtag.getPaint().measureText((String)hashtag.getText());
                if(width + totalWidth > hashtags.getWidth()) break;
                this.hashtags.addView(hashtag);
                totalWidth += width;
                // TODO : use real hashtag
            }
        });

        this.setPoint(evaluation.point_overall);

        if(evaluation.request_user_vote == null) this.setStatus(VoteStatus.NONE);
        else if(evaluation.request_user_vote == 1) this.setStatus(VoteStatus.UP);
        else this.setStatus(VoteStatus.DOWN);

        this.setVoteCount(evaluation.up_vote_count, evaluation.down_vote_count);

        Picasso.with(context).load(R.drawable.ic_light_comment_16dp).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.inactive))).into(this.commentIcon);
        this.commentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}