package com.papyruth.android.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppManager;
import com.papyruth.android.model.unique.AppTracker;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.CourseViewHolder;
import com.papyruth.android.recyclerview.viewholder.EvaluationItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.PlaceholderViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "CourseAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private RecyclerViewItemClickListener evaluationItemClickListener;
    private List<EvaluationData> evaluations;
    private boolean mUserLearnedInform;
    private boolean isEmptyData;
    public CourseAdapter(List<EvaluationData> initialEvaluations, RecyclerViewItemClickListener listener) {
        this.evaluations = initialEvaluations;
        this.evaluationItemClickListener = listener;
        mUserLearnedInform = AppManager.getInstance().getBoolean(USER_LEARNED_INFORM, false);
        isEmptyData = false;
    }

    public void setIsEmptyData(boolean isEmptyData){
        this.isEmptyData = isEmptyData;
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
                        this.mUserLearnedInform = true;
                        if(action == null) action = parent.getResources().getString(R.string.ga_event_hide_once);
                        AppTracker.getInstance().getTracker().send(
                            new HitBuilders.EventBuilder(parent.getResources().getString(R.string.ga_category_inform), action).build()
                        );
                        break;
                    default : Timber.d("Unexpected view #%x", view.getId());
                }
            } else evaluationItemClickListener.onRecyclerViewItemClick(view, position - getItemOffset());
        });
    }

    /**
     * @param holder
     * @param position HEADER / INFORM(if not learned) / COURSE / EVALUATIONs
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_course, R.color.inform_course);
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) ((CourseViewHolder) holder).bind(Course.getInstance());
        else if(evaluations.isEmpty() && isEmptyData)
            ((PlaceholderViewHolder) holder).bind(
                User.getInstance().getMandatoryEvaluationCount() > 0 ? R.string.no_data_you_cant : R.string.no_data_evaluation
            );
        else ((EvaluationItemViewHolder) holder).bind(this.evaluations.get(position - getItemOffset()));
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    public int getItemOffset() {
        return 2 + (mUserLearnedInform ? 0 : 1) + (evaluations.isEmpty() && isEmptyData ? 1: 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.COURSE;
        else if(evaluations.isEmpty() && isEmptyData) return ViewHolderFactory.ViewType.PLACEHOLDER;
        else return ViewHolderFactory.ViewType.EVALUATION_ITEM;
    }
}
