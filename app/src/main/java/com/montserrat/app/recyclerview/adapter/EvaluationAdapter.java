package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.recyclerview.viewholder.CommentItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.EvaluationViewHolder;
import com.montserrat.app.recyclerview.viewholder.InformViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

/**
 * Created by SSS on 2015-05-08.
 */
public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "EvaluationAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private boolean mUserLearnedInform;
    private RecyclerViewItemClickListener commentItemClickListener;
    private List<CommentData> comments;
    public EvaluationAdapter(List<CommentData> initialComments, RecyclerViewItemClickListener listener) {
        this.comments = initialComments;
        this.commentItemClickListener = listener;
        mUserLearnedInform = AppManager.getInstance().getBoolean(USER_LEARNED_INFORM, false);
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
            } else commentItemClickListener.onRecyclerViewItemClick(view, position - getItemOffset());
        });
    }

    /**
     * @param holder
     * @param position HEADER / INFORM(if not learned) / EVALUATION / COMMENTs
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_evaluation);
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) ((EvaluationViewHolder) holder).bind(Evaluation.getInstance());
        else ((CommentItemViewHolder) holder).bind(this.comments.get(position - getItemOffset()));
    }

    public int getItemOffset() {
        return 2 + (mUserLearnedInform ? 0 : 1);
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (this.comments == null ? 0 : this.comments.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.EVALUATION;
        else return ViewHolderFactory.ViewType.COMMENT_ITEM;
    }
}