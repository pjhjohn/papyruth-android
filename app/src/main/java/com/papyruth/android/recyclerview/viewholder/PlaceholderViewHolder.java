package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.papyruth.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class PlaceholderViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.placeholder_body) protected TextView mBody;
    private final Context mContext;
    public PlaceholderViewHolder(View view) {
        super(view);
        ButterKnife.inject(this, view);
        mContext = view.getContext();
    }

    public void bind(int resid) {
        mBody.setText(resid);
    }
    public void bind(CharSequence text) {
        mBody.setText(text);
    }
}
