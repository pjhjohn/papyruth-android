package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.recyclerview.viewholder.MyEvaluationViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

public class MyEvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "MyEvaluationAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private List<EvaluationData> mEvaluationDataList;
    private boolean mUserLearnedInform;
    private boolean mShowPlaceholder;

    public MyEvaluationAdapter(List<EvaluationData> initialEvaluationDataList, RecyclerViewItemClickListener listener) {
        mRecyclerViewItemClickListener = listener;
        mEvaluationDataList = initialEvaluationDataList;
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_home, R.color.inform_my_evaluation);
        else if (mEvaluationDataList.isEmpty() && mShowPlaceholder) ((PlaceholderViewHolder) holder).bind(R.string.no_data_my_evaluation);
        else ((MyEvaluationViewHolder) holder).bind(mEvaluationDataList.get(position - getItemOffset()));
    }

    public int getItemOffset() {
        return 1 + (mUserLearnedInform ? 0 : 1) + (mEvaluationDataList.isEmpty() && mShowPlaceholder ? 1 : 0);
    }

    @Override
    public int getItemCount() {
        if (mEvaluationDataList == null) return getItemOffset();
        return mEvaluationDataList.size() + getItemOffset();
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if (mEvaluationDataList.isEmpty() && mShowPlaceholder) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.MY_EVALUATION;
    }
}