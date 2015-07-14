package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Paint;
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
public class EvaluationItemDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView (R.id.evaluation_item_avatar) protected ImageView avatar;
    @InjectView (R.id.evaluation_item_category) protected TextView category;
    @InjectView (R.id.evaluation_item_lecture) protected TextView lecture;
    @InjectView (R.id.evaluation_item_professor) protected TextView professor;
    @InjectView (R.id.evaluation_item_timestamp) protected TextView timestamp;
    @InjectView (R.id.evaluation_item_body) protected TextView body;
    @InjectView (R.id.evaluation_item_nickname) protected TextView nickname;
    @InjectView (R.id.evaluation_item_hashtag) protected LinearLayout hashtag;
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
    public enum VoteStatus {
        UP, DOWN, NONE
    }
    public EvaluationItemDetailViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.lecture.setPaintFlags(this.lecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setPaintFlags(this.category.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setTextColor(itemView.getContext().getResources().getColor(R.color.fg_accent));
    }

    private void setStatus(VoteStatus newStatus) {
        this.status = newStatus;

        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_chevron_up).transform(new ContrastColorFilterTransformation(status == VoteStatus.UP ? AppConst.COLOR_POSITIVE : AppConst.COLOR_NEUTRAL)).into(this.upIcon);
        this.upCount.setTextColor(status == VoteStatus.UP ? AppConst.COLOR_POSITIVE : AppConst.COLOR_NEUTRAL);

        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_chevron_down).transform(new ContrastColorFilterTransformation(status == VoteStatus.DOWN ? AppConst.COLOR_NEGATIVE : AppConst.COLOR_NEUTRAL)).into(this.downIcon);
        this.downCount.setTextColor(status == VoteStatus.DOWN ? AppConst.COLOR_NEGATIVE : AppConst.COLOR_NEUTRAL);
    }

    private void setVoteCount(Integer upCount, Integer downCount) {
        this.upCount.setText(String.valueOf(upCount == null ? 0 : upCount));
        this.downCount.setText(String.valueOf(downCount == null ? 0 : downCount));
    }

    public void bind(EvaluationData evaluation) {
        final Context context = this.itemView.getContext();
        Picasso.with(context).load(evaluation.avatar_url).transform(new CircleTransformation()).into(this.avatar);
        this.category.setText("Àü°ø"); // TODO -> evaluation.category
        this.lecture.setText(evaluation.lecture_name);
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), evaluation.professor_name, context.getResources().getString(R.string.professor_postfix))));
        this.timestamp.setText(DateTimeUtil.timeago(context, evaluation.created_at));
        this.body.setText(evaluation.body);
        this.nickname.setText(evaluation.user_nickname);
        this.hashtag.removeAllViews();
        for(int i = 0; i < 5; i ++) this.hashtag.addView(new Hashtag(this.itemView.getContext(), "tag" + i)); // TODO : use real hashtag
        if(evaluation.point_overall == null || evaluation.point_overall < 0) this.pointText.setText("N/A");
        else if(evaluation.point_overall >= 10) this.pointText.setText("10");
        else this.pointText.setText(String.format("%d.0", evaluation.point_overall));
        this.pointStar.setRating(evaluation.point_overall);

        if(evaluation.request_user_vote == null) this.setStatus(VoteStatus.NONE);
        else if(evaluation.request_user_vote == 1) this.setStatus(VoteStatus.UP);
        else this.setStatus(VoteStatus.DOWN);

        this.setVoteCount(evaluation.up_vote_count, evaluation.down_vote_count);

        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_comment).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.commentIcon);
        this.commentCount.setText(String.valueOf(evaluation.comment_count == null ? 0 : evaluation.comment_count));
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}