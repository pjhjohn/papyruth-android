package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.papyruth.android.model.MyCommentData;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.recyclerview.viewholder.MyCommentViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

public class MyCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "MyCommentAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private List<MyCommentData> mMyCommentDataList;
    private boolean mUserLearnedInform;
    private boolean mShowPlaceholder;

    public MyCommentAdapter(List<MyCommentData> initialMyCommentDataList, RecyclerViewItemClickListener listener) {
        mRecyclerViewItemClickListener = listener;
        mMyCommentDataList = initialMyCommentDataList;
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
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_home, R.color.inform_my_comment);
        else if (mMyCommentDataList.isEmpty() && mShowPlaceholder) ((PlaceholderViewHolder) holder).bind(R.string.no_data_my_comment);
        else ((MyCommentViewHolder) holder).bind(mMyCommentDataList.get(position - (mUserLearnedInform ? 1 : 2)));
    }

    public int getItemOffset() {
        return 1 + (mUserLearnedInform ? 0 : 1) + (mMyCommentDataList.isEmpty() && mShowPlaceholder ? 1 : 0);
    }

    @Override
    public int getItemCount() {
        if (mMyCommentDataList == null) return getItemOffset();
        return mMyCommentDataList.size() + getItemOffset();
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if (mMyCommentDataList.isEmpty() && mShowPlaceholder) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.MY_COMMENT;
    }
}