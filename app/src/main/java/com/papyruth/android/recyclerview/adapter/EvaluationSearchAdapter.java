package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.papyruth.android.R;
import com.papyruth.android.fragment.main.EvaluationStep2Fragment;
import com.papyruth.android.model.CandidateData;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.CourseItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.recyclerview.viewholder.VoidViewHolder;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.support.utility.customview.EmptyStateView;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EvaluationSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
//    private static final String HIDE_INFORM = "BookmarkAdapter.mHideInform"; // Inform is UNIQUE per Adapter.

    private EmptyStateView mEmptyState;
    private List<CourseData> mCourses;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemObjectClickListener;
    private boolean mHideInform;
    private boolean mHideShadow;
    private int mIndexHeader; // INDEX 0
    private int mIndexInform; // UNDER HEADER unless user learned inform, -1 otherwise
    private int mIndexSingle; // UNDER INFORM if exists, -1 otherwise
    private int mIndexShadow; // UNDER SINGLE if exists, -1 otherwise
    private int mIndexContent;// UNDER SHADOW if exists.
    private int mIndexFooter; // AT LAST
    private FrameLayout mShadow;
    private View mFooterBorder;
    private RelativeLayout mFooterMaterialProgressBar;
    private Context mContext;
    private Navigator mNavigator;

    public EvaluationSearchAdapter(Context context, EmptyStateView emptystate, Navigator navigator,RecyclerViewItemObjectClickListener listener) {
        mEmptyState = emptystate;
//        mEmptyState.setIconDrawable(R.drawable.ic_light_history).setBody("hello").setTitle("error?").show();
        mContext = context;
        mNavigator = navigator;
        mCourses = new ArrayList<>();
        mRecyclerViewItemObjectClickListener = listener;
        mHideInform = true;
        mHideShadow = true;
        mIndexHeader = 0;
        mIndexInform = mHideInform? -1 : 1;
        mIndexSingle = -1;
        mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
        mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
        mIndexFooter = mCourses.size() + mIndexContent;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType == ViewHolderFactory.ViewType.HEADER){
            viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {}, R.layout.cardview_white_0dp);
        }else {
            viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
                if (position == mIndexFooter) {
                    mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY);
                } else
                    mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mCourses.get(position - mIndexContent));
            });
        }
        if(viewType == ViewHolderFactory.ViewType.SHADOW && viewHolder instanceof VoidViewHolder) mShadow = (FrameLayout) viewHolder.itemView.findViewById(R.id.cardview_shadow);
        if (viewHolder instanceof FooterViewHolder) {
            mFooterBorder = viewHolder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewHolder.itemView.findViewById(R.id.material_progress_medium);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) return;
        if (position == mIndexInform) return;
        if (position == mIndexSingle) return;
        if (position == mIndexShadow) return;
        if (position == mIndexFooter) return;
        ((CourseItemViewHolder) holder).bind(mCourses.get(position - mIndexContent));
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if (position == mIndexInform) return ViewHolderFactory.ViewType.INFORM;
        if (position == mIndexSingle) ;
        if (position == mIndexShadow) return ViewHolderFactory.ViewType.SHADOW;
        if (position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.COURSE_ITEM;
    }
    private void reconfigure(){
        if(mCourses.isEmpty()){
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mCourses.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_IN(mEmptyState).start();
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            if(mShadow != null)
                mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            mEmptyState.setBody(R.string.empty_state_content_empty_search_result)
                .setTitle(String.format(mContext.getResources().getString(R.string.empty_state_title_empty_something), mContext.getResources().getString(R.string.empty_state_content_empty_search_result)))
                .show();
        }else{
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mCourses.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_OUT(mEmptyState).start();
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            if(mShadow != null)
                mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
    }

    public void searchCourse(CandidateData candidate, String query) {
        if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_IN(mFooterMaterialProgressBar).start();
        Api.papyruth()
            .get_search_search(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                candidate.lecture_id,
                candidate.professor_id,
                query
            )
            .map(response -> response.courses)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(courses -> {
                if (courses != null) {
                    if (candidate.professor_id != null && candidate.lecture_id != null && courses.size() > 0)
                        nextEvaluatonStep(courses.get(0));
                    else {
                        mCourses.clear();
                        mCourses.addAll(courses);
                    }
                }
                if (mFooterMaterialProgressBar != null)
                    AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                reconfigure();
            }, error -> {
                ErrorHandler.handle(error, this);
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
            });
    }
    public void nextEvaluatonStep(CourseData course) {
        if(course.id == null || !course.id.equals(EvaluationForm.getInstance().getCourseId())) EvaluationForm.getInstance().clear();
        EvaluationForm.getInstance().setCourseId(course.id);
        EvaluationForm.getInstance().setLectureName(course.name);
        EvaluationForm.getInstance().setProfessorName(course.professor_name);
        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), course.id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    mNavigator.navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_ALREADY_REGISTERED);
                }
            }, error -> ErrorHandler.handle(error, this));
    }
}