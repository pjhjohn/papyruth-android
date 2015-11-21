package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.AppManager;
import com.papyruth.android.recyclerview.viewholder.EvaluationItemDetailViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

public class EvaluationItemsDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "EvaluationItemsDetailAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private List<EvaluationData> mEvaluatinDataList;
    private boolean mUserLearnedInform;
    private boolean mShowPlaceholder;

    public EvaluationItemsDetailAdapter(List<EvaluationData> initialEvaluationDataList, RecyclerViewItemClickListener listener) {
        mEvaluatinDataList = initialEvaluationDataList;
        mRecyclerViewItemClickListener = listener;
        mUserLearnedInform = AppManager.getInstance().getBoolean(USER_LEARNED_INFORM, false);
        mShowPlaceholder = false;
    }

    public void setShowPlaceholder(boolean show){
        mShowPlaceholder = show;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
            if(!mUserLearnedInform && position == 1) {
                switch(view.getId()) {
                    case R.id.inform_btn_optional :
                        AppManager.getInstance().putBoolean(USER_LEARNED_INFORM, true);
                    case R.id.inform_btn_positive :
                        this.notifyItemRemoved(position);
                        mUserLearnedInform = true;
                        break;
                    default : Timber.d("Unexpected view #%x", view.getId());
                }
            } else mRecyclerViewItemClickListener.onRecyclerViewItemClick(view, position - getItemOffset());
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
        else if (mEvaluatinDataList.isEmpty() && mShowPlaceholder) ((PlaceholderViewHolder) holder).bind(R.string.no_data_you_cant);
        else ((EvaluationItemDetailViewHolder) holder).bind(mEvaluatinDataList.get(position - 1 - (mUserLearnedInform ? 0 : 1)));
    }

    public int getItemOffset() {
        return 1 + (mUserLearnedInform ? 0 : 1) + (mEvaluatinDataList.isEmpty() && mShowPlaceholder ? 1 : 0);
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (mEvaluatinDataList == null ? 0 : mEvaluatinDataList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if(mEvaluatinDataList.isEmpty() && mShowPlaceholder) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.EVALUATION_ITEM_DETAIL;
    }
}