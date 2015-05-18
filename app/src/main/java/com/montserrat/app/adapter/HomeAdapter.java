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
            case Type.HEADER : return Header.newInstance(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.cardview_evaluation_brief, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (!isPositionOfHeader(position)) {
            Holder holder = (Holder) viewHolder;
            PartialEvaluation item = this.items.get(position - 1);
            holder.bind(item);
        }
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
        return isPositionOfHeader(position)? Type.HEADER : Type.ITEM;
    }

    private boolean isPositionOfHeader (int position) {
        return position == 0;
    }


    /* Header of list-list recyclerview */
    public static class Header extends RecyclerView.ViewHolder {
        private Header(View itemView) {
            super(itemView);
        }
        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Header(parent);
        }
    }

    /* Item of list-like recyclerview */
    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView professor;
        private final TextView lecture;
        private final TextView comment;
        private final RatingBar rating;
        private Holder(final View parent, TextView professor, TextView lecture, TextView comment, RatingBar rating) {
            super(parent);
            this.professor = professor;
            this.lecture = lecture;
            this.comment = comment;
            this.rating = rating;
            parent.setOnClickListener(this);
        }
        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Holder(
                parent,
                (TextView) parent.findViewById(R.id.professor),
                (TextView) parent.findViewById(R.id.lecture),
                (TextView) parent.findViewById(R.id.comment),
                (RatingBar) parent.findViewById(R.id.point_overall)
            );
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
