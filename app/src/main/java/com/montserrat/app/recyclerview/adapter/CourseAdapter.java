package com.montserrat.app.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.recyclerview.viewholder.CourseViewHolder;
import com.montserrat.app.recyclerview.viewholder.EvaluationItemViewHolder;
import com.montserrat.app.recyclerview.viewholder.InformViewHolder;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;

import timber.log.Timber;

/**
 * Created by SSS on 2015-04-25.
 */
public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String USER_LEARNED_INFORM = "CourseAdapter.mUserLearnedInform"; // Inform is UNIQUE per Adapter.
    private RecyclerViewItemClickListener evaluationItemClickListener;
    private List<EvaluationData> evaluations;
    private boolean mUserLearnedInform;
    public CourseAdapter(List<EvaluationData> initialEvaluations, RecyclerViewItemClickListener listener) {
        this.evaluations = initialEvaluations;
        this.evaluationItemClickListener = listener;
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
                        this.mUserLearnedInform = true;
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
        if (position == (mUserLearnedInform ? 0 : 1)) ((InformViewHolder) holder).bind(R.string.inform_course);
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) ((CourseViewHolder) holder).bind(Course.getInstance());
        else ((EvaluationItemViewHolder) holder).bind(this.evaluations.get(position - getItemOffset()));
    }

    @Override
    public int getItemCount() {
        return getItemOffset() + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    public int getItemOffset() {
        return 2 + (mUserLearnedInform ? 0 : 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return ViewHolderFactory.ViewType.HEADER;
        if (position == (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.INFORM;
        else if (position == 1 + (mUserLearnedInform ? 0 : 1)) return ViewHolderFactory.ViewType.COURSE;
        else return ViewHolderFactory.ViewType.EVALUATION_ITEM;
    }
}
