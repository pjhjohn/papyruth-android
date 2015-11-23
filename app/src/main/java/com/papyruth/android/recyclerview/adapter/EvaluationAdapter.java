package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.model.unique.AppTracker;
import com.papyruth.android.recyclerview.viewholder.EvaluationViewHolder;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.CommentData;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.recyclerview.viewholder.CommentItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "EvaluationAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private View.OnClickListener mOnClickListener;
    private List<CommentData> mCommentDataList;
    private boolean mUserLearnedInform;
    private boolean mShowPlaceholder;

    public EvaluationAdapter(List<CommentData> initialCommentDataList, RecyclerViewItemClickListener listener, View.OnClickListener onClick) {
        mRecyclerViewItemClickListener = listener;
        mCommentDataList = initialCommentDataList;
        mOnClickListener = onClick;
        mUserLearnedInform = AppManager.getInstance().getBoolean(USER_LEARNED_INFORM, false);
        mShowPlaceholder = false;
    }
    public void setShowPlaceholder(boolean show){
        this.mShowPlaceholder = show;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
            if(!mUserLearnedInform && position == 1) {
                String action = null;
                switch(view.getId()) {
                    case R.id.inform_btn_optional :
                        AppManager.getInstance().putBoolean(USER_LEARNED_INFORM, true);
                        action = parent.getResources().getString(R.string.ga_event_hide_always);this.mUserLearnedInform = true;
                        if(action == null) action = parent.getResources().getString(R.string.ga_event_hide_once);
                        AppTracker.getInstance().getTracker().send(
                            new HitBuilders.EventBuilder(parent.getResources().getString(R.string.ga_category_inform), action).build()
                        );
                    case R.id.inform_btn_positive :
                        this.notifyItemRemoved(position);
                        mUserLearnedInform = true;
                        break;
                    default : Timber.d("Unexpected view #%x", view.getId());
                }
            } else mRecyclerViewItemClickListener.onRecyclerViewItemClick(view, position - getItemOffset());
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_evaluation, R.color.inform_evaluation);
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) ((EvaluationViewHolder) holder).bind(Evaluation.getInstance(), mOnClickListener);
        else if (mCommentDataList.isEmpty() && mShowPlaceholder) ((PlaceholderViewHolder) holder).bind(R.string.no_data_comment);
        else ((CommentItemViewHolder) holder).bind(this.mCommentDataList.get(position - getItemOffset()));
    }

    public int getItemOffset() {
        return 2 + (mUserLearnedInform ? 0 : 1) + (mCommentDataList.isEmpty() && mShowPlaceholder ? 1 : 0);
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (this.mCommentDataList == null ? 0 : this.mCommentDataList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.EVALUATION;
        else if(mCommentDataList.isEmpty() && mShowPlaceholder) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.COMMENT_ITEM;
    }
}