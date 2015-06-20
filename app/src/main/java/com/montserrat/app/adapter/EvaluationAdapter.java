package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.CourseData;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-05-08.
 */
public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static EvaluationAdapter newInstance(List<CourseData> initItemList, RecyclerViewClickListener listener) {
        return new EvaluationAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<CourseData> items;
    public EvaluationAdapter (List<CourseData> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        EvaluationAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return new Holder(inflater.inflate(R.layout.evaluation_list_item, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.lecture) protected TextView lecture;
        @InjectView(R.id.professor) protected TextView professor;
        public Holder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(CourseData item) {
            this.lecture.setText(item.name); // lecture represents the name of course
            this.professor.setText(item.professor_name);
        }

        @Override
        public void onClick (View view) {
            EvaluationAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

    }
}