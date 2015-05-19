package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.main.HomeFragment HomeFragment}
 * as an adapter for List-type {@link RecyclerView} to provide latest evaluations to user
 */
public class PartialEvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 1;
        public static final int PARTIAL_EVALUATION = 2;
    }
    public static PartialEvaluationAdapter newInstance(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        return new PartialEvaluationAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<PartialEvaluation> items;
    private PartialEvaluationAdapter(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        PartialEvaluationAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new Header(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.PARTIAL_EVALUATION: return new PartialEvaluationHolder(inflater.inflate(R.layout.cardview_evaluation_partial, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) return; // Header
        ((PartialEvaluationHolder) holder).bind(this.items.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 1 : this.items.size()+1; // 1 for HEADER
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? Type.HEADER : Type.PARTIAL_EVALUATION;
    }

    protected class Header extends RecyclerView.ViewHolder {
        public Header(View parent) { super(parent); }
    }

    protected class PartialEvaluationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.professor) protected TextView professor;
        @InjectView (R.id.lecture) protected TextView lecture;
        @InjectView (R.id.comment) protected TextView comment;
        @InjectView (R.id.point_overall) protected RatingBar rating;

        public PartialEvaluationHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(PartialEvaluation evaluation) {
            this.professor.setText(evaluation.professor_name);
            this.lecture.setText(evaluation.lecture_name);
            this.comment.setText(evaluation.comment);
            this.rating.setRating(evaluation.point_overall);
        }

        @Override
        public void onClick (View view) {
            PartialEvaluationAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
