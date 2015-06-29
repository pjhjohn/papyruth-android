package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.montserrat.app.model.unique.Evaluation;

import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class InformViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public InformViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bind(Evaluation evaluation) {

    }

    @Override
    public void onClick(View view) {

    }
}
