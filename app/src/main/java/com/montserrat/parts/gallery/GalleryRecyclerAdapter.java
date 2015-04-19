package com.montserrat.parts.gallery;

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
public class GalleryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 1;
        public static final int ITEM = 2;
    }
    public static GalleryRecyclerAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        return new GalleryRecyclerAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Holder.Data> items;
    private GalleryRecyclerAdapter (List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        this.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return Header.newInstance(inflater.inflate(R.layout.recycler_item_school_header, parent, false));
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.recycler_item_school, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d("DEBUG", "View " + this.getClass().toString() + " @" + position);
        if (!isPositionOfHeader(position)) {
            Holder holder = (Holder) viewHolder;
            Holder.Data item = this.items.get(position - 1);
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
        return isPositionOfHeader(position)? Type.HEADER : Type.ITEM;
    }

    private boolean isPositionOfHeader (int position) {
        return position == 0;
    }


    /* Header of list-list recyclerview */
    public static class Header extends RecyclerView.ViewHolder {
        private Header(View itemView) {
            super(itemView);
        }
        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Header(parent);
        }
    }

    /* Item of list-like recyclerview */

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

        public void bind(GalleryRecyclerAdapter.Holder.Data item) {
            this.school.setText(item.school);
        }

        @Override
        public void onClick (View view) {
            GalleryRecyclerAdapter.itemListener.recyclerViewListClicked(view, this.getPosition() - 1); // HEADER
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
