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
    private static final String TAG = "BaseView";
    protected int layoutId;
    protected T viewHolder;
    protected E entity;
    public UniversalItemView (){}

    public UniversalItemView (E entity, int layoutId) {
        this.entity = entity;
        this.layoutId = layoutId;
    }

    protected void invokeView(View v) {
        try {
            Field fs[] = viewHolder.getClass().getFields();
            for(Field f : fs) {
                InvokeView a = f.getAnnotation(InvokeView.class);

                int id = a.viewId();
                Log.d("DEBUG", "field name : " + f.getName());
                Log.d("DEBUG", "view id : " + id);
                Log.d("DEBUG", "class : " + f.getClass());
                f.set(viewHolder, v.findViewById(id));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public View buildView(View v, LayoutInflater inflater, ViewGroup parent) {
        if (v == null) {
            v = inflater.inflate(layoutId, parent, false);
            invokeView(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (T) v.getTag();
        }

        mappingData(viewHolder, entity);
        return v;
    }

    protected abstract void mappingData(T viewHolder, E entity);
}
