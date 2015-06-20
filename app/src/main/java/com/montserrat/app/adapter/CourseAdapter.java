package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-04-25.
 */
public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static CourseAdapter newInstance(List<EvaluationData> initItemList, RecyclerViewClickListener listener) {
        return new CourseAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener; // TODO : use if implemented.

    private List<EvaluationData> items;
    private CourseAdapter(List<EvaluationData> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        CourseAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM: return new SimpleEvaluationHolder(inflater.inflate(R.layout.cardview_evaluation, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SimpleEvaluationHolder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }


    public static class SimpleEvaluationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nickname) protected TextView nickname;
        @InjectView(R.id.body) protected TextView comment;
        @InjectView(R.id.like) protected Button like;
        @InjectView(R.id.date) protected TextView date;
        @InjectView(R.id.point_overall) protected TextView pointOverall;
//        @InjectView(R.id.point_gpa_satisfaction) protected SeekBar pointSatisfaction;
//        @InjectView(R.id.point_clarity) protected SeekBar pointClarity;
//        @InjectView(R.id.point_easiness) protected SeekBar pointEasiness;
        public SimpleEvaluationHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(EvaluationData item) {
            this.nickname.setText(item.professor_name);   // TODO : user nickname should be contained in SimpleEvaluation
            this.comment.setText(item.body);
            this.like.setText(item.point_overall+""); // TODO : # of likes should be contained in SimpleEvaluation
        }

        @Override
        public void onClick(View v) {
            CourseAdapter.itemListener.recyclerViewListClicked(v, this.getPosition());
        }

    }
}
