package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class NavRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static NavRecyclerAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        return new NavRecyclerAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Holder.Data> items;
    private NavRecyclerAdapter (List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        NavRecyclerAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.recycler_item_nav, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
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

    /* Item of list-like recyclerview */

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView categoryText;
        private final ImageView categoryIcon;
        private Holder(final View parent, TextView viewCategoryText, ImageView viewCategoryIcon) {
            super(parent);
            this.categoryText = viewCategoryText;
            this.categoryIcon = viewCategoryIcon;
            parent.setOnClickListener(this);
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Holder(
                    parent,
                    (TextView) parent.findViewById(R.id.nav_item_text),
                    (ImageView) parent.findViewById(R.id.nav_item_icon)
            );
        }

        public void bind(NavRecyclerAdapter.Holder.Data item) {
            this.categoryText.setText(item.categoryText);
            this.categoryIcon.setImageDrawable(this.categoryIcon.getResources().getDrawable(item.categoryIconResId));
        }

        @Override
        public void onClick (View view) {
            NavRecyclerAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

        public static class Data {
            public String categoryText;
            int categoryIconResId;
            public Data(String categoryText, int categoryIconResId) {
                this.categoryText = categoryText;
                this.categoryIconResId = categoryIconResId;
            }
        }
    }
}
