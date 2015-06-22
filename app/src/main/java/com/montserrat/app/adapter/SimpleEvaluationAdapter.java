package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.main.HomeFragment HomeFragment}
 * as an adapter for List-type {@link RecyclerView} to provide latest evaluations to user
 */
public class SimpleEvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 1;
        public static final int SIMPLE_EVALUATION = 2;
    }

    private RecyclerViewClickListener simpleEvaluationItemClickListener;
    private List<EvaluationData> evaluations;
    public SimpleEvaluationAdapter(List<EvaluationData> initialEvaluations, RecyclerViewClickListener listener) {
        this.evaluations = initialEvaluations;
        this.simpleEvaluationItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new HeaderViewHolder(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.SIMPLE_EVALUATION : return new SimpleEvaluationViewHolder(inflater.inflate(R.layout.cardview_evaluation_less_simple, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        ((SimpleEvaluationViewHolder) holder).bind(this.evaluations.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return 1 + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? Type.HEADER : Type.SIMPLE_EVALUATION;
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected class SimpleEvaluationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.professor) protected TextView professor;
        @InjectView (R.id.lecture) protected TextView lecture;
        @InjectView (R.id.evaluation_body) protected TextView comment;
        @InjectView (R.id.point_overall) protected RatingBar overall;

        public SimpleEvaluationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(EvaluationData evaluation) {
            this.professor.setText(evaluation.professor_name);
            this.lecture.setText(evaluation.lecture_name);
            this.comment.setText(evaluation.body);
            this.overall.setRating(evaluation.point_overall);
        }

        @Override
        public void onClick (View view) {
            simpleEvaluationItemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 1);
        }
    }
}
