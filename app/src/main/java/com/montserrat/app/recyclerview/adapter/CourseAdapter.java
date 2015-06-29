package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.recyclerview.viewholder.CourseViewHolder;
import com.montserrat.app.recyclerview.viewholder.EvaluationItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

/**
 * Created by SSS on 2015-04-25.
 */
public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RecyclerViewItemClickListener evaluationItemClickListener; // TODO : use if implemented.
    private List<EvaluationData> evaluations;
    public CourseAdapter(List<EvaluationData> initialEvaluations, RecyclerViewItemClickListener listener) {
        this.evaluations = initialEvaluations;
        this.evaluationItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, evaluationItemClickListener);
    }

    /**
     * @param holder
     * @param position 0 for header, 1 for course, 2+ for evaluations
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        else if (position == 1) ((CourseViewHolder) holder).bind(Course.getInstance());
        else ((EvaluationItemViewHolder) holder).bind(this.evaluations.get(position - 2));
    }

    @Override
    public int getItemCount() {
        return 2 + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        else if (position == 1) return ViewHolderFactory.ViewType.COURSE;
        else return ViewHolderFactory.ViewType.EVALUATION_ITEM;
    }
}
