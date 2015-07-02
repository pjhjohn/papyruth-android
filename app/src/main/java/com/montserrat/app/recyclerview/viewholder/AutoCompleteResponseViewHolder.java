package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */

public class AutoCompleteResponseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.type_icon) protected ImageView icon;
    @InjectView(R.id.content) protected TextView content;
    private RecyclerViewItemClickListener itemClickListener;
    public AutoCompleteResponseViewHolder(View parent, RecyclerViewItemClickListener listener) {
        super(parent);
        ButterKnife.inject(this, parent);
        parent.setOnClickListener(this);
        itemClickListener = listener;
    }

    public void bind(Candidate item) {
        if (item.course != null){
            this.content.setText(item.course.name + " - " +item.course.professor_name);
        }else if(item.lecture_name != null) {
            this.icon.setImageResource(R.drawable.ic_dark_lecture);
            this.content.setText(item.lecture_name);
        }else if(item.professor_name != null){
            this.icon.setImageResource(R.drawable.ic_dark_school);
            this.content.setText((item.professor_name));
        }
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}
