package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.FavoriteData;
import com.papyruth.android.model.Footer;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.CourseItemViewHolder;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.HeaderViewHolder;
import com.papyruth.android.recyclerview.viewholder.InformViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.android.recyclerview.viewholder.VoidViewHolder;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.EmptyStateView;
import com.papyruth.support.utility.error.Error;
import com.papyruth.support.utility.error.ErrorDefaultRetrofit;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.error.ErrorNetwork;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FavoriteAdapter extends TrackerAdapter implements IAdapter, Error.OnReportToGoogleAnalytics {
    private static final String HIDE_INFORM = "FavoriteAdapter.mHideInform"; // Inform is UNIQUE per Adapter.
    private Context mContext;
    private SwipeRefreshLayout mSwipeRefresh;
    private EmptyStateView mEmptyState;
    private List<FavoriteData> mFavorites;
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
    private boolean mTempHideInform;

    public FavoriteAdapter(Context context, SwipeRefreshLayout swiperefresh, EmptyStateView emptystate, RecyclerViewItemObjectClickListener listener) {
        mContext = context;
        mSwipeRefresh = swiperefresh;
        mEmptyState = emptystate;
        mFavorites = new ArrayList<>();
        mRecyclerViewItemObjectClickListener = listener;
        mTempHideInform = false;
        mHideInform = true;
        mHideShadow = mHideInform;
        mSinceId = null;
        mIndexHeader = 0;
        mIndexInform = mHideInform? -1 : 1;
        mIndexSingle = -1;
        mIndexShadow = mHideShadow? -1 : 1 + (mHideInform? 0 : 1);
        mIndexContent= 1 + (mHideShadow? 0 : 1) + (mHideInform? 0 : 1);
        mIndexFooter = mFavorites.size() + mIndexContent;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position) -> {
            if(!mHideInform && position == mIndexInform) {
                switch (view.getId()) {
                    case R.id.inform_btn_optional:
                        AppManager.getInstance().putBoolean(HIDE_INFORM, true);
                    case R.id.inform_btn_positive:
                        notifyItemRemoved(position);
                        mHideInform = true;
                        mHideShadow = true;
                        mTempHideInform = true;
                        reconfigure();
                        break;
                    default:
                        Timber.d("Unexpected view #%x", view.getId());
                }
            } else if(position == mIndexFooter) {
                if(mFullyLoaded) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, Footer.DUMMY);
            } else if(position - mIndexContent >= 0 && position - mIndexContent < mFavorites.size()) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mFavorites.get(position - mIndexContent).course);
        });
        if(viewType == ViewHolderFactory.ViewType.SHADOW && viewHolder instanceof VoidViewHolder) {
            mShadow = (FrameLayout) viewHolder.itemView.findViewById(R.id.cardview_shadow);
            if(mFavorites.isEmpty()) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            else mShadow.setBackgroundResource(R.drawable.shadow_white);
        }
        if(viewHolder instanceof FooterViewHolder) {
            mFooterBorder = viewHolder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewHolder.itemView.findViewById(R.id.material_progress_medium);
            mFooterFullyLoadedIndicator = (RelativeLayout) viewHolder.itemView.findViewById(R.id.footer_fully_loaded_indicator);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position <= mIndexHeader) {((HeaderViewHolder) holder).bind(R.color.toolbar_red); return;}
        if(position == mIndexInform) {
            ((InformViewHolder) holder).bind(R.string.inform_favorite, R.color.inform_favorite);
            return;
        }
        if(position == mIndexSingle) return;
        if(position == mIndexShadow) return;
        if(position == mIndexFooter) return;
        ((CourseItemViewHolder) holder).bind(mFavorites.get(position - mIndexContent).course);
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if(position == mIndexInform) return ViewHolderFactory.ViewType.INFORM;
        if(position == mIndexSingle) ;
        if(position == mIndexShadow) return ViewHolderFactory.ViewType.SHADOW;
        if(position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.COURSE_ITEM;
    }

    private void reconfigure() {
        Timber.d("Reconfigured with mFavorites having size of %d", mFavorites.size());
        if(mFavorites.isEmpty()) {
            mIndexHeader = 0;
            mHideInform = AppManager.getInstance().getBoolean(HIDE_INFORM, false) || mTempHideInform;
            mHideShadow = mHideInform;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform? 0 : 1);
            mIndexContent = 1 + (mHideShadow? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mFavorites.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_OUT(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_transparent);
            mEmptyState.setIconDrawable(R.drawable.emptystate_favorite).setTitle(R.string.emptystate_title_favorite).setBody(R.string.emptystate_body_favorite).show();
        } else {
            mSinceId = mFavorites.get(mFavorites.size() - 1).id;
            mIndexHeader = 0;
            mHideInform = AppManager.getInstance().getBoolean(HIDE_INFORM, false) || mTempHideInform;
            mHideShadow = mHideInform;
            mIndexInform = mHideInform? -1 : 1;
            mIndexSingle = -1;
            mIndexShadow = mHideShadow? -1 : 1 + (mHideInform? 0 : 1);
            mIndexContent = 1 + (mHideShadow? 0 : 1) + (mHideInform? 0 : 1);
            mIndexFooter = mFavorites.size() + mIndexContent;
            notifyDataSetChanged();
            AnimatorHelper.FADE_IN(mFooterBorder).start();
            if(mShadow != null) mShadow.setBackgroundResource(R.drawable.shadow_white);
            mEmptyState.hide();
        }
        if(mFullyLoaded != null && mFullyLoaded) AnimatorHelper.FADE_IN(mFooterFullyLoadedIndicator).start();
        else AnimatorHelper.FADE_OUT(mFooterFullyLoadedIndicator).start();
    }

    @Override
    public void refresh() {
        mSwipeRefresh.setRefreshing(true);
        Api.papyruth()
            .get_users_me_favorites(
                User.getInstance().getAccessToken(),
                null,
                mSinceId == null ? null : mSinceId - 1,
                null
            )
            .map(response -> response.favorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(favorites -> {
                mSwipeRefresh.setRefreshing(false);
                mFavorites.clear();
                mFavorites.addAll(favorites);
                mFullyLoaded = false;
                mLoading = false;
                reconfigure();
            }, error -> {
                mSwipeRefresh.setRefreshing(false);
                if(error instanceof RetrofitError) {
                    if(ErrorNetwork.handle(((RetrofitError) error), this.getFragment()).handled) {
                        mEmptyState.setIconDrawable(R.drawable.emptystate_network).setTitle(R.string.emptystate_title_network).setBody(R.string.emptystate_body_network).show();
                    } else {
                        mEmptyState.setIconDrawable(R.drawable.emptystate_favorite).setTitle(R.string.emptystate_title_favorite).setBody(R.string.emptystate_body_favorite).show();
                        ErrorDefaultRetrofit.handle(((RetrofitError) error), this.getFragment());
                    }
                } else {
                    ErrorHandler.handle(error, this.getFragment());
                }
                error.printStackTrace();
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
        if(mFooterMaterialProgressBar != null)
            AnimatorHelper.FADE_IN(mFooterMaterialProgressBar).start();
        Api.papyruth()
            .get_users_me_favorites(
                User.getInstance().getAccessToken(),
                null,
                mSinceId == null ? null : mSinceId - 1,
                null
            )
            .map(response -> response.favorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(favorites -> {
                if(favorites != null) {
                    mFavorites.addAll(favorites);
                    mFullyLoaded = favorites.isEmpty();
                }
                if(mFooterMaterialProgressBar != null)
                    AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
                reconfigure();
            }, error -> {
                if(error instanceof RetrofitError) {
                    if(ErrorNetwork.handle(((RetrofitError) error), this.getFragment()).handled) {
                        mEmptyState.setIconDrawable(R.drawable.emptystate_network).setTitle(R.string.emptystate_title_network).setBody(R.string.emptystate_body_network).show();
                    } else {
                        mEmptyState.setIconDrawable(R.drawable.emptystate_favorite).setTitle(R.string.emptystate_title_favorite).setBody(R.string.emptystate_body_favorite).show();
                        ErrorDefaultRetrofit.handle(((RetrofitError) error), this.getFragment());
                    }
                } else ErrorHandler.handle(error, this.getFragment());
                if(mFooterMaterialProgressBar != null) AnimatorHelper.FADE_OUT(mFooterMaterialProgressBar).start();
                mLoading = false;
            });
    }

    @Override
    public void onReportToGoogleAnalytics(String cause, String from, boolean isFatal) {
        Timber.d("cause %s, from %s, isFatal %s", cause, from, isFatal);
    }
}
