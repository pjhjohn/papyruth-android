package com.montserrat.parts.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.montserrat.activity.R;

import java.util.List;

/**
 * Created by mrl on 2015-03-16.
 */
public class MainAdapter extends ArrayAdapter<MainItem> {
    Context context;
    List<MainItem> mainItemList;
    int layoutResId;

    public MainAdapter(Context context, int layoutResId, List<MainItem> initItemList) {
        super(context, layoutResId, initItemList);
        this.context = context;
        this.mainItemList = initItemList;
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
            holder.subject = (TextView) view.findViewById(R.id.main_item_subject);
            holder.professor = (TextView) view.findViewById(R.id.main_item_professor);
            holder.rating = (RatingBar) view.findViewById(R.id.main_item_rating);
            view.setTag(holder);
        } else {
            holder = (NavItemHolder) view.getTag();
        }
        MainItem item = (MainItem) this.mainItemList.get(position);
        holder.subject.setText(item.getSubject());
        holder.professor.setText(item.getProfessor());
        holder.rating.setRating(item.getRate());
        return view;
    }

    private static class NavItemHolder {
        TextView subject;
        TextView professor;
        RatingBar rating;
    }
}
