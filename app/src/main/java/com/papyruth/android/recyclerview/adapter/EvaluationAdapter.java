package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.papyruth.support.utility.error.ErrorDefaultRetrofit;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.papyruth.support.utility.customview.EmptyStateView;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IAdapter {
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

        if (Evaluation.getInstance().getId() != null){
            Api.papyruth().get_evaluation(User.getInstance().getAccessToken(),Evaluation.getInstance().getId())
                .map(response -> response.evaluation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evaluationData -> {
                    Evaluation.getInstance().update(evaluationData);
                    mToolbar.getMenu().findItem(R.id.menu_evaluation_edit).setVisible(Evaluation.getInstance().getUserId() != null && Evaluation.getInstance().getUserId().equals(User.getInstance().getId()));
                    notifyItemChanged(mIndexSingle);
                }, error -> ErrorHandler.handle(error, this));
        }
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
            else if(position == mIndexFooter) { if(mFullyLoaded) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY); }
            else mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mComments.get(position - mIndexContent));
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
        }
        return viewholder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) return;
        if (position == mIndexInform) {((InformViewHolder) holder).bind(R.string.inform_evaluation, R.color.inform_evaluation); return; }
        if (position == mIndexSingle) {((EvaluationViewHolder) holder).bind(Evaluation.getInstance()); return; }
        if (position == mIndexShadow) return;
        if (position == mIndexFooter) return;
        ((CommentItemViewHolder) holder).bind(mComments.get(position - mIndexContent));
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

    private void reconfigure() {
        if(mComments.isEmpty()) {
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = 1 + (mHideInform?  0 : 1);
            mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
            mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mComments.size() + mIndexContent;
            notifyDataSetChanged();
            if(mIndexSingle > 0) this.notifyItemChanged(mIndexSingle);
            AnimatorHelper.FADE_IN(mEmptyState).start();
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            if(mIndexSingle < 0) mEmptyState.setIconDrawable(R.drawable.ic_password_48dp).setBody(R.string.empty_state_content_empty_comment)
                .setTitle(String.format(mContext.getResources().getString(R.string.empty_state_title_empty_something), mContext.getResources().getString(R.string.empty_state_content_empty_comment)))
                .show();
        } else {
            mSinceId = mComments.get(mComments.size()-1).id;
            mIndexHeader = 0;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = 1 + (mHideInform?  0 : 1);
            mIndexShadow = mHideShadow? -1 : 2 + (mHideInform?  0 : 1);
            mIndexContent= 2 + (mHideShadow ? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mComments.size() + mIndexContent;
            notifyDataSetChanged();
            if(mIndexSingle > 0) this.notifyItemChanged(mIndexSingle);
            AnimatorHelper.FADE_OUT(mEmptyState).start();
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
        if(mFullyLoaded != null && mFullyLoaded) AnimatorHelper.FADE_IN(mFooterFullyLoadedIndicator).start();
        else AnimatorHelper.FADE_OUT(mFooterFullyLoadedIndicator).start();
    }

    /* IAdapter methods*/
    @Override
    public void refresh() {
        mSwipeRefresh.setRefreshing(true);

        Observable.combineLatest(
                Api.papyruth().get_evaluation(User.getInstance().getAccessToken(), Evaluation.getInstance().getId() ),
                Api.papyruth().get_comments(User.getInstance().getAccessToken(), Evaluation.getInstance().getId(), null, null, null),
                (a, b)->{
                    if(a.evaluation != null){
                        Evaluation.getInstance().update(a.evaluation);
                    }
                    if(b.comments != null){
                        this.mComments.clear();
                        this.mComments.addAll(b.comments);
                    }
                    return a.evaluation != null && b.comments != null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mSwipeRefresh.setRefreshing(false);
                    mLoading = false;
                    mFullyLoaded = false;
                    reconfigure();
                }, error -> {
                    mSwipeRefresh.setRefreshing(false);
                    if (error instanceof RetrofitError) {
                        if (ErrorNetwork.handle(((RetrofitError) error), this).handled) {
                            this.mEmptyState.setIconDrawable(R.drawable.ic_password_48dp).setTitle(R.string.empty_state_title_network).setBody(R.string.empty_state_content_network).show();
                        } else {
                            this.mEmptyState.setIconDrawable(R.drawable.ic_password_48dp).setTitle(R.string.empty_state_title_network).setBody(R.string.empty_state_content_network).show();
                            ErrorDefaultRetrofit.handle(((RetrofitError) error), this);
                        }
                    } else {
                        ErrorHandler.handle(error, this);
                    }
                });
    }

    private Boolean mLoading;
    private Boolean mFullyLoaded;
    @Override
    public void loadMore() {
        if(mLoading != null && mLoading) return;
        mLoading = true;
        if(mFullyLoaded != null && mFullyLoaded) return;
        mFullyLoaded = false;
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
                if(comments != null) {
                    if(comments.isEmpty()) mFullyLoaded = true;
                    else mComments.addAll(comments);
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