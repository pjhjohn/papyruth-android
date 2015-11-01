package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.MyCommentData;
import com.montserrat.app.recyclerview.viewholder.InformViewHolder;
import com.montserrat.app.recyclerview.viewholder.MyCommentViewHolder;
import com.montserrat.app.recyclerview.viewholder.NoDataViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.main.HomeFragment HomeFragment}
 * as an adapter for List-type {@link android.support.v7.widget.RecyclerView} to provide latest evaluations to user
 */
public class MyCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "MyWrittenCommentAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private boolean mUserLearnedInform;
    private RecyclerViewItemClickListener itemClickListener;
    private List<MyCommentData> myWritten;

    public MyCommentAdapter(List<MyCommentData> initialEvaluations, RecyclerViewItemClickListener listener) {
        this.myWritten = initialEvaluations;
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
        else{
            if(myWritten.isEmpty())
                ((NoDataViewHolder) holder).bind(R.string.no_data);
            else
                ((MyCommentViewHolder) holder).bind(this.myWritten.get(position - (mUserLearnedInform ? 1 : 2)));
        }
    }

    public int getItemOffset() {
        return 1 + (mUserLearnedInform ? 0 : 1) +(myWritten.isEmpty()? 1 : 0);
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (this.myWritten == null ? 0 : this.myWritten.size());

    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else {
            if (myWritten.isEmpty())
                return ViewHolderFactory.ViewType.NO_DATA;
            else
                return ViewHolderFactory.ViewType.MY_WRITTEN_COMMENT;
        }
    }
}