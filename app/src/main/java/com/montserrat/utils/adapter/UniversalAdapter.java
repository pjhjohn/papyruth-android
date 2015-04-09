package com.montserrat.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public class UniversalAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Adaptable> items;

    @SuppressWarnings("unchecked")
    public UniversalAdapter (List items, Context ctx) {
        this.items = (List<Adaptable>) items;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return items.get(position).buildView(convertView, inflater, parent);
    }
}
