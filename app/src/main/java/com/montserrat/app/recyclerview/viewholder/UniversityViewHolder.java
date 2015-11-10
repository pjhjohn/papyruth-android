package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.UniversityData;
import com.montserrat.utils.view.SquareImageView;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class UniversityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.university_item_name) protected TextView name;
    @InjectView(R.id.university_item_image) protected SquareImageView image;
    private RecyclerViewItemClickListener itemClickListener;
    private final Context context;
    public UniversityViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        this.context = itemView.getContext();
        itemView.setOnClickListener(this);
        this.itemClickListener = listener;
    }

    public void bind(UniversityData universityData) {
        this.name.setText(universityData.name);
        Picasso.with(context).load(universityData.image_url).into(this.image);
    }

    @Override
    public void onClick(View view) {
        this.itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}
