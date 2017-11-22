package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.android.model.TermData;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class TermViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.term_name) protected TextView mName;
    private final Context mContext;
    public TermViewHolder(View view, RecyclerViewItemClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = view.getContext();
        mName.setPaintFlags(mName.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        if(listener != null) view.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, super.getAdapterPosition()));
    }

    public void bind(TermData osl) {
        mName.setText(osl.name);
    }
}
