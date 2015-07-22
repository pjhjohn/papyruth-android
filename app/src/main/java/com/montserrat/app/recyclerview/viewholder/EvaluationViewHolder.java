package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.materialdialog.VotersDialog;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.picasso.ContrastColorFilterTransformation;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.Hashtag;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.evaluation_lecture) protected TextView lecture;
    @InjectView(R.id.evaluation_timestamp) protected TextView timestamp;
    @InjectView(R.id.evaluation_category) protected TextView category;
    @InjectView(R.id.evaluation_professor) protected TextView professor;
    @InjectView(R.id.evaluation_avatar) protected ImageView avatar;
    @InjectView(R.id.evaluation_nickname) protected TextView nickname;
    @InjectView(R.id.evaluation_body) protected TextView body;
    @InjectView(R.id.evaluation_point_overall_prefix) protected TextView pointOverallPrefix;
    @InjectView(R.id.evaluation_point_overall_text) protected TextView pointOverallText;
    @InjectView(R.id.evaluation_point_overall_star) protected RatingBar pointOverallRating;
    @InjectView(R.id.evaluation_point_clarity_prefix) protected TextView pointClarityPrefix;
    @InjectView(R.id.evaluation_point_clarity_text) protected TextView pointClarityText;
    @InjectView(R.id.evaluation_point_clarity_progress) protected ProgressBar pointClarityProgress;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_prefix) protected TextView pointGpaSatisfactionPrefix;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_text) protected TextView pointGpaSatisfactionText;
    @InjectView(R.id.evaluation_point_gpa_satisfaction_progress) protected ProgressBar pointGpaSatisfactionProgress;
    @InjectView(R.id.evaluation_point_easiness_prefix) protected TextView pointEasinessPrefix;
    @InjectView(R.id.evaluation_point_easiness_text) protected TextView pointEasinessText;
    @InjectView(R.id.evaluation_point_easiness_progress) protected ProgressBar pointEasinessProgress;
    @InjectView(R.id.evaluation_hashtags) protected LinearLayout hashtags;
    @InjectView(R.id.evaluation_up_vote_icon) protected ImageView upIcon;
    @InjectView(R.id.evaluation_up_vote_count) protected TextView upCount;
    @InjectView(R.id.evaluation_down_vote_icon) protected ImageView downIcon;
    @InjectView(R.id.evaluation_down_vote_count) protected TextView downCount;
    @InjectView(R.id.evaluation_comment_icon) protected ImageView commentIcon;
    @InjectView(R.id.evaluation_comment_count) protected TextView commentCount;
    private int id;
    private VoteStatus status;
    public enum VoteStatus {
        UP, DOWN, NONE
    }
    public EvaluationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.lecture.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.upIcon.setOnClickListener(this);
        this.upCount.setOnClickListener(this);
        this.downIcon.setOnClickListener(this);
        this.downCount.setOnClickListener(this);
        this.setStatus(VoteStatus.NONE);
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

    private void setRatingBarColor(RatingBar rating, int color) {
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void setPointRating(TextView prefix, RatingBar rating, TextView text, Integer point) {
        if(point == null || point < 0) {
            prefix.setTextColor(AppConst.COLOR_NEUTRAL);
            this.setRatingBarColor(rating, AppConst.COLOR_NEUTRAL);
            text.setTextColor(AppConst.COLOR_NEUTRAL);
            text.setText("N/A");
        } else {
            prefix.setTextColor(point >= 8 ? AppConst.COLOR_POINT_HIGH : AppConst.COLOR_POINT_LOW);
            this.setRatingBarColor(rating, point >= 8 ? AppConst.COLOR_POINT_HIGH : AppConst.COLOR_POINT_LOW);
            text.setTextColor(point >= 8 ? AppConst.COLOR_POINT_HIGH : AppConst.COLOR_POINT_LOW);
            text.setText(point >= 10 ? "10" : String.format("%d.0", point));
        } rating.setRating(point == null || point < 0 ? 5.0f : (float)point/2f);
    }

    private void setPointProgress(TextView prefix, ProgressBar progress, TextView text, Integer point) {
        if(point == null || point < 0) {
            prefix.setTextColor(AppConst.COLOR_NEUTRAL);
            progress.setProgressDrawable(new ColorDrawable(AppConst.COLOR_NEUTRAL));
            progress.setProgress(100);
            text.setTextColor(AppConst.COLOR_NEUTRAL);
            text.setText("N/A");
        } else {
            progress.setProgress(point);
            text.setText(point >= 10 ? "10" : String.format("%d.0", point));
        }
    }


    public void bind(Evaluation evaluation) {
        final Context context = this.itemView.getContext();
        this.id = evaluation.getId();
        this.lecture.setText(evaluation.getLectureName());
        this.timestamp.setText(DateTimeUtil.timeago(context, evaluation.getCreatedAt()));
        this.category.setText(context.getString(R.string.category_major)); // TODO -> evaluation.category
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), evaluation.getProfessorName(), " "+context.getResources().getString(R.string.professor_postfix))));
        Picasso.with(this.itemView.getContext()).load(evaluation.getAvatarUrl()).transform(new CircleTransformation()).into(this.avatar);
        this.nickname.setText(evaluation.getUserNickname());
        this.body.setText(evaluation.getBody());
        this.pointOverallPrefix.setText(R.string.label_point_overall);
        this.setPointRating(this.pointOverallPrefix, this.pointOverallRating, this.pointOverallText, evaluation.getPointOverall());
        this.pointClarityPrefix.setText(R.string.label_point_clarity);
        this.setPointProgress(this.pointClarityPrefix, this.pointClarityProgress, this.pointClarityText, evaluation.getPointClarity());
        this.pointGpaSatisfactionPrefix.setText(R.string.label_point_gpa_satisfaction);
        this.setPointProgress(this.pointGpaSatisfactionPrefix, this.pointGpaSatisfactionProgress, this.pointGpaSatisfactionText, evaluation.getPointGpaSatisfaction());
        this.pointEasinessPrefix.setText(R.string.label_point_easiness);
        this.setPointProgress(this.pointEasinessPrefix, this.pointEasinessProgress, this.pointEasinessText, evaluation.getPointEasiness());
        this.hashtags.removeAllViews();
        RetrofitApi.getInstance()
        .get_evaluation_hashtag(User.getInstance().getAccessToken(), this.id)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
                if (response.hashtags != null) this.hashtags.post(() -> {
                    float totalWidth = 0;
                    for (String hashtag : response.hashtags) {
                        Hashtag tag = new Hashtag(this.itemView.getContext(), hashtag);
                        float width = tag.getPaint().measureText((String) tag.getText());
                        if (width + totalWidth > hashtags.getWidth()) break;
                        this.hashtags.addView(tag);
                        totalWidth += width;
                    }
                });
            }
        );

        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_comment_16dp).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.commentIcon);
        this.commentCount.setText(evaluation.getCommentCount() == null || evaluation.getCommentCount() < 0 ? "N/A" : String.valueOf(evaluation.getCommentCount()));

        if(evaluation.getRequestUserVote() == null) this.setStatus(VoteStatus.NONE);
        else if(evaluation.getRequestUserVote() == 1) this.setStatus(VoteStatus.UP);
        else this.setStatus(VoteStatus.DOWN);

        this.setVoteCount(evaluation.getUpVoteCount(), evaluation.getDownVoteCount());
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.evaluation_up_vote_icon:
                if(this.status == VoteStatus.UP) RetrofitApi.getInstance()
                    .delete_evaluation_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.NONE);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                else RetrofitApi.getInstance()
                    .post_evaluation_vote(User.getInstance().getAccessToken(), this.id, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.UP);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);

                    });
                break;
            case R.id.evaluation_down_vote_icon:
                if(this.status == VoteStatus.DOWN) RetrofitApi.getInstance()
                    .delete_evaluation_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.NONE);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                else RetrofitApi.getInstance()
                    .post_evaluation_vote(User.getInstance().getAccessToken(), this.id, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.DOWN);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                break;
            case R.id.evaluation_up_vote_count:
            case R.id.evaluation_down_vote_count:
                RetrofitApi.getInstance()
                    .get_evaluation_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> VotersDialog.show(
                        view.getContext(),
                        view.getId() == R.id.evaluation_up_vote_count ? "UP" : "DOWN",
                        view.getId() == R.id.evaluation_up_vote_count ? response.up : response.down
                    ));
                break;
            default : Timber.d("Clicked view : %s", view);
        }
    }
}
