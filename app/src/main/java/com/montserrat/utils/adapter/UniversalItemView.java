package com.montserrat.utils.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * Created by pjhjohn on 2015-04-09.
 */
public abstract class UniversalItemView<T,E> implements Adaptable{
    private static final String TAG = "UniversalItemView";
    protected T viewHolder;
    protected E entity;

    public UniversalItemView (E entity) {
        this.entity = entity;
    }

    protected void invokeView(View v) {
        try {
            for(Field field : viewHolder.getClass().getFields()) {
                InvokeView annotation = field.getAnnotation(InvokeView.class);

                int id = annotation.viewId();
//                Log.d("DEBUG", String.format("View #%d : Field %s", id, field.getName()));
                field.set(viewHolder, v.findViewById(id));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public View buildView(View view, LayoutInflater inflater, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(this.getLayoutId(), parent, false);
            invokeView(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (T) view.getTag();
        }

        mappingData(viewHolder, entity);
        return view;
    }

    protected abstract void mappingData(T viewHolder, E entity);
    protected abstract int getLayoutId();
}
