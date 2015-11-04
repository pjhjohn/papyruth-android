package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.recyclerview.viewholder.CourseItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.NoDataViewHolder;
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
    private Integer headerLayoutResourceId;
    private int resIdNoDataText;
    private boolean isEmptyData;

    public CourseItemsAdapter(List<CourseData> initialCourses, RecyclerViewItemClickListener listener, int noDataTextRes) {
        this(initialCourses, listener, null, noDataTextRes);
    }
    public CourseItemsAdapter(List<CourseData> initialCourses, RecyclerViewItemClickListener listener, Integer headerLayoutResourceId, int resIdNoDataText) {
        this.courses = initialCourses;
        this.courseItemClickListener = listener;
        this.headerLayoutResourceId = headerLayoutResourceId;
        this.resIdNoDataText = resIdNoDataText;
        this.isEmptyData = false;
    }

    public void setIsEmptyData(boolean isEmptyData){
        this.isEmptyData = isEmptyData;
    }

    public void setResIdNoDataText(int resIdNoDataText){
        this.resIdNoDataText = resIdNoDataText;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ViewHolderFactory.ViewType.HEADER)
            return ViewHolderFactory.getInstance().create(parent, viewType, courseItemClickListener, headerLayoutResourceId);
        else return ViewHolderFactory.getInstance().create(parent, viewType, courseItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if(courses.isEmpty() && isEmptyData)
            ((NoDataViewHolder) holder).bind(this.resIdNoDataText);
        else
            ((CourseItemViewHolder) holder).bind(this.courses.get(position - 1));
    }

    public int getItemOffset() {
        return 1 +(courses.isEmpty() && isEmptyData ? 1 : 0);
    }
    @Override
    public int getItemCount() {
        return getItemOffset() + (this.courses == null ? 0 : this.courses.size());
    }

    @Override
    public int getItemViewType(int position) {
        if(position <= 0) return ViewHolderFactory.ViewType.HEADER;
        else if(courses.isEmpty() && isEmptyData) return ViewHolderFactory.ViewType.NO_DATA;
        else return ViewHolderFactory.ViewType.COURSE_ITEM;
    }
}