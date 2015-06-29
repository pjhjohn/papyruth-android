package com.montserrat.app.recyclerview.viewholder;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CommentItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.comment_user_avatar) protected ImageView avatar;
    @InjectView (R.id.comment_user_nickname) protected TextView nickname;
    @InjectView (R.id.comment_body) protected TextView body;
    RecyclerViewItemClickListener itemClickListener;
    public CommentItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        itemClickListener = listener;
    }

    public void bind(CommentData comment) {
        Picasso.with(this.itemView.getContext()).load(R.drawable.avatar_dummy/*comment.user_avatar_url*/).transform(new CircleTransformation()).into(this.avatar);
        this.nickname.setText(comment.user_nickname);
        this.body.setText(comment.body);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 2);
    }
}
