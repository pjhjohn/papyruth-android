package com.montserrat.parts.subject;

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
public class SubjectAdapter extends ArrayAdapter<SubjectItem> {
    Context context;
    List<SubjectItem> subjectItemList;
    int layoutResId;

    public SubjectAdapter(Context context, int layoutResId, List<SubjectItem> initItemList) {
        super(context, layoutResId, initItemList);
        this.context = context;
        this.subjectItemList = initItemList;
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
        SubjectItem item = (SubjectItem) this.subjectItemList.get(position);
        holder.icon.setImageDrawable(view.getResources().getDrawable(item.getItemImgResId()));
        holder.text.setText(item.getItemText());
        return view;
    }

    private static class NavItemHolder {
        TextView text;
        ImageView icon;
    }
}
