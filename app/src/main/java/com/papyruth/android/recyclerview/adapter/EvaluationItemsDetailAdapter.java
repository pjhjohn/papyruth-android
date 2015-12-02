package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.unique.AppTracker;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.EvaluationItemDetailViewHolder;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
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
import timber.log.Timber;

public class EvaluationItemsDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IAdapter {
    private static final String USER_LEARNED_INFORM = "EvaluationItemsDetailAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private final Context mContext;
    private SwipeRefreshLayout mSwipeRefresh;
    private View mEmptyState;
    private List<EvaluationData> mEvaluations;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemClickListener;
    private boolean mHideInform;
    private boolean mHideShadow;
    private Integer mSinceId;
    private int mIndexHeader; // INDEX 0
    private int mIndexInform; // UNDER HEADER unless user learned inform, -1 otherwise
    private int mIndexSingle; // UNDER INFORM if exists, -1 otherwise
    private int mIndexShadow; // UNDER SINGLE if exists, -1 otherwise
    private int mIndexContent;// UNDER SHADOW if exists.
    private int mIndexFooter; // AT LAST
    private RelativeLayout mMaterialProgressBar;
    private FrameLayout mShadow;

    public EvaluationItemsDetailAdapter(Context context, SwipeRefreshLayout swiperefresh, View emptystate, RecyclerViewItemObjectClickListener listener) {
        mContext = context;
        mSwipeRefresh = swiperefresh;
        mEmptyState = emptystate;
        mEvaluations = new ArrayList<>();
        mRecyclerViewItemClickListener = listener;
        mHideInform = AppManager.getInstance().getBoolean(USER_LEARNED_INFORM, false);
        mSinceId = null;
        mIndexHeader = 0;
        mIndexInform = mHideInform? -1 : 1;
        mIndexSingle = -1;
        mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
        mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
        mIndexFooter = mEvaluations.size() + mIndexContent;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewholder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
            if(!mHideInform && position == 1) {
                String action = null;
                switch(view.getId()) {
                    case R.id.inform_btn_optional :
                        AppManager.getInstance().putBoolean(USER_LEARNED_INFORM, true);
                        action = parent.getResources().getString(R.string.ga_event_hide_always);
                    case R.id.inform_btn_positive :
                        notifyItemRemoved(position);
                        mHideInform = true;
                        mHideShadow = true;
                        if(action == null) action = parent.getResources().getString(R.string.ga_event_hide_once);
                        AppTracker.getInstance().getTracker().send(
                            new HitBuilders.EventBuilder(parent.getResources().getString(R.string.ga_category_inform), action).build()
                        );
                        reconfigure();
                        break;
                    default : Timber.d("Unexpected view #%x", view.getId());
                }
            } else mRecyclerViewItemClickListener.onRecyclerViewItemClick(view, mEvaluations.get(position - mIndexContent));
        });
        if (viewType == ViewHolderFactory.ViewType.SHADOW && viewholder instanceof VoidViewHolder) mShadow = (FrameLayout) viewholder.itemView.findViewById(R.id.cardview_shadow);
        if (viewholder instanceof FooterViewHolder) mMaterialProgressBar = (RelativeLayout) viewholder.itemView.findViewById(R.id.material_progress_medium);
        return viewholder;
    }

    /**
     * @param holder
     * @param position HEADER / INFORM(if not learned) / EVALUATION_ITEM_DETAILs
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) return;
        if (position == mIndexInform) {((InformViewHolder) holder).bind(R.string.inform_home, R.color.inform_evaluation_item_detail); return; }
//        else if (position == mIndexSingle) ((WhateverComesAfterInformSolelyViewHolder) holder).bind(correspondingData);
        if (position == mIndexShadow) return;
        if (position == mIndexFooter) return;
        ((EvaluationItemDetailViewHolder) holder).bind(mEvaluations.get(position - mIndexContent));
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if (position == mIndexInform) return ViewHolderFactory.ViewType.INFORM;
//        if (position == mIndexSingle) return ViewholderFactory.ViewType.WHATEVER_COMES_AFTER_INFORM_SOLELY
        if (position == mIndexShadow) return ViewHolderFactory.ViewType.SHADOW;
        if (position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.EVALUATION_ITEM_DETAIL;
    }

    private void reconfigure() {
        if(mEvaluations.isEmpty()) {
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mEvaluations.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_IN(mEmptyState).start();
            mShadow.setBackgroundResource(R.drawable.shadow_transparent);
        } else {
            mSinceId = mEvaluations.get(mEvaluations.size()-1).id;
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mEvaluations.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_OUT(mEmptyState).start();
            mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
    }

    /* IAdapter methods*/
    @Override
    public void refresh() {
        mSwipeRefresh.setRefreshing(true);
        Api.papyruth().get_evaluations(User.getInstance().getAccessToken(), User.getInstance().getUniversityId(), null, null, null, null)
            .map(response -> response.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                evaluations -> {
                    mSwipeRefresh.setRefreshing(false);
                    mEvaluations.clear();
                    mEvaluations.addAll(evaluations);
                    reconfigure();
                }, error -> {
                    mSwipeRefresh.setRefreshing(false);
                    ErrorHandler.handle(error, this);
                }
            );
    }

    private Boolean mLoading;
    @Override
    public void loadMore() {
        if(mLoading != null && mLoading) return;
        mLoading = true;
        if(mMaterialProgressBar != null) AnimatorHelper.FADE_IN(mMaterialProgressBar).start();
        Api.papyruth()
            .get_evaluations(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                null,
                mSinceId == null ? null : mSinceId - 1,
                null,
                null
            )
            .map(evaluations -> evaluations.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                mLoading = false;
                if(mMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mMaterialProgressBar).start();
                if (evaluations != null) mEvaluations.addAll(evaluations);
                reconfigure();
            }, error -> {
                mLoading = false;
                if(mMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mMaterialProgressBar).start();
                ErrorHandler.handle(error, this);
            });
    }
}