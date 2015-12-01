package com.papyruth.android.recyclerview.adapter;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppManager;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.unique.AppTracker;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.EvaluationItemDetailViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.R;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EvaluationItemsDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "EvaluationItemsDetailAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private SwipeRefreshLayout mSwipeRefresh;
    private View mEmptyState, mProgress;
    private List<EvaluationData> mEvaluations;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemClickListener;
    private boolean mUserLearnedInform;

    public EvaluationItemsDetailAdapter(SwipeRefreshLayout swiperefresh, View emptystate, View progress, RecyclerViewItemObjectClickListener listener) {
        mSwipeRefresh = swiperefresh;
        mEmptyState = emptystate;
        mProgress = progress;
        mEvaluations = new ArrayList<>();
        mRecyclerViewItemClickListener = listener;
        mUserLearnedInform = AppManager.getInstance().getBoolean(USER_LEARNED_INFORM, false);
        mSinceId = null;
    }

    public RecyclerView.ViewHolder getHeader() {
        return null;
    }

    public RecyclerView.ViewHolder getInform() {
        return null;
    }

    public RecyclerView.ViewHolder getFooter() {
        return null;
    }

    private int mShadowIndex;
    public int getShadowIndex() {
        return mShadowIndex;
    }
    public void setShadowIndex(int index) {
        mShadowIndex = index;
    }

    private Integer mSinceId;
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
                    mSinceId = mEvaluations.isEmpty()? null : mEvaluations.get(mEvaluations.size()-1).id;
                    this.notifyDataSetChanged();
                }, error -> {
                    mSwipeRefresh.setRefreshing(false);
                    ErrorHandler.handle(error, this);
                }
            );
    }
    public void loadMore() {
        AnimatorHelper.FADE_IN(mProgress).start();
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
                AnimatorHelper.FADE_OUT(mProgress).start();
                if (evaluations != null) {
                    mEvaluations.addAll(evaluations);
                    mSinceId = mEvaluations.isEmpty()? null : mEvaluations.get(mEvaluations.size()-1).id;
                } this.notifyDataSetChanged();
            }, error -> {
                AnimatorHelper.FADE_OUT(mProgress).start();
                ErrorHandler.handle(error, this);
            });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
            if(!mUserLearnedInform && position == 1) {
                String action = null;
                switch(view.getId()) {
                    case R.id.inform_btn_optional :
                        AppManager.getInstance().putBoolean(USER_LEARNED_INFORM, true);
                        action = parent.getResources().getString(R.string.ga_event_hide_always);
                    case R.id.inform_btn_positive :
                        this.notifyItemRemoved(position);
                        mUserLearnedInform = true;
                        if(action == null) action = parent.getResources().getString(R.string.ga_event_hide_once);
                        AppTracker.getInstance().getTracker().send(
                            new HitBuilders.EventBuilder(parent.getResources().getString(R.string.ga_category_inform), action).build()
                        );
                        break;
                    default : Timber.d("Unexpected view #%x", view.getId());
                }
            } else mRecyclerViewItemClickListener.onRecyclerViewItemClick(view, mEvaluations.get(position - getItemOffset()));
        });
    }

    /**
     * @param holder
     * @param position HEADER / INFORM(if not learned) / EVALUATION_ITEM_DETAILs
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_home, R.color.inform_evaluation_item_detail);
        else if (mEvaluations.isEmpty()) ((PlaceholderViewHolder) holder).bind(R.string.no_data_you_cant);
        else ((EvaluationItemDetailViewHolder) holder).bind(mEvaluations.get(position - 1 - (mUserLearnedInform ? 0 : 1)));
    }

    public int getItemOffset() {
        return 1 + (mUserLearnedInform ? 0 : 1) + (mEvaluations.isEmpty() ? 1 : 0);
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (mEvaluations == null ? 0 : mEvaluations.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if(mEvaluations.isEmpty()) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.EVALUATION_ITEM_DETAIL;
    }
}