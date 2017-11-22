package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class InformViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.cardview_inform)     protected RelativeLayout mRoot;
    @BindView(R.id.inform_body)         protected TextView mBody;
    @BindView(R.id.inform_btn_positive) protected Button mButtonPositive;
    @BindView(R.id.inform_btn_optional) protected Button mButtonOptional;
    private final Context mContext;
    public InformViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        if(listener != null) {
            mButtonPositive.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
            mButtonOptional.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
        }
    }

    public void bind(int bodyResourceId) {
        bind(bodyResourceId, 0);
    }

    public void bind(int bodyResourceId, int colorResourceId) {
        if(colorResourceId != 0) mRoot.setBackgroundColor(mContext.getResources().getColor(colorResourceId));
        mBody.setText(mContext.getString(bodyResourceId));
    }
}
