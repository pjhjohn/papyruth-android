package com.montserrat.parts.auth;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.activity.R;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class UniversityRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static UniversityRecyclerAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        return new UniversityRecyclerAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Holder.Data> items;
    private UniversityRecyclerAdapter (List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        UniversityRecyclerAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.recycler_item_university, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Holder holder = (Holder) viewHolder;
        Holder.Data item = this.items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }

    /* Item of list-like recyclerview : WILL BE DISPLAYED AS GRIDVIEW-ELEMENT */
    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView school;
        private Holder(final View parent, TextView school) {
            super(parent);
            this.school = school;
            parent.setOnClickListener(this);
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            TextView school = (TextView) parent.findViewById(R.id.school_item_school);
            return new Holder(parent, school);
        }

        public void bind(UniversityRecyclerAdapter.Holder.Data item) {
            this.school.setText(item.school);
        }

        @Override
        public void onClick (View view) {
            UniversityRecyclerAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

        public static class Data {
            public String school;
            public int schoolCode;
            private Data(){}
            public Data(String school, int schoolCode) {
                this.school = school;
                this.schoolCode = schoolCode;
            }
        }
    }
}
