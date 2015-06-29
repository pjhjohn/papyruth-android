package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.Course;
import com.montserrat.utils.support.mpandroidchart.ChartUtil;
import com.montserrat.utils.view.Hashtag;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class CourseViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.course_title) protected TextView title;
    @InjectView(R.id.course_professor) protected TextView professor;
    @InjectView(R.id.course_category) protected TextView category;
    @InjectView(R.id.course_thumbnail) protected ImageView thumbnail;
    @InjectView(R.id.course_chart) protected HorizontalBarChart chart;
    @InjectView(R.id.course_hashtags) protected LinearLayout hashtags;
    public CourseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
    }

    public void bind(Course course) {
        this.title.setText(course.getName());
        this.professor.setText(course.getProfessor());
        this.category.setText(""); // TODO : define it!
//        Picasso.with(this.itemView.getContext()).load("").transform(new CircleTransformation()).into(this.thumbnail);
        ChartUtil.init(this.chart);
        ChartUtil.bindData(this.chart, course);
        this.chart.animateY(1000);
        for(String hashtag : course.getHashtags()) this.hashtags.addView(new Hashtag(this.itemView.getContext(), hashtag));
    }
}
