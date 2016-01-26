package com.papyruth.support.utility.search;

import android.animation.AnimatorSet;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.model.CandidateData;
import com.papyruth.android.model.HistoryData;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.AutoCompleteResponseViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.materialprogressbar.MaterialProgressBar;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AutoCompleteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private boolean isHistory;
    private Context mContext;
    private ImageView mBackIcon;
    private MaterialProgressBar mMaterialProgressBar;
    private List<CandidateData> mCandidates;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemObjectClickListener;
    private boolean mHideShadow;
    private Integer mPage;
    private int mIndexHeader; // INDEX 0
    private int mIndexShadow; // UNDER SINGLE if exists, -1 otherwise
    private int mIndexContent;// UNDER SHADOW if exists.

    private String mQuery;

    public AutoCompleteAdapter(Context context, ImageView backIcon, MaterialProgressBar materialProgressBar, RecyclerViewItemObjectClickListener listener) {
        mContext = context;
        mCandidates = new ArrayList<>();
        mBackIcon = backIcon;
        mMaterialProgressBar = materialProgressBar;
        mRecyclerViewItemObjectClickListener = listener;
        mHideShadow = true;
        mIndexHeader = 0;
        mIndexContent= 1;
        mIndexShadow = (mHideShadow ? -1 : mCandidates.size() + mIndexContent);
        isHistory = true;

        mPage = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;

        if(viewType == ViewHolderFactory.ViewType.HR_WHITE || viewType == ViewHolderFactory.ViewType.TOOLBAR_SHADOW) {
            viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, null);
        } else {
            viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mCandidates.get(position - mIndexContent)) );
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position <= mIndexHeader) return;
        if(position == mIndexShadow) return;
        if(!mCandidates.isEmpty()) ((AutoCompleteResponseViewHolder) holder).bind(mCandidates.get(position - mIndexContent), isHistory);
    }

    @Override
    public int getItemViewType(int position) {
        if(position <= mIndexHeader) return ViewHolderFactory.ViewType.HR_WHITE;
        if(position == mIndexShadow) return ViewHolderFactory.ViewType.TOOLBAR_SHADOW;
        return ViewHolderFactory.ViewType.AUTO_COMPLETE_RESPONSE;
    }

    @Override
    public int getItemCount() {
        return mIndexShadow + 1;
    }

    public void clear(){
        this.mCandidates.clear();
    }

    public boolean isHistory(){ return isHistory; }

    public void history(){
        List<CandidateData> candidates = new ArrayList<>();
        this.isHistory = true;
        if(AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
            candidates = ((HistoryData)AppManager.getInstance().getStringParsed( AppConst.Preference.HISTORY, HistoryData.class )).candidates;
        } // TODO : Otherwise, Inform history is empty whenever history is empty.
        this.mCandidates.clear();
        this.mCandidates.addAll(candidates);
        reconfigure();
    }

    private Boolean mLoading;
    private Boolean mFullyLoaded;
    AnimatorSet animators;

    public void search(String query) {
        this.isHistory = false;
        if(query == null) return;
        mFullyLoaded = false;
        mQuery = query;
        animators = new AnimatorSet();
        animators.playTogether(AnimatorHelper.FADE_IN(mMaterialProgressBar), AnimatorHelper.FADE_OUT(mBackIcon));
        animators.start();
        mPage = 1;
        getCandidates();
    }

    public void loadMore(){
        if(isHistory) return;
        if(mLoading != null && mLoading) return;
        mLoading = true;
        if(mFullyLoaded != null && mFullyLoaded) return;
        mFullyLoaded = false;
        if(animators !=null && animators.isRunning()) animators.cancel();
        animators = new AnimatorSet();
        animators.playTogether(AnimatorHelper.FADE_IN(mMaterialProgressBar), AnimatorHelper.FADE_OUT(mBackIcon));
        animators.start();
        getCandidates();
    }

    public void getCandidates() {
        if(isHistory) return;
        Api.papyruth()
            .get_search_autocomplete(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                mQuery,
                mPage
            )
            .map(response -> response.candidates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(candidates -> {
                if(candidates != null) {
                    if(mPage != null && mPage <= 1) mCandidates.clear();
                    mCandidates.addAll(candidates);
                    mFullyLoaded = candidates.isEmpty();
                }
                mLoading = false;
                reconfigure();
            }, error -> {
                animators = new AnimatorSet();
                animators.playTogether(AnimatorHelper.FADE_IN(mBackIcon), AnimatorHelper.FADE_OUT(mMaterialProgressBar));
                animators.start();
                ErrorHandler.handle(error, mContext, true);
                mLoading = false;
            });
    }

    private void reconfigure() {
        Timber.d("Reconfigured with mCandidates having size of %d", mCandidates.size());
        if(mCandidates.isEmpty()) {
            mIndexHeader = 0;
            mIndexContent = 1;
            mHideShadow = true;
            mIndexShadow = (mHideShadow ? -1 : mCandidates.size() + mIndexContent);
            notifyDataSetChanged();
        } else {
            mPage++;
            mIndexHeader = 0;
            mIndexContent = 1;
            mHideShadow = false;
            mIndexShadow = (mHideShadow ? -1 : mCandidates.size() + mIndexContent);
            notifyDataSetChanged();
        }

        if (animators != null && animators.isRunning()) animators.cancel();
        animators = new AnimatorSet();
        animators.playTogether(AnimatorHelper.FADE_IN(mBackIcon), AnimatorHelper.FADE_OUT(mMaterialProgressBar));
        animators.start();
    }
}
