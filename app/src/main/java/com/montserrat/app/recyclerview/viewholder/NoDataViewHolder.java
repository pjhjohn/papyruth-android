package com.montserrat.app.recyclerview.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class NoDataViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.no_data_text) protected TextView body;
    public NoDataViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
    }

    public void bind(int bodyResourceId) {
        this.body.setText(this.itemView.getContext().getString(bodyResourceId));
    }
    public void bind(String bodyText) {
        this.body.setText(bodyText);
    }
}
