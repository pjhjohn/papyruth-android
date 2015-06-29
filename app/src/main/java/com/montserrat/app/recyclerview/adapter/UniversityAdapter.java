package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.model.UniversityData;
import com.montserrat.app.recyclerview.viewholder.UniversityViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.auth.SignUpStep1Fragment SignUpStep1Fragment}
 * as an adapter for Grid-type {@link RecyclerView} to provide university selection to user
 */
public class UniversityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener universityItemClickListener;
    private List<UniversityData> universities;
    public UniversityAdapter(List<UniversityData> initialUniversities, RecyclerViewItemClickListener listener) {
        this.universities = initialUniversities;
        this.universityItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, universityItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UniversityViewHolder) holder).bind(this.universities.get(position));
    }

    @Override
    public int getItemCount() {
        return this.universities == null ? 0 : this.universities.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ViewHolderFactory.ViewType.UNIVERSITY;
    }
}
