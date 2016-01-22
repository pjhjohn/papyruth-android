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
import com.papyruth.support.utility.customview.EmptyStateView;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EvaluationSearchAdapter extends TrackerAdapter{
//    private static final String HIDE_INFORM = "EvaluationSearchAdapter.mHideInform"; // Inform is UNIQUE per Adapter.

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
    private RelativeLayout mFooterFullyLoadedIndicator;
    private Context mContext;
    private Navigator mNavigator;
    private int mPage;

    public EvaluationSearchAdapter(Context context, EmptyStateView emptystate, Navigator navigator,RecyclerViewItemObjectClickListener listener) {
        mEmptyState = emptystate;
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
        mPage = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType == ViewHolderFactory.ViewType.HEADER){
            viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {}, R.layout.cardview_white_0dp);
        } else {
            viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
                if (position == mIndexFooter) {
                    mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY);
                } else if(position - mIndexContent >= 0 && position - mIndexContent < mCourses.size())
                    mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mCourses.get(position - mIndexContent));
            });
        }
        if(viewType == ViewHolderFactory.ViewType.SHADOW && viewHolder instanceof VoidViewHolder) {
            mShadow = (FrameLayout) viewHolder.itemView.findViewById(R.id.cardview_shadow);
            if(mCourses.isEmpty()) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            else mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
        if (viewHolder instanceof FooterViewHolder) {
            mFooterBorder = viewHolder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewHolder.itemView.findViewById(R.id.material_progress_medium);
            mFooterFullyLoadedIndicator = (RelativeLayout) viewHolder.itemView.findViewById(R.id.footer_fully_loaded_indicator);
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
            if(mEmptyState.getVisibility() != View.VISIBLE)
                AnimatorHelper.FADE_IN(mEmptyState).start();
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            mEmptyState.setIconDrawable(R.drawable.emptystate_search).setTitle(R.string.emptystate_title_search_result).setBody(R.string.emptystate_body_search_result).show();
        }else{
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform?  0 : 1);
            mIndexContent= 1 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mCourses.size() + mIndexContent;
            mPage++;
            notifyDataSetChanged();
            if(mEmptyState.getVisibility() == View.VISIBLE)
                AnimatorHelper.FADE_OUT(mEmptyState).start();
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_white);
            if(mFullyLoad) AnimatorHelper.FADE_IN(mFooterFullyLoadedIndicator).start();
            else AnimatorHelper.FADE_OUT(mFooterFullyLoadedIndicator).start();
        }
    }

    private boolean mLoadding = false, mFullyLoad = false;
    public void searchCourse(CandidateData candidate, String query, boolean clear) {
        if (clear) {
            mPage = 1;
            mFullyLoad = false;
        } else if(mLoadding || mFullyLoad) return;
        mLoadding = true;
        if(candidate.course_id != null){
            nextEvaluatonStep(candidate);
        } else {
            if (mFooterMaterialProgressBar != null) AnimatorHelper.FADE_IN(mFooterMaterialProgressBar).start();
            Api.papyruth()
                .get_search_search(
                    User.getInstance().getAccessToken(),
                    User.getInstance().getUniversityId(),
                    candidate.lecture_id,
                    candidate.professor_id,
                    query,
                    mPage,
                    null
                )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(courses -> {
                    mLoadding = false;
                    if (courses != null) {
                        if (courses.isEmpty()) mFullyLoad = true;
                        if (candidate.professor_id != null && candidate.lecture_id != null && courses.size() > 0)
                            nextEvaluatonStep(courses.get(0));
                        else {
                            if (clear) mCourses.clear();
                            mCourses.addAll(courses);
                        }
                    }
                    if (mFooterMaterialProgressBar != null)
                        AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                    reconfigure();
                }, error -> {
                    mLoadding = false;
                    ErrorHandler.handle(error, this.getFragment());
                    if (mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                });
        }
    }
    public void nextEvaluatonStep(CourseData course) {
        if(course.id == null || !course.id.equals(EvaluationForm.getInstance().getCourseId()))
            EvaluationForm.getInstance().clear();
        EvaluationForm.getInstance().setCourseId(course.id);
        EvaluationForm.getInstance().setLectureName(course.name);
        EvaluationForm.getInstance().setProfessorName(course.professor_name);

        checkEvaluationPossible(course.id);
    }
    public void nextEvaluatonStep(CandidateData candidate) {
        if(candidate.course_id == null || !candidate.course_id.equals(EvaluationForm.getInstance().getCourseId()))
            EvaluationForm.getInstance().clear();
        EvaluationForm.getInstance().setCourseId(candidate.course_id);
        EvaluationForm.getInstance().setLectureName(candidate.lecture_name);
        EvaluationForm.getInstance().setProfessorName(candidate.professor_name);

        checkEvaluationPossible(candidate.course_id);
    }

    private void checkEvaluationPossible(int courseId){
        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), courseId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    mNavigator.navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_ALREADY_REGISTERED);
                }
            }, error -> ErrorHandler.handle(error, this.getFragment()));
    }
}