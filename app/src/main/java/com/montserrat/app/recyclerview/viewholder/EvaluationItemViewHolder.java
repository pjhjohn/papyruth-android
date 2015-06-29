package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
// TODO : UP / DOWN vote
public class EvaluationItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//    @InjectView(R.id.evaluation_user_avatar) protected ImageView avatar;
    @InjectView(R.id.evaluation_user_nickname) protected TextView nickname;
    @InjectView(R.id.evaluation_body) protected TextView body;
    @InjectView(R.id.evaluation_point_overall) protected RatingBar overall;
    RecyclerViewItemClickListener itemClickListener;
    public EvaluationItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
    }

    public void bind(EvaluationData evaluation) {
//        Picasso.with(this.itemView.getContext()).load("").transform(new CircleTransformation()).into(this.avatar); // TODO : Needs evaluation.user_avatar_url
//        this.nickname.setText(evaluation.user_name) // TODO : Needs evaluation.user_name
        this.body.setText(evaluation.body);
        this.overall.setRating((float)evaluation.point_overall); // TODO : More clear value
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 2);
    }
}