package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.model.CourseData;
import com.papyruth.android.recyclerview.viewholder.CourseItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

public class CourseItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener mRecyclerItemClickListener;
    private List<CourseData> mCourseDataList;
    private Integer mHeaderViewHolderLayoutResourceId;
    private boolean mShowPlaceholder;
    private int mPlaceholderTextResId;

    public CourseItemsAdapter(List<CourseData> initialCourses, RecyclerViewItemClickListener listener, int placeholderTextResId) {
        this(initialCourses, listener, null, placeholderTextResId);
    }
    public CourseItemsAdapter(List<CourseData> initialCourses, RecyclerViewItemClickListener listener, Integer resid, int placeholderTextResId) {
        this.mRecyclerItemClickListener = listener;
        this.mCourseDataList = initialCourses;
        this.mHeaderViewHolderLayoutResourceId = resid;
        this.mShowPlaceholder = false;
        this.mPlaceholderTextResId = placeholderTextResId;
    }

    public void setShowPlaceholder(boolean show){
        this.mShowPlaceholder = show;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ViewHolderFactory.ViewType.HEADER) return ViewHolderFactory.getInstance().create(parent, viewType, mRecyclerItemClickListener, mHeaderViewHolderLayoutResourceId);
        else return ViewHolderFactory.getInstance().create(parent, viewType, mRecyclerItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if(mCourseDataList.isEmpty() && mShowPlaceholder) ((PlaceholderViewHolder) holder).bind(this.mPlaceholderTextResId);
        else ((CourseItemViewHolder) holder).bind(this.mCourseDataList.get(position - getItemOffset()));
    }

    public int getItemOffset() {
        return 1 + (mCourseDataList.isEmpty() && mShowPlaceholder ? 1 : 0);
    }
    @Override
    public int getItemCount() {
        return getItemOffset() + (this.mCourseDataList == null ? 0 : this.mCourseDataList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if(position <= 0) return ViewHolderFactory.ViewType.HEADER;
        else if(mCourseDataList.isEmpty() && mShowPlaceholder) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.COURSE_ITEM;
    }
}