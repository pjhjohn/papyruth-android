package com.montserrat.parts.nav;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.activity.R;

import java.util.List;

/**
 * Created by mrl on 2015-03-16.
 */
public class NavAdapter extends ArrayAdapter<NavItem> {
    Context context;
    List<NavItem> navItemList;
    int layoutResId;

    public NavAdapter(Context context, int layoutResId, List<NavItem> initItemList) {
        super(context, layoutResId, initItemList);
        this.context = context;
        this.navItemList = initItemList;
        this.layoutResId = layoutResId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        NavItemHolder holder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new NavItemHolder();

            view = inflater.inflate(this.layoutResId, parentView, false);
            holder.text = (TextView) view.findViewById(R.id.nav_item_text);
            holder.icon = (ImageView) view.findViewById(R.id.nav_item_icon);
            view.setTag(holder);
        } else {
            holder = (NavItemHolder) view.getTag();
        }
        NavItem item = (NavItem) this.navItemList.get(position);
        holder.icon.setImageDrawable(view.getResources().getDrawable(item.getItemImgResId()));
        holder.text.setText(item.getItemText());
        return view;
    }

    private static class NavItemHolder {
        TextView text;
        ImageView icon;
    }
}
