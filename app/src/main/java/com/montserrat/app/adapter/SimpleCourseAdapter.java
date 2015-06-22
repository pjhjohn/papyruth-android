package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.CourseData;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link SimpleCourseFragment SearchCourseFragment}
 * as an adapter for List-type {@link RecyclerView} to provide course search result
 */
public class SimpleCourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 0;
        public static final int SIMPLE_COURSE = 1;
    }

    private RecyclerViewClickListener courseItemClickListener;
    private List<CourseData> courses;

    public SimpleCourseAdapter(List<CourseData> initialCourses, RecyclerViewClickListener listener) {
        this.courses = initialCourses;
        this.courseItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new HeaderViewHolder(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.SIMPLE_COURSE: return new SimpleCourseViewHolder(inflater.inflate(R.layout.cardview_course_simple, parent, false));
            default : throw new RuntimeException("There is no ViewHolder availiable for type#" + viewType + " Make sure you're using valid type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        ((SimpleCourseViewHolder) holder).bind(this.courses.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return 1 + (this.courses == null ? 0 : this.courses.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position <= 0 ? Type.HEADER : Type.SIMPLE_COURSE;
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected class SimpleCourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.lecture) protected TextView lecture;
        @InjectView (R.id.professor) protected TextView professor;
        @InjectView (R.id.point_overall) protected RatingBar overall;
        public SimpleCourseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(CourseData item) {
            this.lecture.setText(item.name);
            this.professor.setText(item.professor_name);
            this.overall.setRating(item.point_overall);
        }

        @Override
        public void onClick (View view) {
            courseItemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 1);
        }
    }
}
