package com.montserrat.app.recyclerview.viewholder;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.materialdialog.VotersDialog;
import com.montserrat.utils.support.mpandroidchart.ChartUtil;
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
    @InjectView(R.id.evaluation_lecture_name) protected TextView lectureName;
    @InjectView (R.id.evaluation_timestamp) protected TextView timestamp;
    @InjectView (R.id.evaluation_user_avatar) protected ImageView avatar;
    @InjectView (R.id.evaluation_user_nickname) protected TextView nickname;
    @InjectView (R.id.evaluation_body) protected TextView body;
    @InjectView (R.id.evaluation_chart) protected HorizontalBarChart chart;
    @InjectView (R.id.evaluation_hashtags) protected LinearLayout hashtags;
    @InjectView (R.id.evaluation_up_vote_icon) protected ImageView upIcon;
    @InjectView (R.id.evaluation_up_vote_count) protected TextView upCount;
    @InjectView (R.id.evaluation_down_vote_icon) protected ImageView downIcon;
    @InjectView (R.id.evaluation_down_vote_count) protected TextView downCount;
    @InjectView (R.id.evaluation_comment_icon) protected ImageView commentIcon;
    @InjectView (R.id.evaluation_comment_count) protected TextView commentCount;
    private int id;
    private VoteStatus status;
    public enum VoteStatus {
        UP, DOWN, NONE
    }
    public EvaluationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.lectureName.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.upIcon.setOnClickListener(this);
        this.upCount.setOnClickListener(this);
        this.downIcon.setOnClickListener(this);
        this.downCount.setOnClickListener(this);
        this.setStatus(VoteStatus.NONE);
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

    public void bind(Evaluation evaluation) {
        this.id = evaluation.getId();

        this.lectureName.setText(evaluation.getLectureName());
        this.timestamp.setText(DateTimeUtil.timestamp(evaluation.getCreatedAt()));
        Picasso.with(this.itemView.getContext()).load(evaluation.getAvatarUrl()).transform(new CircleTransformation()).into(this.avatar);
        this.nickname.setText(evaluation.getUserNickname());
        this.body.setText(evaluation.getBody());
        ChartUtil.init(this.chart);
        ChartUtil.bindData(this.chart, evaluation);
        this.chart.animateY(1000);
        this.hashtags.removeAllViews();
        for(int i = 0; i < 5; i ++) this.hashtags.addView(new Hashtag(this.itemView.getContext(), "tag" + i));
        Picasso.with(this.itemView.getContext()).load(R.drawable.ic_light_comment).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.commentIcon);
        this.commentCount.setText(String.valueOf(evaluation.getCommentCount() == null ? 0 : evaluation.getCommentCount()));

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
