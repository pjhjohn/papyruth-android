package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.OpenSourceLicenseData;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class OpenSourceLicenseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.osl_icon) ImageView icon;
    @InjectView(R.id.osl_name) TextView name;
    OpenSourceLicenseData data;
    RecyclerViewItemClickListener itemClickListener;
    public OpenSourceLicenseViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        itemClickListener = listener;
        this.name.setPaintFlags(this.name.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public void bind(OpenSourceLicenseData osl) {
        final Context context = this.itemView.getContext();
        this.data = osl;
        this.name.setText(osl.name);
        Picasso.with(context).load(osl.repoIconResId).transform(new ColorFilterTransformation(Color.GRAY)).into(this.icon);
    }

    @Override
    public void onClick (View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition() - 1);
    }
}