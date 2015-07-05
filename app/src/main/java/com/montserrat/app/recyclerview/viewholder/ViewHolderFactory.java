package com.montserrat.app.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

/**
 * Created by pjhjohn on 2015-06-29.
 */
public class ViewHolderFactory {
    private static ViewHolderFactory instance;
    private ViewHolderFactory() {}
    public static synchronized ViewHolderFactory getInstance() {
        if (instance == null) instance = new ViewHolderFactory();
        return instance;
    }

    private Context context = null;
    private LayoutInflater inflater = null;
    public void setContext(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public static class ViewType {
        /* unique : maximum one per recyclerview */
        public static final int HEADER                 = 0x00;
        public static final int INFORM                 = 0x10;
        public static final int COURSE                 = 0x20;
        public static final int EVALUATION             = 0x30;

        /* item : as list item. zero to multiple per recyclerview */
        public static final int COURSE_ITEM            = 0x40;
        public static final int EVALUATION_ITEM        = 0x50;
        public static final int EVALUATION_ITEM_DETAIL = 0x60;
        public static final int COMMENT_ITEM           = 0x70;

        /* item : etc */
        public static final int AUTO_COMPLETE_RESPONSE = 0x80;
        public static final int UNIVERSITY             = 0x90;
    }
    public RecyclerView.ViewHolder create(ViewGroup parent, int viewType) {
        return this.create(parent, viewType, null, null);
    }
    public RecyclerView.ViewHolder create(ViewGroup parent, int viewType, RecyclerViewItemClickListener listener) {
        return this.create(parent, viewType, listener, null);
    }
    public RecyclerView.ViewHolder create(ViewGroup parent, int viewType, RecyclerViewItemClickListener listener, Integer layoutResId) {
        if(context == null) throw new Resources.NotFoundException("Context Not Found. Must set at some point");
        if(inflater == null) throw new Resources.NotFoundException("LayoutInflater Not Found. Must set at some point");
        switch(viewType) {
            case ViewType.HEADER                 : return new               HeaderViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_header                , parent, false));
            case ViewType.INFORM                 : return new               InformViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_inform                , parent, false));
            case ViewType.COURSE                 : return new               CourseViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_course                , parent, false));
            case ViewType.EVALUATION             : return new           EvaluationViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_evaluation            , parent, false));
            case ViewType.COURSE_ITEM            : return new           CourseItemViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_course_item           , parent, false), listener);
            case ViewType.EVALUATION_ITEM        : return new       EvaluationItemViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_evaluation_item       , parent, false), listener);
            case ViewType.EVALUATION_ITEM_DETAIL : return new EvaluationItemDetailViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_evaluation_item_detail, parent, false), listener);
            case ViewType.COMMENT_ITEM           : return new          CommentItemViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_comment_item          , parent, false), listener);
            case ViewType.AUTO_COMPLETE_RESPONSE : return new AutoCompleteResponseViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_autocomplete_response , parent, false), listener);
            case ViewType.UNIVERSITY             : return new           UniversityViewHolder(inflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_university            , parent, false), listener);
            default : throw new RuntimeException(String.format("There doesn't exist a ViewHolder which has viewType#%d. Make sure to put correct viewType.", viewType));
        }
    }
}