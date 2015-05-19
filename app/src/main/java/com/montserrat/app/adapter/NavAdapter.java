package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.Category;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.nav.NavFragment NavFragment}
 * as an adapter for List-type {@link RecyclerView} to provide global navigation for the application
 */
public class NavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int CATEGORY = 1;
    }
    public static NavAdapter newInstance(List<Category> initItemList, RecyclerViewClickListener listener) {
        return new NavAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Category> items;
    private NavAdapter (List<Category> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        NavAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.CATEGORY: return new CategoryHolder(inflater.inflate(R.layout.cardview_category, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CategoryHolder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.CATEGORY;
    }

    public static class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nav_item_text) protected TextView category;
        @InjectView(R.id.nav_item_icon) protected ImageView category_image;

        public CategoryHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(Category category) {
            this.category.setText(category.name);
            this.category_image.setImageDrawable(this.category_image.getResources().getDrawable(category.imgResId));
        }

        @Override
        public void onClick (View view) {
            NavAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
