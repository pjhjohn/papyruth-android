package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppManager;
import com.papyruth.android.AppTracker;
import com.papyruth.android.R;
import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.CourseViewHolder;
import com.papyruth.android.recyclerview.viewholder.EvaluationItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.recyclerview.viewholder.VoidViewHolder;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.utils.view.customview.EmptyStateView;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IAdapter{
    private static final String HIDE_INFORM = "CourseAdapter.mHideInform"; // Inform is UNIQUE per Adapter.
    private final Context mContext;
    private SwipeRefreshLayout mSwipeRefresh;
    private EmptyStateView mEmptyState;
    private List<EvaluationData> mEvaluations;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemObjectClickListener;
    private boolean mHideInform;
    private boolean mHideShadow;
    private Integer mSinceId;
    private int mIndexHeader; // INDEX 0
    private int mIndexInform; // UNDER HEADER unless user learned inform, -1 otherwise
    private int mIndexSingle; // UNDER INFORM if exists, -1 otherwise
    private int mIndexShadow; // UNDER SINGLE if exists, -1 otherwise
    private int mIndexContent;// UNDER SHADOW if exists.
    private int mIndexFooter; // AT LAST
    private FrameLayout mShadow;
    private View mFooterBorder;
    private RelativeLayout mFooterMaterialProgressBar;
    private ImageView mFooterFullyLoadedIndicator;
    private Navigator mNavigator;

    public CourseAdapter(Context context, SwipeRefreshLayout swiperefresh, EmptyStateView emptystate, Navigator navigator, RecyclerViewItemObjectClickListener listener) {
        mContext = context;
        mNavigator = navigator;
        mSwipeRefresh = swiperefresh;
        mEmptyState = emptystate;
        mEvaluations = new ArrayList<>();
        mRecyclerViewItemObjectClickListener = listener;
        mHideInform = AppManager.getInstance().getBoolean(HIDE_INFORM, false);
        mSinceId = null;
        mIndexHeader = 0;
        mIndexInform = mHideInform? -1 : 1;
        mIndexSingle = 1 + (mHideInform?  0 : 1);
        mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
        mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
        mIndexFooter = mEvaluations.size() + mIndexContent;

        mIndexLastHashtag = 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewholder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
            Timber.d("position : %s", position);
            if(!mHideInform && position == mIndexInform) {
                String action = null;
                switch(view.getId()) {
                    case R.id.inform_btn_optional :
                        AppManager.getInstance().putBoolean(HIDE_INFORM, true);
                        action = parent.getResources().getString(R.string.ga_event_hide_always);
                    case R.id.inform_btn_positive :
                        notifyItemRemoved(position);
                        mHideInform = true;
                        if(action == null) action = parent.getResources().getString(R.string.ga_event_hide_once);
                        AppTracker.getInstance().getTracker().send(
                            new HitBuilders.EventBuilder(parent.getResources().getString(R.string.ga_category_inform), action).build()
                        );
                        reconfigure();
                        break;
                    default : Timber.d("Unexpected view #%x", view.getId());
                }
            }
            else if(position == mIndexSingle) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Evaluation.getInstance());
            else if(position == mIndexFooter) { if(mFullyLoaded) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY); }
            else mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mEvaluations.get(position - mIndexContent));
        });
        if (viewType == ViewHolderFactory.ViewType.SHADOW && viewholder instanceof VoidViewHolder) mShadow = (FrameLayout) viewholder.itemView.findViewById(R.id.cardview_shadow);
        if (viewholder instanceof FooterViewHolder) {
            mFooterBorder = viewholder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewholder.itemView.findViewById(R.id.material_progress_medium);
            mFooterFullyLoadedIndicator = (ImageView) viewholder.itemView.findViewById(R.id.footer_fully_loaded_indicator);
        }
        return viewholder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) return;
        if (position == mIndexInform) {((InformViewHolder) holder).bind(R.string.inform_evaluation, R.color.inform_course); return; }
        if (position == mIndexSingle) {((CourseViewHolder) holder).bind(Course.getInstance()); return; }
        if (position == mIndexShadow) return;
        if (position == mIndexFooter) return;
        ((EvaluationItemViewHolder) holder).bind(mEvaluations.get(position - mIndexContent));
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if (position == mIndexInform) return ViewHolderFactory.ViewType.INFORM;
        if (position == mIndexSingle) return ViewHolderFactory.ViewType.COURSE;
        if (position == mIndexShadow) return ViewHolderFactory.ViewType.SHADOW;
        if (position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.EVALUATION_ITEM;
    }

    private int mIndexLastHashtag;
    public void addHashtagToViewHolder(){
        for(int i = mIndexLastHashtag; i < mEvaluations.size(); i++){
            final int index = i;
            Api.papyruth().get_evaluation_hashtag(User.getInstance().getAccessToken(), mEvaluations.get(i).id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( response -> {
                    mEvaluations.get(index).setHashtags(response.hashtags);
                    notifyItemChanged(index);
                }, error ->  {
                    mEvaluations.get(index).setHashtags(new ArrayList<>());
                    notifyItemChanged(index);
                    ErrorHandler.handle(error, this);
                });
        }
        mIndexLastHashtag = mEvaluations.size()-1;
    }

    private void reconfigure() {
        if(mEvaluations.isEmpty()) {
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = 1 + (mHideInform?  0 : 1);
            mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
            mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mEvaluations.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_IN(mEmptyState).start();
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            mShadow.setBackgroundResource(R.drawable.shadow_transparent);
        } else {
            mSinceId = mEvaluations.get(mEvaluations.size()-1).id;
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = 1 + (mHideInform?  0 : 1);
            mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
            mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mEvaluations.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_OUT(mEmptyState).start();
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
        addHashtagToViewHolder();
        if(mFullyLoaded != null && mFullyLoaded) AnimatorHelper.FADE_IN(mFooterFullyLoadedIndicator).start();
        else AnimatorHelper.FADE_OUT(mFooterFullyLoadedIndicator).start();
    }

    /* IAdapter methods*/
    @Override
    public void refresh() {
        mSwipeRefresh.setRefreshing(true);
        if(User.getInstance().needEmailConfirmed()){
            mSwipeRefresh.setRefreshing(false);
            AlertDialog.show(mContext, mNavigator, AlertDialog.Type.NEED_CONFIRMATION);
            return;
        }else if(User.getInstance().needMoreEvaluation()) {
            mSwipeRefresh.setRefreshing(false);
            AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_MANDATORY);
            return;
        }
        Api.papyruth().get_evaluations(User.getInstance().getAccessToken(),
            User.getInstance().getUniversityId(),
            null,
            null,
            null,
            Course.getInstance().getId())
            .map(response -> response.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                evaluations -> {
                    mSwipeRefresh.setRefreshing(false);
                    mEvaluations.clear();
                    mEvaluations.addAll(evaluations);
                    mLoading = false;
                    mFullyLoaded = false;
                    reconfigure();
                }, error -> {
                    mSwipeRefresh.setRefreshing(false);
                    ErrorHandler.handle(error, this);
                }
            );
    }

    private Boolean mLoading;
    private Boolean mFullyLoaded;
    @Override
    public void loadMore() {

        if(User.getInstance().needEmailConfirmed()){
            AlertDialog.show(mContext, mNavigator, AlertDialog.Type.NEED_CONFIRMATION);
            return;
        }else if(User.getInstance().needMoreEvaluation()) {
            AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_MANDATORY);
            return;
        }
        if(mLoading != null && mLoading) return;
        mLoading = true;
        if(mFullyLoaded != null && mFullyLoaded) return;
        mFullyLoaded = false;
        if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_IN(mFooterMaterialProgressBar).start();
        Api.papyruth()
            .get_evaluations(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                null,
                mSinceId == null ? null : mSinceId - 1,
                null,
                Course.getInstance().getId()
            )
            .map(response -> response.evaluations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(evaluations -> {
                if(evaluations != null) {
                    if(evaluations.isEmpty()) mFullyLoaded = true;
                    else mEvaluations.addAll(evaluations);
                }
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
                reconfigure();
            }, error -> {
                ErrorHandler.handle(error, this);
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
            });
    }
}
