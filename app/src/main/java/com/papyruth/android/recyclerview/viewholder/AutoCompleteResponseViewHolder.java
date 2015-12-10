package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.android.model.Candidate;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by pjhjohn on 2015-06-29.
 */

public class AutoCompleteResponseViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.type_icon) protected ImageView mIcon;
    @Bind(R.id.content)   protected TextView mContent;
    @Bind(R.id.type_text) protected TextView mTypeText;
    private final Context mContext;
    private final Resources mResources;
    private int mIconColor;

    public AutoCompleteResponseViewHolder(View itemView, RecyclerViewItemClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = itemView.getContext();
        mResources = mContext.getResources();
        mIconColor = mResources.getColor(R.color.icon_material);
        itemView.setBackgroundResource(R.drawable.row_selector);
        itemView.setOnClickListener(v -> listener.onRecyclerViewItemClick(v, getAdapterPosition()));
    }

    public void bind(Candidate item, boolean isHistory) {
        if (item.lecture_id != null && item.professor_id != null){
            Picasso.with(mContext).load( isHistory ? R.drawable.ic_history_24dp : R.drawable.ic_lecture_24dp).transform(new ColorFilterTransformation(mIconColor)).into(mIcon);
            mContent.setText(item.lecture_name + " - " + item.professor_name);
            mTypeText.setBackgroundDrawable(mResources.getDrawable(R.drawable.background_round_stroke_red));
            mTypeText.setText(R.string.word_course);
        }else if(item.lecture_name != null) {
            Picasso.with(mContext).load( isHistory ? R.drawable.ic_history_24dp : R.drawable.ic_lecture_24dp).transform(new ColorFilterTransformation(mIconColor)).into(mIcon);
            mContent.setText(item.lecture_name);
            mTypeText.setBackgroundDrawable(mResources.getDrawable(R.drawable.background_round_stroke_blue));
            mTypeText.setText(R.string.word_lecture);
        }else if(item.professor_name != null){
            Picasso.with(mContext).load( isHistory ? R.drawable.ic_history_24dp : R.drawable.ic_professor_24dp).transform(new ColorFilterTransformation(mIconColor)).into(mIcon);
            mContent.setText(item.professor_name);
            mTypeText.setBackgroundDrawable(mResources.getDrawable(R.drawable.background_round_stroke_green));
            mTypeText.setText(R.string.word_professor);
        }
    }
}
