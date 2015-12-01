package com.papyruth.android.navigation_drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.android.recyclerview.viewholder.VoidViewHolder;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link NavigationDrawerFragment NavFragment}
 * as an adapter for List-type {@link RecyclerView} to provide global navigation for the application
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<NavigationDrawerItem> mData;
    private NavigationDrawerCallback mNavigationDrawerCallback;
    private View mSelectedView;
    private int mSelectedViewHolderPosition;
    private Context mContext;

    private static final int HR_INDEX = 2;
    private static final int VIEWTYPE_HR = 0x00;
    private static final int VIEWTYPE_ITEM = 0x10;

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder viewHolder;
        if(viewType == VIEWTYPE_HR){
            viewHolder = new VoidViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hr_drawer, parent, false));

        }else{
            viewHolder = new NavigationDrawerItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_navigation_drawer_item, parent, false));
            viewHolder.itemView.setClickable(true);
            viewHolder.itemView.setOnClickListener(view -> {
                if (mSelectedView != null) mSelectedView.setSelected(false);
                mSelectedViewHolderPosition = viewHolder.getAdapterPosition();
                mSelectedView = view;
                if (mNavigationDrawerCallback != null)
                    mNavigationDrawerCallback.onNavigationDrawerItemSelected(mSelectedViewHolderPosition, true);
            });
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position != HR_INDEX) {
            ((NavigationDrawerItemViewHolder) holder).bind(mData.get(this.getItemsPosition(position)));
            if (mSelectedViewHolderPosition == position) {
                if (mSelectedView != null) mSelectedView.setSelected(false);
                mSelectedView = holder.itemView;
                mSelectedView.setSelected(true);
            }
        }
    }

    public int getItemsPosition(int viewHolderPosition){
        return  viewHolderPosition - (viewHolderPosition > HR_INDEX ? getItemOffset() : 0);
    }

    public int getItemOffset(){
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == HR_INDEX) return VIEWTYPE_HR;
        else return VIEWTYPE_ITEM;
    }

    public void selectPosition(int position) {
        mSelectedViewHolderPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return (mData != null ? mData.size() : 0) + getItemOffset();
    }

    public class NavigationDrawerItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.nav_item_icon) protected ImageView mNavItemIcon;  // 54% #000000
        @InjectView(R.id.nav_item_label) protected TextView mNavItemLabel; // Roboto Medium, 14sp, 87% #000000
        private int mIconColor;
        private final Context mContext;

        public NavigationDrawerItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            mContext = itemView.getContext();
            mIconColor = mContext.getResources().getColor(R.color.icon_material);
        }

        public void bind(NavigationDrawerItem navigationDrawerItem) {
            Picasso.with(mContext).load(navigationDrawerItem.getDrawableResourceId()).transform(new ColorFilterTransformation(mIconColor)).into(mNavItemIcon);
            mNavItemLabel.setText(navigationDrawerItem.getLabel());
        }
    }
}