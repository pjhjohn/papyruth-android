package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.montserrat.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class NoDataViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.no_data_text) protected TextView body;
    private final Context context;
    public NoDataViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.context = itemView.getContext();
    }

    public void bind(int bodyResourceId) {
        this.body.setText(context.getString(bodyResourceId));
    }
    public void bind(String bodyText) {
        this.body.setText(bodyText);
    }
}
