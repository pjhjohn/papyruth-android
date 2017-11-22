package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.papyruth.android.R;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class FooterViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.footer_fully_loaded_indicator_icon) protected ImageView mFullyLoadedIndicatorIcon;
    private final Context mContext;
    public FooterViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
        mContext = view.getContext();
        Picasso.with(mContext).load(R.drawable.ic_scroll_to_top_24dp).transform(new ColorFilterTransformation(mContext.getResources().getColor(R.color.icon_material))).into(mFullyLoadedIndicatorIcon);
    }
}
