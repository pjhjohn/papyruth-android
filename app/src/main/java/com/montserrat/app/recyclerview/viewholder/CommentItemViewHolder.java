package com.montserrat.app.recyclerview.viewholder;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.materialdialog.VotersDialog;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.ContrastColorFilterTransformation;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CommentItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.comment_user_avatar) protected ImageView avatar;
    @InjectView (R.id.comment_user_nickname) protected TextView nickname;
    @InjectView (R.id.comment_timestamp) protected TextView timestamp;
    @InjectView (R.id.comment_body) protected TextView body;
    @InjectView (R.id.comment_up_vote_icon) protected ImageView upIcon;
    @InjectView (R.id.comment_up_vote_count) protected TextView upCount;
    @InjectView (R.id.comment_down_vote_icon) protected ImageView downIcon;
    @InjectView (R.id.comment_down_vote_count) protected TextView downCount;
    private int id;
    private VoteStatus status;
    public enum VoteStatus {
        UP, DOWN, NONE
    }

    public CommentItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
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

    public void bind(CommentData comment) {
        this.id = comment.id;

        Picasso.with(this.itemView.getContext()).load(comment.avatar_url).transform(new CircleTransformation()).into(this.avatar);
        this.nickname.setText(comment.user_nickname);
        this.timestamp.setText(DateTimeUtil.convert(comment.updated_at));
        this.body.setText(comment.body);

        if(comment.request_user_vote == null) this.setStatus(VoteStatus.NONE);
        else if(comment.request_user_vote == 1) this.setStatus(VoteStatus.UP);
        else this.setStatus(VoteStatus.DOWN);

        this.setVoteCount(comment.up_vote_count, comment.down_vote_count);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.comment_up_vote_icon:
                if(this.status == VoteStatus.UP) RetrofitApi.getInstance()
                    .delete_comment_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.NONE);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                else RetrofitApi.getInstance()
                    .post_comment_vote(User.getInstance().getAccessToken(), this.id, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.UP);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                break;
            case R.id.comment_down_vote_icon:
                if(this.status == VoteStatus.DOWN) RetrofitApi.getInstance()
                    .delete_comment_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.NONE);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                else RetrofitApi.getInstance()
                    .post_comment_vote(User.getInstance().getAccessToken(), this.id, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        this.setStatus(VoteStatus.DOWN);
                        this.setVoteCount(response.up_vote_count, response.down_vote_count);
                    });
                break;
            case R.id.comment_up_vote_count:
            case R.id.comment_down_vote_count:
                RetrofitApi.getInstance()
                    .get_comment_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> VotersDialog.show(
                        view.getContext(),
                        view.getId() == R.id.comment_up_vote_count ? "UP" : "DOWN",
                        view.getId() == R.id.comment_up_vote_count ? response.up : response.down
                    ));
                break;
            default : Timber.d("Clicked view : %s", view);
        }
    }
}
