package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-06-29.
 */

public class AutoCompleteResponseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @InjectView(R.id.type_icon) protected ImageView icon;
    @InjectView(R.id.content) protected TextView content;
    @InjectView(R.id.autocomplete_layout) protected RelativeLayout layout;
    @InjectView(R.id.type_text) protected TextView typeText;
    private RecyclerViewItemClickListener itemClickListener;
    private int iconColor;
    public AutoCompleteResponseViewHolder(View parent, RecyclerViewItemClickListener listener) {
        super(parent);
        ButterKnife.inject(this, parent);
        parent.setOnClickListener(this);
        this.itemClickListener = listener;
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.getPaint().setStyle(Paint.Style.STROKE);
        drawable.getPaint().setColor(0xffdddddd);
        this.layout.setBackgroundDrawable(drawable);
        this.iconColor = parent.getContext().getResources().getColor(R.color.icon_material);
        this.itemView.setBackgroundResource(R.drawable.row_selector);
    }

    public void bind(Candidate item) {
        final Context context = this.itemView.getContext();
        if (item.lecture_id != null && item.professor_id != null){
            Picasso.with(context).load(R.drawable.ic_light_new_evaluation).transform(new ColorFilterTransformation(this.iconColor)).into(this.icon);
            this.content.setText(item.lecture_name + " - " +item.professor_name);
            this.typeText.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_round_stroke_red));
            this.typeText.setText(R.string.word_course);
        }else if(item.lecture_name != null) {
            Picasso.with(context).load(R.drawable.ic_light_lecture).transform(new ColorFilterTransformation(this.iconColor)).into(this.icon);
            this.content.setText(item.lecture_name);
            this.typeText.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_round_stroke_blue));
            this.typeText.setText(R.string.word_lecture);
        }else if(item.professor_name != null){
            Picasso.with(context).load(R.drawable.ic_light_school).transform(new ColorFilterTransformation(this.iconColor)).into(this.icon);
            this.content.setText((item.professor_name));
            this.typeText.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_round_stroke_green));
            this.typeText.setText(R.string.word_professor);
        }
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
    }
}
