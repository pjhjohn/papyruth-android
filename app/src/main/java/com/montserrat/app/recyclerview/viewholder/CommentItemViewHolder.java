package com.montserrat.app.recyclerview.viewholder;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.support.picasso.CircleWithBorderTransformation;
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
    private static final int colorPositive = Color.rgb(100, 100, 250), colorNegative = Color.rgb(250, 100, 100), colorNeutral = Color.rgb(100, 100, 100);
    @InjectView(R.id.comment_user_avatar) protected ImageView avatar;
    @InjectView (R.id.comment_user_nickname) protected TextView nickname;
    @InjectView (R.id.comment_timestamp) protected TextView timestamp;
    @InjectView (R.id.comment_body) protected TextView body;
    @InjectView (R.id.comment_up_vote_icon) protected ImageView upIcon;
    @InjectView (R.id.comment_up_vote_count) protected TextView upCount;
    @InjectView (R.id.comment_down_vote_icon) protected ImageView downIcon;
    @InjectView (R.id.comment_down_vote_count) protected TextView downCount;
    private RecyclerViewItemClickListener itemClickListener;
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
        itemClickListener = listener;
        this.upIcon.setOnClickListener(this);
        this.upCount.setOnClickListener(this);
        this.downIcon.setOnClickListener(this);
        this.downCount.setOnClickListener(this);
        this.setStatus(VoteStatus.NONE);
    }

    private void setStatus(VoteStatus newStatus) {
        this.status = newStatus;

        Picasso.with(this.itemView.getContext()).load(R.drawable.avatar_dummy).transform(new CircleWithBorderTransformation(status == VoteStatus.UP?colorPositive:colorNeutral, R.drawable.ic_light_add)).into(this.upIcon);
        this.upCount.setTextColor(status == VoteStatus.UP ? colorPositive : colorNeutral);

        Picasso.with(this.itemView.getContext()).load(R.drawable.avatar_dummy).transform(new CircleWithBorderTransformation(status == VoteStatus.DOWN?colorNegative:colorNeutral, R.drawable.ic_light_clear)).into(this.downIcon);
        this.downCount.setTextColor(status == VoteStatus.DOWN ? colorNegative : colorNeutral);
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

        this.upCount.setText(String.valueOf(comment.up_vote_count == null ? 0 : comment.up_vote_count));
        this.downCount.setText(String.valueOf(comment.down_vote_count == null ? 0 : comment.down_vote_count));
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.comment_up_vote_icon:
            case R.id.comment_up_vote_count:
                if(this.status == VoteStatus.UP) RetrofitApi.getInstance()
                    .delete_comment_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> this.setStatus(VoteStatus.NONE));
                else RetrofitApi.getInstance()
                    .post_comment_vote(User.getInstance().getAccessToken(), this.id, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> this.setStatus(VoteStatus.UP));
                break;
            case R.id.comment_down_vote_icon:
            case R.id.comment_down_vote_count:
                if(this.status == VoteStatus.DOWN) RetrofitApi.getInstance()
                    .delete_comment_vote(User.getInstance().getAccessToken(), this.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> this.setStatus(VoteStatus.NONE));
                else RetrofitApi.getInstance()
                    .post_comment_vote(User.getInstance().getAccessToken(), this.id, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> this.setStatus(VoteStatus.DOWN));
                break;
            default:
                Timber.d("Clicked view : %s", view);
                throw new RuntimeException("Unexpected Event");
        } //itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}
