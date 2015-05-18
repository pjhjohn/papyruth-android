package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class NavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static NavAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        return new NavAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Holder.Data> items;
    private NavAdapter (List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        NavAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return new Holder(inflater.inflate(R.layout.cardview_category, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nav_item_text) protected TextView category;
        @InjectView(R.id.nav_item_icon) protected ImageView category_image;

        public Holder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(NavAdapter.Holder.Data item) {
            this.category.setText(item.category);
            this.category_image.setImageDrawable(this.category_image.getResources().getDrawable(item.categoryImageResId));
        }

        @Override
        public void onClick (View view) {
            NavAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

        public static class Data {
            public String category;
            int categoryImageResId;
            public Data(String category, int categoryImageResId) {
                this.category = category;
                this.categoryImageResId = categoryImageResId;
            }
        }
    }
}
