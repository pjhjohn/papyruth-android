package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.recyclerview.viewholder.CourseItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link SimpleCourseFragment SearchCourseFragment}
 * as an adapter for List-type {@link RecyclerView} to provide course search result
 */
public class CourseItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener courseItemClickListener;
    private List<CourseData> items;
    private int head;

    public CourseItemsAdapter(List<CourseData> initialCourses, RecyclerViewItemClickListener listener) {
        this.items = initialCourses;
        this.courseItemClickListener = listener;
        head = 1;
    }

    public void setHead(boolean isHead){
        if(isHead)
            this.head = 1;
        else
            this.head = 0;
        Timber.d("***head : %d %s", this.head, isHead);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, courseItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (this.head > 0 && position <= 0) return;
        ((CourseItemViewHolder) holder).setHead(this.head);
        ((CourseItemViewHolder) holder).bind(this.items.get(position - head));
    }

    @Override
    public int getItemCount() {
        return head + (this.items == null ? 0 : this.items.size());
    }

    @Override
    public int getItemViewType(int position) {
        return (this.head > 0 && position <= 0) ? ViewHolderFactory.ViewType.HEADER : ViewHolderFactory.ViewType.COURSE_ITEM;
    }
}
