package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.OpenSourceLicenseData;
import com.montserrat.app.recyclerview.viewholder.CourseItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.OpenSourceLicenseViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link SimpleCourseFragment SearchCourseFragment}
 * as an adapter for List-type {@link RecyclerView} to provide course search result
 */
public class OpenSourceLicensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener itemClickListener;
    private List<OpenSourceLicenseData> data;

    public OpenSourceLicensesAdapter(List<OpenSourceLicenseData> initialCourses, RecyclerViewItemClickListener listener) {
        this.data = initialCourses;
        this.itemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, itemClickListener::onRecyclerViewItemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        ((OpenSourceLicenseViewHolder) holder).bind(this.data.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return 1 + (this.data == null ? 0 : this.data.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position <= 0 ? ViewHolderFactory.ViewType.HEADER : ViewHolderFactory.ViewType.OPEN_SOURCE_LICENSE;
    }
}