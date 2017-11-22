package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final Context mContext;
    private final Resources mResources;

    public HeaderViewHolder(View view) {
        super(view);
        mContext = view.getContext();
        mResources = mContext.getResources();
    }

    public void bind(int colorRes){
        itemView.setBackgroundColor(mResources.getColor(colorRes));
    }
}
