package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.recyclerview.viewholder.CourseItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link SimpleCourseFragment SearchCourseFragment}
 * as an adapter for List-type {@link RecyclerView} to provide course search result
 */
public class CourseItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener courseItemClickListener;
    private List<CourseData> courses;

    public CourseItemsAdapter(List<CourseData> initialCourses, RecyclerViewItemClickListener listener) {
        this.courses = initialCourses;
        this.courseItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, courseItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        ((CourseItemViewHolder) holder).bind(this.courses.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return 1 + (this.courses == null ? 0 : this.courses.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position <= 0 ? ViewHolderFactory.ViewType.HEADER : ViewHolderFactory.ViewType.COURSE_ITEM;
    }
}
