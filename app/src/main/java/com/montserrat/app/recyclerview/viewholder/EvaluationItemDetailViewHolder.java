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
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.view.DateTimeUtil;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class EvaluationItemDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView (R.id.evaluation_item_avatar) protected ImageView avatar;
    @InjectView (R.id.evaluation_item_nickname) protected TextView nickname;
    @InjectView (R.id.evaluation_item_lecture) protected TextView lecture;
    @InjectView (R.id.evaluation_item_timestamp) protected TextView timestamp;
    @InjectView (R.id.evaluation_item_category) protected TextView category;
    @InjectView (R.id.evaluation_item_professor) protected TextView professor;
    @InjectView (R.id.evaluation_item_point_overall_star) protected RatingBar pointStar;
    @InjectView (R.id.evaluation_item_point_overall_text) protected TextView pointText;
    @InjectView (R.id.evaluation_item_body) protected TextView body;
    private RecyclerViewItemClickListener itemClickListener;
    private final Context context;
    public EvaluationItemDetailViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.context = itemView.getContext();
        itemView.setOnClickListener(this);
        this.itemClickListener = listener;
        this.nickname.setPaintFlags(this.nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.lecture.setPaintFlags(this.lecture.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setPaintFlags(this.category.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.category.setTextColor(context.getResources().getColor(R.color.colorchip_green_highlight));
    }

    private void setRatingBarColor(int color) {
        LayerDrawable stars = (LayerDrawable) pointStar.getProgressDrawable();
        for(int i = 0; i < 3; i ++) stars.getDrawable(i).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        pointText.setTextColor(color);
    }

    private void setPoint(Integer point) {
        final Resources res = this.context.getResources();
        if(point == null || point < 0) this.setRatingBarColor(res.getColor(R.color.inactive));
        else if(point >= 8) this.setRatingBarColor(res.getColor(R.color.point_high));
        else this.setRatingBarColor(res.getColor(R.color.point_low));
        this.pointStar.setRating(point == null || point < 0 ? 5.0f : point/2f);
        this.pointText.setText(point == null || point < 0 ? "0 " : point.toString()+" ");
    }

    public void bind(EvaluationData evaluation) {
        Picasso.with(context).load(evaluation.avatar_url).transform(new CircleTransformation()).into(this.avatar);
        this.category.setText(context.getString(R.string.category_major)); // TODO -> evaluation.category
        this.lecture.setSelected(true);
        this.lecture.setText(evaluation.lecture_name);
        this.professor.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", context.getResources().getString(R.string.professor_prefix), evaluation.professor_name, context.getResources().getString(R.string.professor_postfix))));
        this.timestamp.setText(DateTimeUtil.timeago(context, evaluation.created_at));
        this.body.setText(evaluation.body);
        this.nickname.setText(evaluation.user_nickname);
        this.setPoint(evaluation.point_overall);
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}