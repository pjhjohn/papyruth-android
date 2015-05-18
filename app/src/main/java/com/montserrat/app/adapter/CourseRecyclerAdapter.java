package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-04-25.
 */
public class CourseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 1;
        public static final int ITEM = 2;
    }
    public static CourseRecyclerAdapter newInstance(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        return new CourseRecyclerAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener; // TODO : use if implemented.

    private List<PartialEvaluation> items;
    private CourseRecyclerAdapter(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        CourseRecyclerAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new Header(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.ITEM   : return new Holder(inflater.inflate(R.layout.cardview_evaluation_reply, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) return;
        ((Holder) holder).bind(this.items.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 1 : this.items.size()+1; // 1 for HEADER
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0? Type.HEADER : Type.ITEM;
    }

    public static class Header extends RecyclerView.ViewHolder {
        public Header(View parent) {
            super(parent);
        }
    }

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nickname) protected TextView nickname;
        @InjectView(R.id.comment) protected TextView comment;
        @InjectView(R.id.like) protected Button like;
        public Holder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(PartialEvaluation item) {
            this.nickname.setText(item.user_id);   // TODO : user nickname should be contained in PartialEvaluation
            this.comment.setText(item.comment);
            this.like.setText(item.point_overall); // TODO : # of likes should be contained in PartialEvaluation
        }

        @Override
        public void onClick(View v) {
            // TODO : implement it.
        }
    }
}
