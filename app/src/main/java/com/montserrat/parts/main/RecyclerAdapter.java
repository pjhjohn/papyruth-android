package com.montserrat.parts.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.activity.R;
import com.montserrat.utils.recycler.RecyclerHeaderViewHolder;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    private List<Data> items;

    public RecyclerAdapter(List<Data> initItemList) {
        this.items = initItemList;
    }

    public static RecyclerAdapter newInstance(List<Data> initItemList) {
        return new RecyclerAdapter(initItemList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == TYPE_ITEM) {
            final View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_main, parent, false);
            return Holder.newInstance(view);
        } else if (viewType == TYPE_HEADER) {
            final View view = LayoutInflater.from(context).inflate(R.layout.recycler_header_empty, parent, false);
            return new RecyclerHeaderViewHolder(view);
        } else throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d("DEBUG", "View " + this.getClass().toString() + " @" + position);
        if (!isPositionHeader(position)) {
            Holder holder = (Holder) viewHolder;
            Data item = this.items.get(position - 1);
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
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public static class Data {
        public String subject;
        public String professor;
        public float rating;
        private Data(){}
        public Data(String subject, String professor, float rating) {
            this.subject = subject;
            this.professor = professor;
            this.rating = rating;
        }
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView subject;
        private final TextView professor;
        private final RatingBar rating;

        public Holder(final View parent, TextView viewSubject, TextView viewProfessor, RatingBar viewRating) {
            super(parent);
            this.subject = viewSubject;
            this.professor = viewProfessor;
            this.rating = viewRating;
        }

        public static Holder newInstance(View parent) {
            TextView vSubject = (TextView) parent.findViewById(R.id.main_item_subject);
            TextView vProfessor = (TextView) parent.findViewById(R.id.main_item_professor);
            RatingBar vRating = (RatingBar) parent.findViewById(R.id.main_item_rating);
            return new Holder(parent, vSubject, vProfessor, vRating);
        }

        public void bind(RecyclerAdapter.Data item) {
            this.subject.setText(item.subject);
            this.professor.setText(item.professor);
            this.rating.setRating(item.rating);
        }

        public void setSubjectData(CharSequence text) {
            this.subject.setText(text);
        }

        public void setProfessorData(CharSequence text) {
            this.professor.setText(text);
        }

        public void setRatingData(float rate) {
            this.rating.setRating(rate);
        }
    }
}
