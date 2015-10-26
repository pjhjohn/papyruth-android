package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class InformViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.inform_body) protected TextView body;
    @InjectView(R.id.inform_btn_positive) protected Button positiveButton;
    @InjectView(R.id.inform_btn_optional) protected Button optionalButton;
    public InformViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        positiveButton.setOnClickListener(view -> {
            if (listener != null) listener.onRecyclerViewItemClick(view, this.getAdapterPosition());
        });
        optionalButton.setOnClickListener(view -> {
            if (listener != null) listener.onRecyclerViewItemClick(view, this.getAdapterPosition());
        });
    }

    public void bind(int bodyResourceId) {
        this.body.setText(this.itemView.getContext().getString(bodyResourceId));
    }
}
