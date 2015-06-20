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
        public static final int SIMPLE_COURSE = 1;
    }

    private RecyclerViewClickListener itemListener;
    private List<CourseData> items;

    public SimpleCourseAdapter(List<CourseData> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        this.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.SIMPLE_COURSE: return new SimpleCourseHolder(inflater.inflate(R.layout.cardview_course_simple, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SimpleCourseHolder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.SIMPLE_COURSE;
    }

    protected class SimpleCourseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.lecture) protected TextView lecture;
        @InjectView (R.id.professor) protected TextView professor;
        @InjectView (R.id.point_overall) protected RatingBar overall;
        public SimpleCourseHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(CourseData item) {
            this.lecture.setText(item.name);
            this.professor.setText(item.professor_name);
            this.overall.setRating(item.point_overall);
        }

        @Override
        public void onClick (View view) {
            SimpleCourseAdapter.this.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
