package com.papyruth.android.recyclerview.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.papyruth.android.R;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

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

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    public void setContext(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
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
        public static final int MY_EVALUATION          = 0xa0;
        public static final int MY_COMMENT_ITEM        = 0xb0;
        public static final int PLACEHOLDER            = 0xc0;
        public static final int OPEN_SOURCE_LICENSE    = 0xd0;
    }

    public RecyclerView.ViewHolder create(ViewGroup parent, int viewType) {
        return this.create(parent, viewType, null, null);
    }
    public RecyclerView.ViewHolder create(ViewGroup parent, int viewType, RecyclerViewItemClickListener listener) {
        return this.create(parent, viewType, listener, null);
    }
    public RecyclerView.ViewHolder create(ViewGroup parent, int viewType, RecyclerViewItemClickListener listener, Integer layoutResId) {
        if(mContext == null) throw new Resources.NotFoundException("Context Not Found. Must set at some point");
        if(mInflater == null) throw new Resources.NotFoundException("LayoutInflater Not Found. Must set at some point");
        switch(viewType) {
            case ViewType.HEADER                 : return new               HeaderViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_white_actionbarsize   , parent, false));
            case ViewType.INFORM                 : return new               InformViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_inform                , parent, false), listener);
            case ViewType.COURSE                 : return new               CourseViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_course                , parent, false));
            case ViewType.EVALUATION             : return new           EvaluationViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_evaluation            , parent, false));
            case ViewType.COURSE_ITEM            : return new           CourseItemViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_course_item           , parent, false), listener);
            case ViewType.EVALUATION_ITEM        : return new       EvaluationItemViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_evaluation_item       , parent, false), listener);
            case ViewType.EVALUATION_ITEM_DETAIL : return new EvaluationItemDetailViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_evaluation_item_detail, parent, false), listener);
            case ViewType.COMMENT_ITEM           : return new          CommentItemViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_comment_item          , parent, false), listener);
            case ViewType.AUTO_COMPLETE_RESPONSE : return new AutoCompleteResponseViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_autocomplete_response , parent, false), listener);
            case ViewType.UNIVERSITY             : return new           UniversityViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_university_item       , parent, false), listener);
            case ViewType.MY_EVALUATION          : return new         MyEvaluationViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_my_evaluation         , parent, false), listener);
            case ViewType.MY_COMMENT_ITEM        : return new        MyCommentItemViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_my_comment            , parent, false), listener);
            case ViewType.PLACEHOLDER            : return new          PlaceholderViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_placeholder           , parent, false));
            case ViewType.OPEN_SOURCE_LICENSE    : return new    OpenSourceLicenseViewHolder(mInflater.inflate(layoutResId != null ? layoutResId : R.layout.cardview_open_source_license   , parent, false), listener);
            default : throw new RuntimeException(String.format("Couldn't find any ViewHolder with ViewType#%d. Check whether you put correct ViewType.", viewType));
        }
    }
}