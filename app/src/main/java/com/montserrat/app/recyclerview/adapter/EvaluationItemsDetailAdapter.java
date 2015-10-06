package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.recyclerview.viewholder.CommentItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.EvaluationItemDetailViewHolder;
import com.montserrat.app.recyclerview.viewholder.EvaluationViewHolder;
import com.montserrat.app.recyclerview.viewholder.InformViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.main.HomeFragment HomeFragment}
 * as an adapter for List-type {@link RecyclerView} to provide latest evaluations to user
 */
public class EvaluationItemsDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "EvaluationItemsDetailAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private boolean mUserLearnedInform;
    private RecyclerViewItemClickListener itemClickListener;
    private List<EvaluationData> evaluations;

    public EvaluationItemsDetailAdapter(List<EvaluationData> initialEvaluations, RecyclerViewItemClickListener listener) {
        this.evaluations = initialEvaluations;
        this.itemClickListener = listener;
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
            } else this.itemClickListener.onRecyclerViewItemClick(view, position - getItemOffset());
        });
    }

    /**
     * @param holder
     * @param position HEADER / INFORM(if not learned) / EVALUATION_ITEM_DETAILs
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_home);
        else ((EvaluationItemDetailViewHolder) holder).bind(this.evaluations.get(position - 1 - (mUserLearnedInform ? 0 : 1)));
    }

    public int getItemOffset() {
        return 1 + (mUserLearnedInform ? 0 : 1);
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else return ViewHolderFactory.ViewType.EVALUATION_ITEM_DETAIL;
    }
}