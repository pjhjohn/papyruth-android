package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.papyruth.android.AppManager;
import com.papyruth.android.AppTracker;
import com.papyruth.android.R;
import com.papyruth.android.model.CommentData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.CommentItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.EvaluationViewHolder;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.recyclerview.viewholder.VoidViewHolder;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.EmptyStateView;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EvaluationAdapter extends TrackerAdapter implements IAdapter {
    private static final String HIDE_INFORM = "EvaluationAdapter.mHideInform";
    private final Context mContext;
    private SwipeRefreshLayout mSwipeRefresh;
    private EmptyStateView mEmptyState;
    private Toolbar mToolbar;
    private List<CommentData> mComments;
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
    private RelativeLayout mFooterFullyLoadedIndicator;

    private int mCommentId;

    public EvaluationAdapter(Context context, SwipeRefreshLayout swiperefresh, EmptyStateView emptystate, Toolbar toolbar, RecyclerViewItemObjectClickListener listener) {
        mContext = context;
        mSwipeRefresh = swiperefresh;
        mEmptyState = emptystate;
        mToolbar = toolbar;
        mComments = new ArrayList<>();
        mRecyclerViewItemObjectClickListener = listener;
        mHideInform = AppManager.getInstance().getBoolean(HIDE_INFORM, false);
        mSinceId = null;
        mIndexHeader = 0;
        mIndexInform = mHideInform? -1 : 1;
        mIndexSingle = 1 + (mHideInform?  0 : 1);
        mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
        mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
        mIndexFooter = mComments.size() + mIndexContent;
        mCommentId = -1;

        if (Evaluation.getInstance().getId() != null){
            Api.papyruth().get_evaluation(User.getInstance().getAccessToken(),Evaluation.getInstance().getId())
                .map(response -> response.evaluation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluationData -> {
                    Evaluation.getInstance().update(evaluationData);
                    mToolbar.getMenu().findItem(R.id.menu_evaluation_edit).setVisible(Evaluation.getInstance().getUserId() != null && Evaluation.getInstance().getUserId().equals(User.getInstance().getId()));
                    notifyItemChanged(mIndexSingle);
                }, error -> ErrorHandler.handle(error, this.getFragment(), true));
        }
        loadMore();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewholder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
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
            else if(position == mIndexFooter) { mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY); }
            else if(position - mIndexContent < mComments.size() && position > 0) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mComments.get(position - mIndexContent));
        });
        if (viewType == ViewHolderFactory.ViewType.SHADOW && viewholder instanceof VoidViewHolder) {
            mShadow = (FrameLayout) viewholder.itemView.findViewById(R.id.cardview_shadow);
            if(mComments.isEmpty()) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            else mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
        if (viewholder instanceof FooterViewHolder) {
            mFooterBorder = viewholder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewholder.itemView.findViewById(R.id.material_progress_medium);
            mFooterFullyLoadedIndicator = (RelativeLayout) viewholder.itemView.findViewById(R.id.footer_fully_loaded_indicator);
            mFooterFullyLoadedIndicator.setVisibility(View.VISIBLE);
        }
        return viewholder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) return;
        if (position == mIndexInform) {((InformViewHolder) holder).bind(R.string.inform_evaluation, R.color.inform_evaluation); return; }
        if (position == mIndexSingle) { ((EvaluationViewHolder) holder).bind(Evaluation.getInstance(), this.isMoreComment()); return; }
        if (position == mIndexShadow) return;
        if (position == mIndexFooter) return;
        if (position - mIndexContent < mComments.size()) ((CommentItemViewHolder) holder).bind(mComments.get(position - mIndexContent));
        if (position - mIndexContent < mComments.size() && mComments.get(position - mIndexContent).id.equals(mCommentId)) {
//            AnimatorHelper.FOCUS_EFFECT(holder.itemView).start();
            mCommentId = -1;
        }
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if (position == mIndexInform) return ViewHolderFactory.ViewType.INFORM;
        if (position == mIndexSingle) return ViewHolderFactory.ViewType.EVALUATION;
        if (position == mIndexShadow) return ViewHolderFactory.ViewType.SHADOW;
        if (position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.COMMENT_ITEM;
    }

    public void setCommentId(int commentId){
        this.mCommentId = commentId;
    }
    public int getFocusIndex() {
        if (mCommentId > 0 && mComments.get(mComments.size()-1).id < mCommentId){
            for(int i = 0; i < mComments.size(); i++){
                if(mComments.get(i).id.equals(mCommentId))
                    return i + mIndexContent;
            }
        }
        return -1;
    }
    public boolean isMoreComment(){
        return Evaluation.getInstance().getCommentCount() != null && Evaluation.getInstance().getCommentCount() - this.mComments.size() > 0;
    }

    private void reconfigure() {
        if(mComments.isEmpty()) {
            mEmptyState.hide();
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = 1 + (mHideInform?  0 : 1);
            mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
            mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mComments.size() + mIndexContent;
            if(mIndexSingle > 0) this.notifyItemChanged(mIndexSingle);
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
        } else {
            mEmptyState.hide();
            mSinceId = mComments.get(0).id;
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = 1 + (mHideInform?  0 : 1);
            mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
            mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mComments.size() + mIndexContent;
            if(mIndexSingle > 0) this.notifyItemChanged(mIndexSingle);
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_white);

            if(mCommentId > 0 && mComments.get(0).id > mCommentId){
                loadMore();
            }
        }
    }

    private void addAllComment(List<CommentData> commentDatas){
        Collections.reverse(commentDatas);
        this.mComments.addAll(0, commentDatas);
        this.notifyItemRangeInserted(mIndexContent, commentDatas.size());
    }

    private void updateAllComment(List<CommentData> commentDatas){
        int size = mComments.size();
        this.notifyItemRangeRemoved(mIndexContent, size);
        this.mComments.clear();
        Collections.reverse(commentDatas);
        this.mComments.addAll(0, commentDatas);
        this.notifyItemRangeInserted(mIndexContent, commentDatas.size());
    }

    /* IAdapter methods*/
    @Override
    public void refresh() {
        mSwipeRefresh.setRefreshing(true);
        Observable.combineLatest(
            Api.papyruth().get_evaluation(User.getInstance().getAccessToken(), Evaluation.getInstance().getId()),
            Api.papyruth().get_comments(User.getInstance().getAccessToken(), Evaluation.getInstance().getId(), null, null, null),
            (evaluationResponse, commentsResponse) -> {
                this.mCommentId = -1;
                if(evaluationResponse.evaluation != null) {
                    Evaluation.getInstance().update(evaluationResponse.evaluation);
                }
                return commentsResponse.comments;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                mSwipeRefresh.setRefreshing(false);
                mLoading = false;
                this.updateAllComment(comments);
                reconfigure();
            }, error -> {
                mSwipeRefresh.setRefreshing(false);
                if(ErrorNetwork.handle(error, this.getFragment()).handled) mEmptyState.setIconDrawable(R.drawable.emptystate_network).setTitle(R.string.emptystate_title_network).setBody(R.string.emptystate_body_network).show();
                else ErrorHandler.handle(error, this.getFragment(), true);
            });
    }

    private Boolean mLoading;
    @Override
    public void loadMore() {
        if(mLoading != null && mLoading) return;
        mLoading = true;
        if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_IN(mFooterMaterialProgressBar).start();
        Api.papyruth()
            .get_comments(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                null,
                mSinceId == null ? null : mSinceId - 1,
                null
            )
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(comments -> {
                if (comments != null) {
                    this.addAllComment(comments);
                    this.notifyItemChanged(mIndexSingle);
                }
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
                reconfigure();
            }, error -> {
                if(ErrorNetwork.handle(error, this.getFragment()).handled) mEmptyState.setIconDrawable(R.drawable.emptystate_network).setTitle(R.string.emptystate_title_network).setBody(R.string.emptystate_body_network).show();
                else ErrorHandler.handle(error, this.getFragment(), true);
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
            });
    }
}