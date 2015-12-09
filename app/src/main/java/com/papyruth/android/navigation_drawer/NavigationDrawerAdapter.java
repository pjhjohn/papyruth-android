package com.papyruth.android.navigation_drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link NavigationDrawerFragment NavFragment}
 * as an adapter for List-type {@link RecyclerView} to provide global navigation for the application
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<NavigationDrawerItem> mNavigationDrawerItems;
    private NavigationDrawerCallback mNavigationDrawerCallback;
    private View mSelectedView;
    private int mSelectedViewHolderPosition;
    private Context mContext;

    private static final int POSITION_SEPARATOR = 2;

    public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> data) {
        mNavigationDrawerItems = data;
        mContext = context;
    }

    public NavigationDrawerCallback getNavigationDrawerCallback() {
        return mNavigationDrawerCallback;
    }

    public void setClickCategoryCallback(NavigationDrawerCallback navigationDrawerCallback) {
        mNavigationDrawerCallback = navigationDrawerCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case NavigationDrawerItemSeparatorViewHolder.VIEWTYPE :
                return new NavigationDrawerItemSeparatorViewHolder(LayoutInflater.from(mContext).inflate(R.layout.hr_drawer, parent, false));
            case NavigationDrawerItemViewHolder.VIEWTYPE :
                RecyclerView.ViewHolder viewholder = new NavigationDrawerItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.cardview_navigation_drawer_item, parent, false));
                viewholder.itemView.setClickable(true);
                viewholder.itemView.setOnClickListener(view -> {
                    if (mSelectedView != null) mSelectedView.setSelected(false);
                    mSelectedViewHolderPosition = viewholder.getAdapterPosition();
                    mSelectedView = view;
                    if (mNavigationDrawerCallback != null) mNavigationDrawerCallback.onNavigationDrawerItemSelected(mSelectedViewHolderPosition, true);
                });
                return viewholder;
        } return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position == POSITION_SEPARATOR) return;
        ((NavigationDrawerItemViewHolder) holder).bind(mNavigationDrawerItems.get(this.getItemsPosition(position)));
        if (mSelectedViewHolderPosition == position) {
            if (mSelectedView != null) mSelectedView.setSelected(false);
            mSelectedView = holder.itemView;
            mSelectedView.setSelected(true);
        }
    }

    public int getItemsPosition(int position) {
        return position > POSITION_SEPARATOR ? position - 1 : position;
    }

    public void selectPosition(int position) {
        mSelectedViewHolderPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position == POSITION_SEPARATOR ? NavigationDrawerItemSeparatorViewHolder.VIEWTYPE : NavigationDrawerItemViewHolder.VIEWTYPE;
    }

    @Override
    public int getItemCount() {
        return 1 + (mNavigationDrawerItems != null ? mNavigationDrawerItems.size() : 0);
    }

    /* ViewHolders */
    public class NavigationDrawerItemSeparatorViewHolder extends RecyclerView.ViewHolder {
        public static final int VIEWTYPE = 0x1;
        public NavigationDrawerItemSeparatorViewHolder(View view) {
            super(view);
        }
    }
    public class NavigationDrawerItemViewHolder extends RecyclerView.ViewHolder {
        public static final int VIEWTYPE = 0x2;
        @Bind(R.id.navigation_drawer_item_icon)  protected ImageView mNavigationDrawerItemIcon; // 54% #000000
        @Bind(R.id.navigation_drawer_item_label) protected TextView mNavigationDrawerItemLabel; // Roboto Medium, 14sp, 87% #000000
        private int mColorIconMaterial;
        private final Context mContext;

        public NavigationDrawerItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
            mColorIconMaterial = mContext.getResources().getColor(R.color.icon_material);
        }

        public void bind(NavigationDrawerItem navigationDrawerItem) {
            Picasso.with(mContext).load(navigationDrawerItem.getDrawableResourceId()).transform(new ColorFilterTransformation(mColorIconMaterial)).into(mNavigationDrawerItemIcon);
            mNavigationDrawerItemLabel.setText(navigationDrawerItem.getLabel());
        }
    }
}