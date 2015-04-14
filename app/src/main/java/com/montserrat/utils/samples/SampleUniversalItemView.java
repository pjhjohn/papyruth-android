package com.montserrat.utils.samples;

import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.activity.R;
import com.montserrat.utils.adapter.InvokeView;
import com.montserrat.utils.adapter.UniversalItemView;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class SampleUniversalItemView extends UniversalItemView<SampleUniversalItemView.Holder, SampleUniversalItemView.Data> {
    public SampleUniversalItemView (Data data) {
        super(data);
        this.viewHolder = new Holder();
    }

    @Override
    public void mappingData (Holder holder, Data entity) {
        holder.subject.setText(entity.subject);
        holder.professor.setText(entity.professor);
        holder.rating.setRating(entity.rating);
    }

    @Override
    public int getLayoutId() {
        return R.layout.recycler_item_main;
    }

    public static class Data {
        public String subject;
        public String professor;
        public float rating;

        public Data (){}
        public Data (String subject, String professor, float rating) {
            this.subject = subject;
            this.professor = professor;
            this.rating = rating;
        }
    }

    public static class Holder {
        @InvokeView (viewId = R.id.main_item_subject)
        public TextView subject;

        @InvokeView(viewId = R.id.main_item_professor)
        public TextView professor;

        @InvokeView(viewId = R.id.main_item_rating)
        public RatingBar rating;
    }
}