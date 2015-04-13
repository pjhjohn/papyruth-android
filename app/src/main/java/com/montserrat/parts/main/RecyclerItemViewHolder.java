package com.montserrat.parts.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.activity.R;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class RecyclerItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView subject;
    private final TextView professor;
    private final RatingBar rating;

    public RecyclerItemViewHolder(final View parent, TextView viewSubject, TextView viewProfessor, RatingBar viewRating) {
        super(parent);
        this.subject = viewSubject;
        this.professor = viewProfessor;
        this.rating = viewRating;
    }

    public static RecyclerItemViewHolder newInstance(View parent) {
        TextView vSubject = (TextView) parent.findViewById(R.id.main_item_subject);
        TextView vProfessor = (TextView) parent.findViewById(R.id.main_item_professor);
        RatingBar vRating = (RatingBar) parent.findViewById(R.id.main_item_rating);
        return new RecyclerItemViewHolder(parent, vSubject, vProfessor, vRating);
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
