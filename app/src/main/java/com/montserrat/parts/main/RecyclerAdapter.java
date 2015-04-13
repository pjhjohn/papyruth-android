package com.montserrat.parts.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.activity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    private List<Data> items;

    public RecyclerAdapter(List<Data> initItemList) {
        this.items = initItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == TYPE_ITEM) {
            final View view = LayoutInflater.from(context).inflate(R.layout.main_item, parent, false);
            return RecyclerItemViewHolder.newInstance(view);
        } else if (viewType == TYPE_HEADER) {
            final View view = LayoutInflater.from(context).inflate(R.layout.main_header, parent, false);
            return new RecyclerHeaderViewHolder(view);
        } else throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d("DEBUG", "View " + this.getClass().toString() + " @" + position);
        if (!isPositionHeader(position)) {
            RecyclerItemViewHolder holder = (RecyclerItemViewHolder) viewHolder;
            Data item = this.items.get(position - 1);
            holder.bind(item);
        }
    }

    // old item count
    public int getBasicItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount() + 1; // 1 is for header.
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public static class Data {
        public String subject;
        public String professor;
        public float rating;
        private Data(){}
        public Data(String subject, String professor, float rating) {
            this.subject = subject;
            this.professor = professor;
            this.rating = rating;
        }
    }
}
