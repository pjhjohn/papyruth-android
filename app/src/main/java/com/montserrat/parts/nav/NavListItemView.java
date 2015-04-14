package com.montserrat.parts.nav;

import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.activity.R;
import com.montserrat.utils.adapter.InvokeView;
import com.montserrat.utils.adapter.UniversalItemView;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class NavListItemView extends UniversalItemView<NavListItemView.Holder, NavListItemView.Data> {
    public NavListItemView (Data data) {
        super(data);
        this.viewHolder = new Holder();
    }

    @Override
    public void mappingData (Holder holder, Data entity) {
        holder.title.setText(entity.navTitle);
        holder.icon.setImageDrawable(holder.icon.getResources().getDrawable(entity.navIconResId));
    }

    @Override
    public int getLayoutId() {
        return R.layout.list_item_nav;
    }

    public static class Data {
        String navTitle;
        int navIconResId;

        public Data (){}
        public Data(String title, int resourceId) {
            this.navTitle = title;
            this.navIconResId = resourceId;
        }
    }

    public static class Holder {
        @InvokeView (viewId = R.id.nav_item_icon)
        public ImageView icon;

        @InvokeView(viewId = R.id.nav_item_text)
        public TextView title;
    }
}