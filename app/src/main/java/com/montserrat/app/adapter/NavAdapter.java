package com.montserrat.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.Category;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.squareup.picasso.Picasso;

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

    private static RecyclerViewClickListener itemListener;
    private List<Category> items;
    private Context context;
    public NavAdapter (Context context, List<Category> initItemList, RecyclerViewClickListener listener) {
        this.context = context;
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

    public class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nav_item_icon) protected ImageView icon;
        @InjectView(R.id.nav_item_text) protected TextView category;

        private int filter;
        public CategoryHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
            filter = NavAdapter.this.context.getResources().getColor(R.color.nav_filter);
        }

        public void bind(Category category) {
            Picasso.with(NavAdapter.this.context).load(category.imgResId).transform(new ColorFilterTransformation(this.filter)).into(this.icon);
            this.category.setText(category.name);
        }

        @Override
        public void onClick (View view) {
            NavAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
