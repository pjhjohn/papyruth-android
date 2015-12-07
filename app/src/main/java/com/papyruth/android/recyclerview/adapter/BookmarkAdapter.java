package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.papyruth.android.R;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.FavoriteData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.CourseItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.recyclerview.viewholder.VoidViewHolder;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookmarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IAdapter {
//    private static final String HIDE_INFORM = "BookmarkAdapter.mHideInform"; // Inform is UNIQUE per Adapter.

    private SwipeRefreshLayout mSwipeRefresh;
    private View mEmptyState;
    private List<CourseData> mCourses;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemObjectClickListener;
    private boolean mHideInform;
    private boolean mHideShadow;
    private Integer mPage;
    private int mIndexHeader; // INDEX 0
    private int mIndexInform; // UNDER HEADER unless user learned inform, -1 otherwise
    private int mIndexSingle; // UNDER INFORM if exists, -1 otherwise
    private int mIndexShadow; // UNDER SINGLE if exists, -1 otherwise
    private int mIndexContent;// UNDER SHADOW if exists.
    private int mIndexFooter; // AT LAST
    private FrameLayout mShadow;
    private View mFooterBorder;
    private RelativeLayout mFooterMaterialProgressBar;
    private ImageView mFooterFullyLoadedIndicator;

    public BookmarkAdapter(Context context, SwipeRefreshLayout swiperefresh, View emptystate, RecyclerViewItemObjectClickListener listener) {
        mSwipeRefresh = swiperefresh;
        mEmptyState = emptystate;
        mCourses = new ArrayList<>();
        mRecyclerViewItemObjectClickListener = listener;
        mHideInform = true;
        mHideShadow = true;
        mPage = 1;
        mIndexHeader = 0;
        mIndexInform = mHideInform? -1 : 1;
        mIndexSingle = -1;
        mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
        mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
        mIndexFooter = mCourses.size() + mIndexContent;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position)->{
            if(position == mIndexFooter) { if(mFullyLoaded) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY); }
            else mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mCourses.get(position - mIndexContent));
        });
        if(viewType == ViewHolderFactory.ViewType.SHADOW && viewHolder instanceof VoidViewHolder) mShadow = (FrameLayout) viewHolder.itemView.findViewById(R.id.cardview_shadow);
        if (viewHolder instanceof FooterViewHolder) {
            mFooterBorder = viewHolder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewHolder.itemView.findViewById(R.id.material_progress_medium);
            mFooterFullyLoadedIndicator = (ImageView) viewHolder.itemView.findViewById(R.id.footer_fully_loaded_indicator);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) return;
        if (position == mIndexInform) return;
        if (position == mIndexSingle) return;
        if (position == mIndexShadow) return;
        if (position == mIndexFooter) return;
        ((CourseItemViewHolder) holder).bind(mCourses.get(position - mIndexContent));
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if (position == mIndexInform) return ViewHolderFactory.ViewType.INFORM;
        if (position == mIndexSingle) ;
        if (position == mIndexShadow) return ViewHolderFactory.ViewType.SHADOW;
        if (position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.COURSE_ITEM;
    }
    private void reconfigure(){
        if(mCourses.isEmpty()){
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mCourses.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_IN(mEmptyState).start();
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            if(mShadow != null)
                mShadow.setBackgroundResource(R.drawable.shadow_transparent);
        }else{
            mPage ++;
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mCourses.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_OUT(mEmptyState).start();
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            if(mShadow != null)
                mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
        if(mFullyLoaded != null && mFullyLoaded) AnimatorHelper.FADE_IN(mFooterFullyLoadedIndicator).start();
        else AnimatorHelper.FADE_OUT(mFooterFullyLoadedIndicator).start();
    }

    private void addFavoriteData(List<FavoriteData> favorites, boolean clear){
        if(clear) mCourses.clear();
        for (FavoriteData favorite : favorites) {
            mCourses.add(favorite.course);
        }
    }

    @Override
    public void refresh() {
        mSwipeRefresh.setRefreshing(true);
        Api.papyruth().users_me_favorites(User.getInstance().getAccessToken(), mPage = 1)
            .map(response -> response.favorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(favorites -> {
                mSwipeRefresh.setRefreshing(false);
                addFavoriteData(favorites, true);
                mLoading = false;
                mFullyLoaded = false;
                reconfigure();
            }, error -> {
                mSwipeRefresh.setRefreshing(false);
                ErrorHandler.handle(error, this);
                error.printStackTrace();
            });
    }

    private Boolean mLoading;
    private Boolean mFullyLoaded;
    @Override
    public void loadMore() {
        if(mLoading != null && mLoading) return;
        mLoading = true;
        if(mFullyLoaded != null && mFullyLoaded) return;
        mFullyLoaded = false;
        if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_IN(mFooterMaterialProgressBar).start();
        Api.papyruth()
            .users_me_favorites(
                User.getInstance().getAccessToken(),
                mPage == null ? null : mPage
            )
            .map(response -> response.favorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(favorites -> {
                if (favorites != null) {
                    if(favorites.isEmpty()) mFullyLoaded = true;
                    else addFavoriteData(favorites, false);
                }
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
                reconfigure();
            }, error -> {
                ErrorHandler.handle(error, this);
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
            });
    }
}