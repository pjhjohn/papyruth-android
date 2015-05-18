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
 * Created by pjhjohn on 2015-04-13.
 */
public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 1;
        public static final int ITEM = 2;
    }
    public static HomeAdapter newInstance(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        return new HomeAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<PartialEvaluation> items;
    private HomeAdapter (List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        HomeAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new Header(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.ITEM   : return new Holder(inflater.inflate(R.layout.cardview_evaluation_partial, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) return; // Header
        ((Holder) holder).bind(this.items.get(position - 1));
    }

    // old item count
    public int getBasicItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount() + 1; // 1 is for header.
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? Type.HEADER : Type.ITEM;
    }

    // ViewHolders
    protected class Header extends RecyclerView.ViewHolder {
        public Header(View parent) { super(parent); }
    }

    /* Item of list-like recyclerview */
    protected class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.professor) protected TextView professor;
        @InjectView (R.id.lecture) protected TextView lecture;
        @InjectView (R.id.comment) protected TextView comment;
        @InjectView (R.id.point_overall) protected RatingBar rating;

        public Holder(View parent) {
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
            HomeAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
