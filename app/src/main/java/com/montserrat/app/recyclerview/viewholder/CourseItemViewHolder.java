package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.CourseData;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.lecture) protected TextView lecture;
    @InjectView (R.id.professor) protected TextView professor;
    @InjectView (R.id.point_overall) protected RatingBar overall;
    RecyclerViewItemClickListener itemClickListener;
    private int head;
    public CourseItemViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
        this.head = 1;
    }
    public void setHead(int head){
        this.head = head;
    }

    public void bind(CourseData item) {
        this.lecture.setText(item.name);
        this.professor.setText(item.professor_name);
        this.overall.setRating(item.point_overall);
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - head);
    }
}
