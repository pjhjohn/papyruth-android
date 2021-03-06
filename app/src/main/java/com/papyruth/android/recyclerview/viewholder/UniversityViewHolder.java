package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.android.model.UniversityData;
import com.papyruth.support.utility.customview.SquareImageView;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class UniversityViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.university_item_name)  protected TextView mUniversityName;
    @BindView(R.id.university_item_image) protected SquareImageView mUniversityImage;
    private final Context mContext;
    public UniversityViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(UniversityData university) {
        mUniversityName.setText(university.name);
        Picasso.with(mContext).load(university.image_url).into(mUniversityImage);
    }
}
