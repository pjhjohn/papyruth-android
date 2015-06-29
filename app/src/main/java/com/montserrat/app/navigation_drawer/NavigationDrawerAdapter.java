package com.montserrat.app.navigation_drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link NavigationDrawerFragment NavFragment}
 * as an adapter for List-type {@link RecyclerView} to provide global navigation for the application
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.NavigationDrawerItemViewHolder> {
    private List<NavigationDrawerItem> mData;
    private NavigationDrawerCallback mNavigationDrawerCallback;
    private View mSelectedView;
    private int mSelectedPosition;
    private Context mContext;

    public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> data) {
        mData = data;
        mContext = context;
    }

    public NavigationDrawerCallback getNavigationDrawerCallback() {
        return mNavigationDrawerCallback;
    }

    public void setClickCategoryCallback(NavigationDrawerCallback navigationDrawerCallback) {
        mNavigationDrawerCallback = navigationDrawerCallback;
    }

    @Override
    public NavigationDrawerItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final NavigationDrawerItemViewHolder viewHolder = new NavigationDrawerItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_navigation_drawer_item, parent, false));
        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setOnClickListener(view -> {
            if (mSelectedView != null) mSelectedView.setSelected(false);
            mSelectedPosition = viewHolder.getAdapterPosition();
            view.setSelected(true);
            mSelectedView = view;
            if (mNavigationDrawerCallback != null)
                mNavigationDrawerCallback.onNavigationDrawerItemSelected(viewHolder.getAdapterPosition(), true);
        });
        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerItemViewHolder holder, int position) {
        holder.bind(mData.get(position));
        if (mSelectedPosition == position) {
            if (mSelectedView != null) mSelectedView.setSelected(false);
            mSelectedPosition = position;
            mSelectedView = holder.itemView;
            mSelectedView.setSelected(true);
        }
    }

    public void selectPosition(int position) {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public class NavigationDrawerItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.nav_item_icon) protected ImageView icon;
        @InjectView(R.id.nav_item_text) protected TextView text;
        private int colorFilter;

        public NavigationDrawerItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            this.colorFilter = mContext.getResources().getColor(R.color.nav_filter);
        }

        public void bind(NavigationDrawerItem navigationDrawerItem) {
            Picasso.with(mContext).load(navigationDrawerItem.getDrawableResourceId()).transform(new ColorFilterTransformation(colorFilter)).into(icon);
            text.setText(navigationDrawerItem.getText());
        }
    }
}